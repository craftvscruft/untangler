#!/usr/bin/env sh
set -euo pipefail

nativeBin="build/native/nativeCompile/untangler"

$nativeBin get functions -f src/test/resources/hello.c