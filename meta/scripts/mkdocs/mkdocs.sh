#!/bin/bash

set -eu

# Make sure mkdocs plugin is already installed.
# pip3 install mkdocs-macros-plugin
# pip3 install mkdocs-minify-plugin

project_dir="$(dirname "$(realpath "$0")")"/../..
project_name=kotlin-mpp-playground
build_dir="build/dokka"

pushd "$project_dir" >/dev/null

echo "Building the Dokka documentation..."
./gradlew dokkaGfm

cp docs/mkdocs/mkdocs.yml ${build_dir}
cp ./*.md "${build_dir}/${project_name}"
cp docs/*.md "${build_dir}/${project_name}"
cp -R docs/logo "${build_dir}/${project_name}"
mv ${build_dir}/${project_name}/{README.md,Overview.md}
sed -i "" 's/"docs\/logo/"..\/logo/g' "${build_dir}/${project_name}/Overview.md"

pushd ${build_dir} >/dev/null
mkdocs build
popd >/dev/null

echo "Copying API doc"
rm -rf docs/apidoc
cp -R "${build_dir}/docs" docs/apidoc

echo "Committing changes"
git add docs/apidoc
git commit -m "doc: site update" -- docs/apidoc || echo "Nothing to commit"

popd >/dev/null
