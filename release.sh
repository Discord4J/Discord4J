#!/bin/bash
if [ -z "$1" ]
  then
    echo "Version argument missing"
    exit 1
fi
./gradlew -Pversion="$1" -PstoresVersion="$1" -PstoresArtifact="com.discord4j:stores-jdk" clean build -x test publishToMavenLocal bintrayUpload
if [ $? -eq 0 ]
  then
    git tag "$1" && git push --tags
fi
