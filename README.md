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

## Testing

Just the unit tests:

./gradlew test

Everything:

./gradlew spotlessApply check

Coverage report in `open build/reports/jacoco/test/html/index.html`

## Vision

* Support each of the [core 6 refactorings](https://arlobelshee.com/the-core-6-refactorings) identified by Arlo Belshee
* Support the Menderbot [target languages](https://github.com/craftvscruft/menderbot/issues/5) with reliable syntax tree manipulation
* Operate independent of a language server
* Be automation-friendly - commands should have an option to output json, for instance

### Core 6 refactorings

* Rename
* Inline
* Extract Method
* Introduce Local Variable
* Introduce Parameter
* Introduce Field

### Target languages

1. Python
2. C / C++
3. Java
4. C#
5. JavaScript / TypeScript
