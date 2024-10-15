#!/usr/bin/env bash

set -e

# Find OS type
case "$OSTYPE" in
darwin*)
  os=macos
  ;;
msys*)
  os=windows
  ;;
linux*)
  os=linux
  ;;
*)
  echo "Unsupported OS: $OSTYPE"
  exit 1
  ;;
esac

# Find CPU architecture
case "$(uname -m)" in
amd64 | x86_64)
  arch=x64
  ;;
aarch64 | arm64)
  arch=aarch64
  ;;
*)
  echo "Unsupported arch: $(uname -m)"
  exit 1
  ;;
esac

PACKAGE_NAME="dev.suresh.${os}.${arch}"
echo "PACKAGE_NAME=${PACKAGE_NAME}" >> $GITHUB_ENV