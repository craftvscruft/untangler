# Add a new language

The folder structure of `vendor/` is important to this process. The build scripts expect:

* Grammars in `vendor/<Language>/`
* Target customization in `vendor/<Language>/<Target>/` (Target == Java)
* Grammars named:
  * `vendor/<Language>/<Language>Lexer.g4`
  * `vendor/<Language>/<Language>Parser.g4`

For instance, `vendor/TypeScript/TypeScriptParser.g4`, the casing is the same.

## Copy grammar files.

Find a directory with the grammar files you want in grammars-v4 - typically they come from there.
Let's call that directory LANG_DIR.
```sh
export LANGUAGE="FooLang"
export LANG_FROM_DIR="grammars-v4/foolang"

mkdir -p "vendor/$LANGUAGE/Java/"
copy "LANG_FROM_DIR"/*.g4 "LANG_FROM_DIR"/README.md "vendor/$LANGUAGE/"
copy "LANG_FROM_DIR"/Java/*.java "vendor/$LANGUAGE/Java/"
```

## Modify grammar files

If comments are skipped in the Lexer, send them to HIDDEN channel instead, like:

```
// Modified for Untangler, making comments go to hidden not skipped
BlockComment: '/*' .*? '*/' -> channel (HIDDEN);
LineComment: '//' ~ [\r\n]* -> channel (HIDDEN);
```

## Generate parser code

Add a new dependency to generateAllParsers at the end of `build.gradle`. 
This will pick up new directory, which is why naming is important.

```groovy
tasks.register('generateAllParsers') {
    //...
    dependsOn 'generateParserFooLang'
}
```

Next generate the parsers and confirm the project still builds.
```sh
./gradlew generateAllParsers

./gradlew check
```

## Add language support
Now comes the hard part.

In `TestGetFunctionsCmd.java`, add a source code example. The ultimate goal will be getting it to 
output what you expect.

Most language-specific details should be in the `strategy` package, you might start by 
subclassing LanguageServer in your own sub-package.

## Update Docs

Anywhere we list the supported languages or licenses should mention the newly added language.