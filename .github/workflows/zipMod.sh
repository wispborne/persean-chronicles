#!/bin/sh

# USAGE
# ./zipMod.sh "mod-folder-name"
#   arg mod-folder-name: Do not append the version, it will be appended.
#     ex: "Persean-Chronicles"

modFolderName="$1"

version=$(git describe --tags)
zipName=$modFolderName-$version.zip

# Recreate the temp folder if it happens to be present
rm -rf "./$modFolderName-$version"
mkdir "$modFolderName-$version"

# 1. List all files in git, which uses gitignore
# 2. Remove any file matching the blacklist (eg afphoto files)
# 3. Copy to a new folder with the mod name and version
git ls-files | grep -Evf ".circleci/blacklist.txt" | while read file; do cp --parents "$file" "$modFolderName-$version"; done

# Zip the folder, then clean it up
zip -r $zipName "./$modFolderName-$version"
rm -rf "./$modFolderName-$version"

# Move the zip to the artifacts folder
mkdir -p ./artifacts
mv ./$zipName ./artifacts/
