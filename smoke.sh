#!/usr/bin/env sh
set -euo pipefail

echo Checking Java version in JAVA_HOME

$JAVA_HOME/bin/java -version

echo "^ If that's not a GraalVM, Gradle will attempt to locate one."

./gradlew nativeCompile

nativeBin="build/native/nativeCompile/untangler"

$nativeBin get functions -f src/test/resources/hello.c

$nativeBin get functions -f src/main/java/ai/mender/parsing/CharsetUtils.java
