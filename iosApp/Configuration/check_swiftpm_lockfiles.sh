#!/bin/bash
# Fails if the SwiftPM lockfiles disagree about which package revisions to use.
#
# Kotlin's swiftPMDependencies keeps its own persisted lockfile under
# .swiftpm-locks/<id>/swiftImport/, while Xcode resolves the package graph
# independently into iosApp.xcodeproj/.../xcshareddata/swiftpm/. Nothing syncs
# the two. When they drift, builds keep working off cached checkouts and only
# break once someone clears DerivedData -- long after the bad commit landed.
#
# Run after changing anything in the SwiftPM graph, and in CI.
#
# ios-matter ITSELF IS NO LONGER PINNED
#
# It is vendored at /ios-matter and declared with `localSwiftPackage`, so it has
# no revision to drift and does not appear in either lockfile. Only its own
# remote dependencies do -- today just Pulse. Editing the vendored sources
# therefore needs none of this: build and go.
#
# CHANGING THE SHAPE OF THE PACKAGE GRAPH
#
# Adding or removing a swiftPackage declaration, or a dependency in
# /ios-matter/Package.swift, still makes Kotlin rewrite the generated manifests
# under iosApp/KotlinMultiplatformLinkedPackage/. Three things follow from that,
# all of which used to be automated by a bump script that no longer has a
# version to bump:
#
#   1. The first Xcode build after such a change FAILS BY DESIGN, with
#      "Synthetic project regenerated" -- Kotlin rewrote the manifest mid-build
#      and will not continue against a manifest it just wrote. Only an
#      Xcode-invoked build regenerates that copy, so this failure cannot be
#      avoided from the CLI. Re-resolve and build again:
#
#        xcodebuild -project iosApp/iosApp.xcodeproj -scheme 'iOS debug' \
#          -resolvePackageDependencies
#        xcodebuild -project iosApp/iosApp.xcodeproj -scheme 'iOS debug' \
#          -destination 'generic/platform=iOS Simulator' build
#
#   2. Renaming or re-scoping a Kotlin module leaves an ORPHANED subpackage
#      under iosApp/KotlinMultiplatformLinkedPackage/subpackages/ -- still
#      tracked in git, no longer declared by the sibling root Package.swift.
#      Xcode's PIF cache in DerivedData keeps serving it and the build dies at
#      CreateBuildDescription with
#
#        error: Missing package product '<orphan name>' (in target 'iosApp')
#
#      which nothing in project.pbxproj explains. Delete any subpackage
#      directory the root Package.swift does not declare, then clear
#      ~/Library/Developer/Xcode/DerivedData/iosApp-* so the PIF cache goes
#      with it. Step 2 of this check flags tracked strays.
#
#   3. Everything the change touched -- build.gradle.kts, BOTH Package.resolved
#      files, the regenerated Package.swift files -- goes in ONE commit.
#      Splitting them across commits is what produces the drift this script
#      detects.

set -euo pipefail

cd "$(dirname "$0")/../.."

status=0

# --- 1. Do the two Package.resolved files pin the same revisions? ------------

xcode_lock="iosApp/iosApp.xcodeproj/project.xcworkspace/xcshareddata/swiftpm/Package.resolved"

# Named, not "first match of .swiftpm-locks/*/swiftImport/". There are two lock
# directories now: `default` for the vendored ios-matter that app builds use, and
# `release` written by -PiosMatter.useVendored=false, which legitimately pins
# ios-matter to a tag. Comparing `release` against Xcode's lockfile would report
# that extra pin as drift, and which one a glob picks up is nobody's intent.
gradle_lock=".swiftpm-locks/default/swiftImport/Package.resolved"
release_lock=".swiftpm-locks/release/swiftImport/Package.resolved"

if [ ! -f "$gradle_lock" ]; then
  echo "error: Gradle SwiftPM lockfile missing: $gradle_lock"
  # NOT syncSyntheticPackageResolvedToPersisted: as of Kotlin 2.4.10 that task
  # fails standalone with "property 'destinationFile' doesn't have a configured
  # value". The cinterop task writes the same lockfile as a side effect.
  echo "       run ./gradlew :composeApp:cinteropSwiftPMImportIosSimulatorArm64"
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
    print("  ./gradlew :composeApp:cinteropSwiftPMImportIosSimulatorArm64")
    print("  xcodebuild -project iosApp/iosApp.xcodeproj -scheme 'iOS debug' -resolvePackageDependencies")
    print("then re-run this script and commit both lockfiles together.")
    print("\nIf 'ios-matter' shows up here at all, something built with")
    print("-PiosMatter.useVendored=false and wrote to the wrong lock directory:")
    print("the vendored package has no revision to pin.")
    sys.exit(1)
PY

# --- 1b. The release lockfile, if one has been produced ----------------------
#
# Reported, never compared: it pins ios-matter to a tag on purpose. It only has
# to agree with iosMatter.version in gradle.properties, which is what a stale
# release lock silently gets wrong.

if [ -f "$release_lock" ]; then
  echo
  echo "Release lockfile (-PiosMatter.useVendored=false)"
  echo "  $release_lock"
  pinned_version=$(python3 -c "
import json, sys
pins = json.load(open(sys.argv[1])).get('pins', [])
print(next((p['state'].get('version', '?') for p in pins
            if p.get('identity', '').lower() == 'ios-matter'), ''))
" "$release_lock")
  declared_version=$(sed -n 's/^iosMatter\.version=//p' gradle.properties)

  if [ -z "$pinned_version" ]; then
    echo "  warning: no ios-matter pin -- the release lock looks incomplete"
  elif [ "$pinned_version" = "$declared_version" ]; then
    echo "  ok     ios-matter: $pinned_version (matches iosMatter.version)"
  else
    echo "  DRIFT  ios-matter: locked $pinned_version, gradle.properties says $declared_version"
    echo "         re-resolve with: ./gradlew :composeApp:cinteropSwiftPMImportIosSimulatorArm64 \\"
    echo "                            -PiosMatter.useVendored=false"
    status=1
  fi
fi

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
