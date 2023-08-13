# Untangler

Multi-language refactoring tool implemented in Java, an experimental spin-off
of [MenderBot](https://github.com/craftvscruft/menderbot).

# Requirements (Build)

* GraalVM JDK20 ([Homebrew tap](https://github.com/graalvm/homebrew-tap))

We're using GraalVM in preparation to ship a static executable that does not require Java to be installed for the end
user.

## Running

export JAVA_HOME=...
./gradlew installDist
build/install/untangler/bin/untangler

alias un="./gradlew installDist -q && build/install/untangler/bin/untangler"

un get functions build.gradle

## Running native binary

export JAVA_HOME=...
./gradlew nativeCompile

...
