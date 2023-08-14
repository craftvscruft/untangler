package ai.mender;
import ai.mender.commands.GetCmd;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "untangler", mixinStandardHelpOptions = true, version = "0.0.1-SNAPSHOT",
        description = "Hello", subcommands = {GetCmd.class})
public class Main {
//    @Parameters(index = "0", description = "The file whose checksum to calculate.")
//    private File file;
//
//    @Option(names = {"-a", "--algorithm"}, description = "MD5, SHA-1, SHA-256, ...")
//    private String algorithm = "SHA-256";

    public static void main(String... args) {
        CommandLine commandLine = new CommandLine(new Main());
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        System.exit(commandLine.execute(args));
    }
}