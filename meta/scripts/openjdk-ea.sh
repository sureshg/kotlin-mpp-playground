#!/usr/bin/env bash

# set -u won't work for sdkman
set -e

jdk_version=${1:-24}

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

echo "Using OS: $os-$arch"
download_url=$(curl -sSL "https://jdk.java.net/$jdk_version" | grep -m1 -Eioh "https:.*($os-$arch).*.(tar.gz|zip)")
openjdk_file="${download_url##*/}"

# Download the OpenJDK EA build
pushd "$HOME/install/openjdk" >/dev/null
echo "Openjdk-$jdk_version file: $openjdk_file"
echo "Downloading $download_url ..."
curl --progress-bar --request GET -L --url "$download_url" --output "$openjdk_file"

# Extract the OpenJDK and cleanup old/downloaded files
jdk_dir=$(tar -tzf "$openjdk_file" | head -3 | tail -1 | cut -f2 -d"/")
rm -rf "$jdk_dir" && tar -xvzf "$openjdk_file" && rm -f "$openjdk_file"
if [ "$os" == "darwin" ]; then
  echo "Removing the quarantine attribute..."
  sudo xattr -r -d com.apple.quarantine "$jdk_dir"
fi

# Install OpenJDK using sdkman
sdkman_id="openjdk-ea"
echo "Installing $jdk_dir ..."
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk rm java "$sdkman_id"
sdk i java "$sdkman_id" "$jdk_dir/Contents/Home"
popd >/dev/null

# Set OpenJDK as default JDK in the current shell
sdk u java "$sdkman_id"
java --version
