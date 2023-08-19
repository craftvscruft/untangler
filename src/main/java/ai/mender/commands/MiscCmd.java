package ai.mender.commands;

import picocli.CommandLine;


@CommandLine.Command(name = "misc", mixinStandardHelpOptions = true,
        description = "Some less common use cases. List with 'misc --help'",
    subcommands = {SplitDeclarationsCmd.class})
public class MiscCmd {
}
