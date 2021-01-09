#!/bin/sh

# CHANGE ME
modFolderName="Persean-Chronicles"

version=$(git describe --tags)
zipName=$modFolderName-$version.zip
git archive master -o $zipName --prefix $modFolderName-$version/
mkdir -p ./artifacts
mv ./$zipName ./artifacts/