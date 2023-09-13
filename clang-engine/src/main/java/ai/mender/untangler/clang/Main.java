package ai.mender.untangler.clang;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        System.out.println("Running...");
        ClangRunner.runClang("/Users/rmyers/dev/ioccc/endoh2/main.c");
    }
}
