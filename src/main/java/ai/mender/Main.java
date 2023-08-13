package ai.mender;
import ai.mender.commands.GetCmd;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Callable;

@Command(name = "untangler", mixinStandardHelpOptions = true, version = "0.0.1-SNAPSHOT",
        description = "Hello", subcommands = {GetCmd.class})
public class Main {
//    @Parameters(index = "0", description = "The file whose checksum to calculate.")
//    private File file;
//
//    @Option(names = {"-a", "--algorithm"}, description = "MD5, SHA-1, SHA-256, ...")
//    private String algorithm = "SHA-256";

    public static void main(String... args) throws IOException {
        System.exit(new CommandLine(new Main()).execute(args));
    }
}