# Untangler
![Tests](https://github.com/craftvscruft/untangler/actions/workflows/ci.yml/badge.svg?branch=main)
[![License: APACHE](https://img.shields.io/github/license/craftvscruft/untangler)](https://github.com/craftvscruft/untangler/blob/main/LICENSE)

> Multi-language refactoring tool implemented in Java, an experimental spin-off
of [MenderBot](https://github.com/craftvscruft/menderbot).

## Commands

* `untangler get functions`: List function definitions in a file
* `untangler get references`: Show symbol and declarations and reference in a file
* `untangler rename`: Rename a symbol and all references to it a file
* `untangler add comment`: Add a comment at a given line
* `untangler remove comment`: Remove a comment at a given line

All commands output text by default but can output machine-readable formats with arguments `-o json` or `-o yaml`.

Arguments to each subcommand can be printed with `untangler <COMMAND> --help`.


## Status

Prototype, not ready for production use.

### Support by language

| Action            | Python | C / C++ | Java | C#   | js / ts |
|-------------------|--------|---------|------|------|---------|
| get functions     | Beta   | Beta    | Beta | Beta | -       |
| get references    | Beta   | Beta    | Beta | Beta | -       |
| rename            | -      | Beta    | -    | -    | -       |
| add comment       | -      | Beta    | -    | -    | -       |
| remove comment    | -      | Beta    | -    | -    | -       |
| extract variable  | -      | -       | -    | -    | -       |
| extract parameter | -      | -       | -    | -    | -       |
| extract field     | -      | -       | -    | -    | -       |

# Requirements (build)

* GraalVM JDK20 ([Homebrew tap](https://github.com/graalvm/homebrew-tap))

We're using GraalVM in preparation to ship a static executable that does not require Java to be installed for the end
user. This doc will assume version 20 but any JDK 16+ should work.

## Running

```
# Your GraalVM JDK 20 instalation
export JAVA_HOME=...

./gradlew installDist

build/install/untangler/bin/untangler
```

For easy rebuilding and running with the `un` shortcut, use:

```
alias un="./gradlew installDist -q && build/install/untangler/bin/untangler"

un get functions -f src/test/resources/hello.c
```

## Testing

Just the unit tests:

```
./gradlew test
```

Everything:

```
./gradlew spotlessApply check
```

Coverage report in `open build/reports/jacoco/test/html/index.html`

## Vision

* Support each of the [core 6 refactorings](https://arlobelshee.com/the-core-6-refactorings) identified by Arlo Belshee
* Support the Menderbot [target languages](https://github.com/craftvscruft/menderbot/issues/5) with reliable syntax tree manipulation
* Operate independent of a language server
* Be automation-friendly,  commands should have an option to output json and indicate success with exit codes, for instance

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

## Building native binary

```
# Your GraalVM JDK 20 instalation
export JAVA_HOME=...

./gradlew nativeCompile

# Run with:

build/native/nativeCompile/untangler
```

During development, you might alias this `nun` for native untangler.
```
alias nun="build/native/nativeCompile/untangler"
```

If you get a reflection error, META-INF may need to be updated.
Check the [Gradle native image](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html#agent-support) docs for more info.
So far what's been needed is for `META-INF/native-image/reflect-config.json` to list all the classes that we will serialize to Json.
Try running GenerateReflectConfig to get new json (this should eventually be a build step).

## Author

👤 **Ray Myers**

* YouTube: [Craft vs Cruft](https://www.youtube.com/channel/UC4nEbAo5xFsOZDk2v0RIGHA)
* Twitter: [@lambdapocalypse](https://twitter.com/lambdapocalypse)
* GitHub: [@raymyers](https://github.com/raymyers)
* LinkedIn: [@cadrlife](https://linkedin.com/in/cadrlife)

## Acknowledgements

* [Antlr](https://www.antlr.org/) - the leading parser generator
* [JavaParser](https://javaparser.org) - analyze and transform Java Code
* [picocli](https://picocli.info/) - a mighty tiny command line interface

## 📝 License

Copyright © 2024 [Ray Myers](https://github.com/raymyers).

This project is [Apache 2](https://www.apache.org/licenses/LICENSE-2.0) licensed.
