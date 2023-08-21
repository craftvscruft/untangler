package ai.mender;

import ai.mender.commands.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "untangler", mixinStandardHelpOptions = true, version = "0.0.1-SNAPSHOT",
        description = "Hello",
        subcommands = {
                GetCmd.class,
                RenameCmd.class,
                RemoveCmd.class,
                AddCmd.class,
                MiscCmd.class
        })
public class Main {

    public static void main(String... args) {
        CommandLine commandLine = new CommandLine(new Main());
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        System.exit(commandLine.execute(args));
    }
}