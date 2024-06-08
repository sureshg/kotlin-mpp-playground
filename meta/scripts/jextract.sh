#!/usr/bin/env bash

set -euo pipefail

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

echo "Using OS: $os"
download_url=$(curl -sSL "https://jdk.java.net/jextract/" | grep -m1 -Eioh "https:.*$os-.*.(tar.gz|zip)")
download_file="${download_url##*/}"

install_dir=${1:-"$HOME/install/openjdk"}
jextract_dir="jextract"

# Download the jextract EA build
pushd "$install_dir" >/dev/null
echo "Downloading $download_url ..."
curl --progress-bar --request GET -L --url "$download_url" --output "$download_file"

# Extract the jextract and cleanup old/downloaded files
rm -rf "$jextract_dir" && mkdir -p "$jextract_dir"
tar xvzf "$download_file" --strip-components=1 -C "$jextract_dir"
rm -f "$download_file"

if [ "$os" == "macos" ]; then
  echo "Removing the quarantine attribute..."
  sudo xattr -r -d com.apple.quarantine "$jextract_dir"
fi

printf "\nOpenJDK jextract version: "
$jextract_dir/bin/jextract --version

popd >/dev/null
