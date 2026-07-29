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
#   1. Rewrites version = exact("...") in composeApp/build.gradle.kts -- the
#      only source of truth for the pin.
#   2. Clears DerivedData and .swiftpm-locks/*/swiftPMCheckout, so everything
#      after this point is resolved cold. A bad pin stays invisible while those
#      caches hold the old checkout.
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

set -euo pipefail

cd "$(dirname "$0")/../.."

XCODEPROJ="iosApp/iosApp.xcodeproj"
SCHEME="iOS debug"
# Overridable so CI can build for a concrete simulator or for a device.
DESTINATION="${BUMP_DESTINATION:-generic/platform=iOS Simulator}"
GRADLE_FILE="composeApp/build.gradle.kts"

logdir=$(mktemp -d)
trap 'echo; echo "Logs kept in $logdir"' EXIT

step() { echo; echo "==> $*"; }

# Print whatever explains a failed xcodebuild run. Deliberately not a pipeline
# ending in `head ... || tail ...`: head exits 0 on empty input, so that form
# silently prints nothing when the log has no "error:" lines at all.
report_failure() {
  local log="$1" errors
  errors=$(grep -E "error:" "$log" | sort -u | head -20 || true)
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

# --- 1. Rewrite the pin ------------------------------------------------------

if [ "$target" = "$current" ]; then
  step "ios-matter already pinned at $current -- re-running the clean cycle"
else
  step "Bumping ios-matter $current -> $target in $GRADLE_FILE"
  # Anchored on the whole exact(...) call so a version string appearing
  # elsewhere in the file cannot be hit by accident.
  perl -pi -e "s/version = exact\(\"\Q$current\E\"\)/version = exact(\"$target\")/" "$GRADLE_FILE"
  grep -n 'version = exact(' "$GRADLE_FILE"
fi

# --- 2. Clear the caches that hide bad pins ----------------------------------

step "Clearing DerivedData and SwiftPM checkouts"
rm -rf ~/Library/Developer/Xcode/DerivedData/iosApp-*
rm -rf .swiftpm-locks/*/swiftPMCheckout

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
