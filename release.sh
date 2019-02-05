#!/bin/bash
if [[ -z "$1" ]]
  then
    echo "Version argument missing"
    exit 1
fi
VERSION=$1
if [[ -z "$2" ]]
  then
    STORES_VERSION=$1
else
  STORES_VERSION=$2
fi
echo "Releasing version $VERSION depending on Stores $STORES_VERSION"
./gradlew build -x test release -Pversion="$VERSION" -PstoresVersion="$STORES_VERSION" -PstoresArtifact="com.discord4j:stores-jdk"
