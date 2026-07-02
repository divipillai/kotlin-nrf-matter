#!/bin/sh
set -e

TAG=$(git -C "$SRCROOT" describe --tags --abbrev=0 2>/dev/null)
if [ -z "$TAG" ]; then
  echo "warning: no git tag found; keeping existing app version"
  exit 0
fi

BUILD_NUMBER=$(git -C "$SRCROOT" rev-list --count HEAD)
INFOPLIST="${TARGET_BUILD_DIR}/${INFOPLIST_PATH}"

/usr/libexec/PlistBuddy -c "Set :CFBundleShortVersionString $TAG" "$INFOPLIST"
/usr/libexec/PlistBuddy -c "Set :CFBundleVersion $BUILD_NUMBER" "$INFOPLIST"

echo "Set app version to $TAG ($BUILD_NUMBER) from git tag"
