#!/bin/sh

# CHANGE ME
modFolderName="Persean-Chronicles"

version=$(git describe --tags)
zipName=$modFolderName-$version.zip

# Recreate the temp folder if it happens to be present
rm -rf "./$modFolderName-$version"
mkdir "$modFolderName-$version"

# 1. List all files in git, which uses gitignore
# 2. Remove any file matching the blacklist (eg afphoto files)
# 3. Copy to a new folder with the mod name and version
git ls-files | grep -Evf ".circleci/blacklist.txt" | xargs -d '\n' cp --parents -t "$modFolderName-$version"

# Zip the folder, then clean it up
zip -r $zipName "./$modFolderName-$version"
rm -rf "./$modFolderName-$version"

# Move the zip to the artifacts folder
mkdir -p ./artifacts
mv ./$zipName ./artifacts/