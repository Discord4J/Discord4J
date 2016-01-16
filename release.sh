#!/usr/bin/env bash
export JAVA_HOME="`/usr/libexec/java_home -v '1.8*'`"
mvn deploy -Dmaven.test.skip=true
sh updateJavadocs.sh
