#!/bin/bash


# Publish Maven release
./gradlew publish --no-daemon --no-parallel

# Close Maven release
./gradlew closeAndReleaseRepository --no-daemon --no-parallel

# Publish to Gradle Plugin Portal
./gradlew publishPlugins

# Create new website docs version
./gradlew versionDocs

# Set all versions in the root README to the new version
./gradlew updateProjectReadmeVersionRefs

echo
echo ' ___ _   _  ___ ___ ___  ___ ___'
echo '/ __| | | |/ __/ __/ _ \/ __/ __|'
echo '\__ \ |_| | (_| (_|  __/\__ \__ \'
echo '|___/\__,_|\___\___\___||___/___/'
echo
echo
echo The release is done and a new docs version has been created for Docusaurus.
echo
echo These changes need to get merged into main and published.
echo
echo
