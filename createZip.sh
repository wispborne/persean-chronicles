#!/bin/bash

read -p "Enter version: " version

git archive master -o Stories-$version.zip --prefix Stories-$version/