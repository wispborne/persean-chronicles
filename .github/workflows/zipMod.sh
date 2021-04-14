#!/bin/sh

# USAGE
$ Run from within the 
# ./zipMod.sh "folderToZip" "outputFolderName"
#   arg folderToZip: The folder containing mod_info.json.
#     ex: "D:/Code/Persean-Chronicles"
#   arg outputFolderName: Do not append the version, it will be appended.
#     ex: "Persean-Chronicles"

folderToZip=$1
outputFolderName=$2
startingDir=$(pwd)

realpath $1

cd $folderToZip
version=$(git describe --tags)
zipName=$outputFolderName-$version.zip

# Recreate the temp folder if it happens to be present
rm -rf "./$outputFolderName-$version"
mkdir "$outputFolderName-$version"

# 1. List all files in git, which uses gitignore
# 2. Remove any file matching the blacklist (eg afphoto files)
# 3. Copy to a new folder with the mod name and version
git ls-files | grep -Evf "$startingDir/blacklist.txt" | while read file; do cp --parents "$file" "$outputFolderName-$version"; done

# Zip the folder, then clean it up
zip -r $zipName "./$outputFolderName-$version"
rm -rf "./$outputFolderName-$version"

# Move the zip to the artifacts folder
mkdir -p ./artifacts
mv ./$zipName ./artifacts/
