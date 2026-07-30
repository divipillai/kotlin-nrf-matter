#!/bin/bash
# Fails if the SwiftPM lockfiles disagree about which package revisions to use.
#
# Kotlin's swiftPMDependencies keeps its own persisted lockfile under
# .swiftpm-locks/<id>/swiftImport/, while Xcode resolves the package graph
# independently into iosApp.xcodeproj/.../xcshareddata/swiftpm/. Nothing syncs
# the two. When they drift, builds keep working off cached checkouts and only
# break once someone clears DerivedData -- long after the bad commit landed.
#
# Run after bumping a swiftPackage version, and in CI.
#
# BUMPING A swiftPackage VERSION
#
# Don't do it by hand. Run:
#
#   ./iosApp/Configuration/bump_ios_matter_version.sh <version>
#
# which rewrites the pin, purges the caches that hide a bad pin, absorbs the
# build that fails by design, re-resolves, prunes orphaned generated
# subpackages, rebuilds, and calls this script. Its header documents each step
# and why it is needed.
#
# Whatever the bump touched -- build.gradle.kts, BOTH Package.resolved files,
# the regenerated Package.swift files -- goes in ONE commit. Splitting them
# across commits is what produces the drift this script detects.

set -euo pipefail

cd "$(dirname "$0")/../.."

status=0

# --- 1. Do the two Package.resolved files pin the same revisions? ------------

xcode_lock="iosApp/iosApp.xcodeproj/project.xcworkspace/xcshareddata/swiftpm/Package.resolved"
gradle_lock=""
for candidate in .swiftpm-locks/*/swiftImport/Package.resolved; do
  [ -f "$candidate" ] && gradle_lock="$candidate" && break
done

if [ -z "$gradle_lock" ]; then
  echo "error: no Gradle SwiftPM lockfile found under .swiftpm-locks/*/swiftImport/"
  echo "       run ./gradlew :composeApp:syncSyntheticPackageResolvedToPersisted"
  exit 1
fi
if [ ! -f "$xcode_lock" ]; then
  echo "error: Xcode lockfile missing: $xcode_lock"
  echo "       run xcodebuild -project iosApp/iosApp.xcodeproj -scheme 'iOS debug' -resolvePackageDependencies"
  exit 1
fi

echo "Comparing SwiftPM pins"
echo "  gradle: $gradle_lock"
echo "  xcode:  $xcode_lock"
echo

python3 - "$gradle_lock" "$xcode_lock" <<'PY' || status=1
import json, sys

def pins(path):
    with open(path) as fh:
        data = json.load(fh)
    # Package.resolved v1 nests under "object"; v2/v3 are flat.
    entries = data.get("pins") or data.get("object", {}).get("pins", [])
    out = {}
    for pin in entries:
        identity = pin.get("identity") or pin.get("package", "")
        state = pin.get("state", {})
        out[identity.lower()] = (state.get("revision"), state.get("version") or state.get("branch"))
    return out

gradle_path, xcode_path = sys.argv[1], sys.argv[2]
gradle, xcode = pins(gradle_path), pins(xcode_path)

drift = False
for identity in sorted(set(gradle) | set(xcode)):
    g, x = gradle.get(identity), xcode.get(identity)
    if g is None:
        print(f"  DRIFT  {identity}: absent from gradle lockfile, xcode has {x[1]}")
        drift = True
    elif x is None:
        print(f"  DRIFT  {identity}: absent from xcode lockfile, gradle has {g[1]}")
        drift = True
    elif g[0] != x[0]:
        print(f"  DRIFT  {identity}: gradle {g[1]} ({(g[0] or '?')[:12]}) != xcode {x[1]} ({(x[0] or '?')[:12]})")
        drift = True
    else:
        print(f"  ok     {identity}: {g[1]} ({(g[0] or '?')[:12]})")

if drift:
    print("\nThe lockfiles disagree. To realign:")
    print("  ./gradlew :composeApp:syncSyntheticPackageResolvedToPersisted")
    print("  xcodebuild -project iosApp/iosApp.xcodeproj -scheme 'iOS debug' -resolvePackageDependencies")
    print("then re-run this script and commit both lockfiles together.")
    sys.exit(1)
PY

# --- 2. Is there a generated Package.swift outside the canonical directory? ---
#
# Gradle regenerates the linked-package manifests on every build. Stale copies
# at old paths stay tracked in git, pinned to whatever version was current when
# they were generated, and silently mislead readers.
#
# Matching on the path alone is not enough: Gradle also writes a full copy of
# the scaffolding to .swiftpm-locks/*/swiftImport/, whose directory is not named
# after the package, so a '*KotlinMultiplatformLinkedPackage/Package.swift' glob
# walks straight past it. That copy went unnoticed long enough to drift -- it
# declared a `_shared` subpackage the Xcode copy no longer has. So identify
# generated manifests by content: either they declare the linked package itself,
# or they are one of its subpackages pinning a swiftPackage dependency.

canonical_dir="iosApp/KotlinMultiplatformLinkedPackage"

echo
echo "Checking generated KotlinMultiplatformLinkedPackage manifests"

generated=""
while IFS= read -r manifest; do
  [ -n "$manifest" ] || continue
  if grep -qE 'name: "(KotlinMultiplatformLinkedPackage|_[A-Za-z])' "$manifest"; then
    generated="$generated$manifest"$'\n'
  fi
done <<EOF
$(git ls-files '*Package.swift')
EOF

generated=$(printf '%s' "$generated" | sed '/^$/d')

if [ -z "$generated" ]; then
  echo "  none tracked"
else
  strays=""
  while IFS= read -r manifest; do
    case "$manifest" in
      "$canonical_dir"/*) echo "  ok     $manifest" ;;
      *) echo "  STRAY  $manifest"; strays="$strays  $manifest"$'\n' ;;
    esac
  done <<EOF
$generated
EOF

  if [ -n "$strays" ]; then
    echo
    echo "  error: tracked generated manifests outside $canonical_dir/."
    echo "         Gradle only regenerates the copy Xcode builds; the others are"
    echo "         stale orphans that will disagree after the next version bump."
    echo "         Untrack them (git rm --cached) and leave them gitignored:"
    printf '%s' "$strays"
    status=1
  fi
fi

echo
if [ "$status" -eq 0 ]; then
  echo "SwiftPM lockfiles are consistent."
else
  echo "SwiftPM lockfile check FAILED."
fi
exit "$status"
