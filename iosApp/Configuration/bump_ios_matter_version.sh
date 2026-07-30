#!/bin/bash
# Bumps the pinned ios-matter SwiftPM version and leaves the tree in a state
# that actually builds from cold caches.
#
#   ./iosApp/Configuration/bump_ios_matter_version.sh 0.0.12   # bump to 0.0.12
#   ./iosApp/Configuration/bump_ios_matter_version.sh          # re-run the
#                                                              # clean cycle at
#                                                              # the current pin
#
# WHY THIS EXISTS
#
# Bumping the version by hand takes six steps, two of which are unintuitive:
# the first build FAILS ON PURPOSE, and stale DerivedData has to be thrown away
# or the next person to clear it inherits the breakage. This script does all of
# it in order and fails loudly if any step goes wrong.
#
# WHAT IT DOES
#
#   0. Checks the requested tag actually exists on the remote, before touching
#      anything. A typo'd version otherwise costs two full builds before it
#      surfaces, as an error that does not name the version.
#   1. Rewrites version = exact("...") in composeApp/build.gradle.kts -- the
#      only source of truth for the pin.
#   2. Clears DerivedData and .swiftpm-locks/*/swiftPMCheckout, and refreshes
#      SwiftPM's global bare mirror, so everything after this point is resolved
#      cold. A bad pin stays invisible while those caches hold the old checkout.
#   3. Builds once, EXPECTING FAILURE with "Synthetic project regenerated":
#      Kotlin rewrites the generated Package.swift mid-build and refuses to
#      continue against a manifest it just wrote. Only the Xcode-invoked
#      generator writes that file, so this first failure cannot be avoided from
#      the CLI -- it is not a broken build. Any OTHER failure aborts here.
#   4. Re-resolves the package graph against the regenerated manifests.
#   5. Prunes orphaned generated subpackages (see below).
#   6. Builds again -- this one must succeed.
#   7. Runs check_swiftpm_lockfiles.sh.
#   8. Prints the file list to commit as ONE commit.
#
# ORPHANED SUBPACKAGES
#
# Kotlin generates one subpackage per module under
# KotlinMultiplatformLinkedPackage/subpackages/, named after the module's
# Maven coordinates or project path. Rename or re-scope a module and the old
# subpackage is left behind, still tracked in git, still pinning whatever
# ios-matter version was current when it was generated -- but no longer
# referenced by the root Package.swift.
#
# That orphan is not merely untidy. Xcode's PIF cache in DerivedData keeps
# serving its package product long after it leaves the graph, and the build
# dies with
#
#     error: Missing package product '<orphan name>' (in target 'iosApp')
#
# at CreateBuildDescription, before anything compiles. Nothing in
# project.pbxproj or workspace-state.json mentions the product, which makes it
# a miserable thing to chase. Step 5 deletes any subpackage directory the
# sibling root Package.swift does not declare.
#
# THE STALE GLOBAL MIRROR
#
# SwiftPM keeps one bare mirror per package in its GLOBAL cache,
#
#     ~/Library/Caches/org.swift.swiftpm/repositories/<name>-<hash>
#
# and resolves against it without necessarily contacting the remote. A tag
# pushed after that mirror was last fetched is therefore INVISIBLE, and
# resolution fails with
#
#     no versions of 'ios-matter' match the requirement <version>
#
# for a tag that demonstrably exists on GitHub. The mirror lives outside
# DerivedData and outside .swiftpm-locks, so clearing either does nothing --
# which makes this look like a bad pin rather than a cold cache. Step 2 fetches
# the mirror (and drops it if the fetch fails, so SwiftPM re-clones).

set -euo pipefail

cd "$(dirname "$0")/../.."

XCODEPROJ="iosApp/iosApp.xcodeproj"
SCHEME="iOS debug"
# Overridable so CI can build for a concrete simulator or for a device.
DESTINATION="${BUMP_DESTINATION:-generic/platform=iOS Simulator}"
GRADLE_FILE="composeApp/build.gradle.kts"
SWIFTPM_CACHE="${SWIFTPM_CACHE_DIR:-$HOME/Library/Caches/org.swift.swiftpm}"

logdir=$(mktemp -d)

# Set once the run has done everything it promised. Anything else means the tree
# is half-bumped -- the pin moved but the lockfiles did not -- and committing it
# is exactly the drift check_swiftpm_lockfiles.sh exists to catch. The pin is
# deliberately NOT reverted: when build 2 fails because the new version changed
# its API, the fix is to adapt the source, and pulling the pin back out from
# under the user would undo the work they are mid-way through.
completed=0
pin_moved=""
on_exit() {
  if [ "$completed" -ne 1 ] && [ -n "$pin_moved" ]; then
    echo
    echo "NOTE: $GRADLE_FILE is now pinned at $pin_moved but this run did not"
    echo "      finish, so the lockfiles do NOT match it yet. Do not commit as"
    echo "      is. Either fix the failure above and re-run, or revert the pin:"
    echo "        git checkout -- $GRADLE_FILE"
  fi
  echo
  echo "Logs kept in $logdir"
}
trap on_exit EXIT

step() { echo; echo "==> $*"; }

# Print whatever explains a failed xcodebuild run. Deliberately not a pipeline
# ending in `head ... || tail ...`: head exits 0 on empty input, so that form
# silently prints nothing when the log has no "error:" lines at all.
#
# Two trailing lines of context are kept, because xcodebuild puts the useful
# half of a resolution failure UNDER the "error:" line:
#
#     xcodebuild: error: Could not resolve package dependencies:
#       ... no versions of 'ios-matter' match the requirement 0.0.13
#
# Grepping "error:" alone reports that something failed without saying what.
# Duplicate lines are dropped in place rather than with `sort -u`, which would
# scramble each error away from its own context.
report_failure() {
  local log="$1" errors
  errors=$(grep -E -A2 "error:" "$log" | awk '!seen[$0]++' | head -40 || true)
  if [ -n "$errors" ]; then
    printf '%s\n' "$errors"
  else
    echo "(no 'error:' lines; last 40 lines of $log)"
    tail -40 "$log"
  fi
}

# --- 0. Sanity ---------------------------------------------------------------

command -v xcodebuild >/dev/null || { echo "error: xcodebuild not on PATH"; exit 1; }
[ -f "$GRADLE_FILE" ] || { echo "error: $GRADLE_FILE not found"; exit 1; }

current=$(sed -n 's/.*version = exact("\([^"]*\)").*/\1/p' "$GRADLE_FILE")
matches=$(grep -c 'version = exact(' "$GRADLE_FILE" || true)
if [ "$matches" -ne 1 ]; then
  echo "error: expected exactly one 'version = exact(...)' in $GRADLE_FILE, found $matches"
  echo "       this script cannot tell which pin you mean -- bump by hand"
  exit 1
fi

target="${1:-$current}"
if ! [[ "$target" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "error: '$target' is not a version like 1.2.3"
  exit 1
fi

package_url=$(sed -n 's/.*url = url("\([^"]*\)").*/\1/p' "$GRADLE_FILE" | head -1)
if [ -z "$package_url" ]; then
  echo "error: could not read the package url from $GRADLE_FILE"
  exit 1
fi
# Mirror directories are named <repo>-<hash>; only the basename is predictable.
package_name=$(basename "$package_url" .git)

# Ask the REMOTE, not the mirror, so a tag that exists is never reported as
# missing and a tag that does not exist is caught before any cache is cleared.
step "Checking $package_name $target exists on the remote"
if remote_tags=$(git ls-remote --tags "$package_url" 2>"$logdir/lsremote.log"); then
  if awk '{print $2}' <<<"$remote_tags" \
       | grep -qxF -e "refs/tags/$target" -e "refs/tags/v$target"; then
    echo "    found"
  else
    echo
    echo "error: $package_url has no tag '$target'"
    echo "       latest tags:"
    awk '{print $2}' <<<"$remote_tags" | sed 's|refs/tags/||' \
      | grep -v '\^{}$' | sort -V | tail -5 | sed 's/^/         /'
    echo
    echo "       Nothing has been changed. Push the tag first, or pass a tag"
    echo "       that exists."
    exit 1
  fi
elif [ "${BUMP_SKIP_TAG_CHECK:-0}" = "1" ]; then
  echo "    warning: remote unreachable, continuing (BUMP_SKIP_TAG_CHECK=1)"
else
  echo
  echo "error: could not reach $package_url to verify the tag:"
  sed 's/^/       /' "$logdir/lsremote.log"
  echo "       Re-run with BUMP_SKIP_TAG_CHECK=1 to bump without this check."
  exit 1
fi

# --- 1. Rewrite the pin ------------------------------------------------------

if [ "$target" = "$current" ]; then
  step "ios-matter already pinned at $current -- re-running the clean cycle"
else
  step "Bumping ios-matter $current -> $target in $GRADLE_FILE"
  # Anchored on the whole exact(...) call so a version string appearing
  # elsewhere in the file cannot be hit by accident.
  perl -pi -e "s/version = exact\(\"\Q$current\E\"\)/version = exact(\"$target\")/" "$GRADLE_FILE"
  grep -n 'version = exact(' "$GRADLE_FILE"
  pin_moved="$target"
fi

# --- 2. Clear the caches that hide bad pins ----------------------------------

step "Clearing DerivedData and SwiftPM checkouts"
rm -rf ~/Library/Developer/Xcode/DerivedData/iosApp-*
rm -rf .swiftpm-locks/*/swiftPMCheckout

# The global mirror is the one cache that survives everything above, and a
# stale one makes a freshly pushed tag unresolvable. See THE STALE GLOBAL
# MIRROR in the header.
step "Refreshing SwiftPM's global mirror of $package_name"
found_mirror=0
for mirror in "$SWIFTPM_CACHE"/repositories/"$package_name"-*; do
  [ -d "$mirror" ] || continue
  found_mirror=1
  if git -C "$mirror" fetch --tags --prune --quiet 2>"$logdir/mirror.log"; then
    echo "    fetched $(basename "$mirror")"
  else
    # A mirror that cannot be fetched (corrupt, or the remote moved) is worse
    # than no mirror: SwiftPM would keep resolving against its stale tags.
    echo "    fetch failed, dropping $(basename "$mirror") so SwiftPM re-clones"
    sed 's/^/      /' "$logdir/mirror.log"
    rm -rf "$mirror"
  fi
done
[ "$found_mirror" -eq 1 ] || echo "    no mirror cached yet -- nothing to refresh"

# --- 3. First build: expected to fail ----------------------------------------

step "Build 1/2 -- expected to fail with \"Synthetic project regenerated\""
if xcodebuild -project "$XCODEPROJ" -scheme "$SCHEME" -destination "$DESTINATION" build \
     >"$logdir/build1.log" 2>&1; then
  echo "    Build succeeded (manifests were already current). Continuing."
elif grep -q "Synthetic project regenerated" "$logdir/build1.log"; then
  echo "    Failed as expected -- Kotlin regenerated the manifests. Continuing."
else
  echo
  echo "error: build 1 failed for an unexpected reason:"
  report_failure "$logdir/build1.log"
  exit 1
fi

# --- 4. Re-resolve against the regenerated manifests -------------------------

step "Resolving package dependencies"
xcodebuild -project "$XCODEPROJ" -scheme "$SCHEME" -resolvePackageDependencies \
  >"$logdir/resolve.log" 2>&1 || { report_failure "$logdir/resolve.log"; exit 1; }
grep -E "^  (ios-matter|Pulse|_)" "$logdir/resolve.log" || true

# --- 5. Prune orphaned generated subpackages ---------------------------------

step "Pruning orphaned generated subpackages"
pruned=0
for root in iosApp/KotlinMultiplatformLinkedPackage .swiftpm-locks/*/swiftImport; do
  manifest="$root/Package.swift"
  [ -f "$manifest" ] || continue
  [ -d "$root/subpackages" ] || continue

  # Names the root manifest actually declares, e.g. .package(path: "subpackages/_composeApp")
  declared=$(sed -n 's|.*\.package(path: "subpackages/\([^"]*\)").*|\1|p' "$manifest")
  if [ -z "$declared" ]; then
    # Declaring nothing while subpackages/ exists means the manifest is not the
    # shape this script understands. Deleting every subpackage on that basis
    # would be catastrophic and wrong, so leave the directory alone.
    echo "    warning: $manifest declares no subpackages -- skipping, prune by hand"
    continue
  fi

  for dir in "$root"/subpackages/*/; do
    [ -d "$dir" ] || continue
    name=$(basename "$dir")
    if ! grep -qxF "$name" <<<"$declared"; then
      echo "    orphan: $dir"
      rm -rf "$dir"
      pruned=$((pruned + 1))
    fi
  done
done

if [ "$pruned" -eq 0 ]; then
  echo "    none"
else
  # An orphan created by THIS run (a module rename regenerates under a new
  # name) is already baked into the PIF cache from build 1. Throw it away
  # again, or build 2 asks for a product that no longer exists.
  step "Re-clearing DerivedData after pruning $pruned orphan(s)"
  rm -rf ~/Library/Developer/Xcode/DerivedData/iosApp-*
fi

# --- 6. Second build: must succeed -------------------------------------------

step "Build 2/2 -- must succeed"
if ! xcodebuild -project "$XCODEPROJ" -scheme "$SCHEME" -destination "$DESTINATION" build \
       >"$logdir/build2.log" 2>&1; then
  echo
  echo "error: build 2 failed:"
  report_failure "$logdir/build2.log"
  exit 1
fi
echo "    BUILD SUCCEEDED"

# --- 7. Verify the lockfiles agree -------------------------------------------

step "Verifying SwiftPM lockfiles"
./iosApp/Configuration/check_swiftpm_lockfiles.sh

# --- 8. Tell the user what to commit -----------------------------------------

completed=1

step "ios-matter is pinned at $target and iosApp builds from cold caches"
echo
echo "Commit these together -- splitting them across commits is what produces"
echo "the lockfile drift check_swiftpm_lockfiles.sh detects:"
echo
git status --short -- \
  "$GRADLE_FILE" \
  .swiftpm-locks \
  iosApp/KotlinMultiplatformLinkedPackage \
  "$XCODEPROJ/project.xcworkspace/xcshareddata/swiftpm/Package.resolved" \
  | sed 's/^/  /'
echo
echo "  git add -A $GRADLE_FILE .swiftpm-locks iosApp/KotlinMultiplatformLinkedPackage \\"
echo "        $XCODEPROJ/project.xcworkspace/xcshareddata/swiftpm/Package.resolved"
