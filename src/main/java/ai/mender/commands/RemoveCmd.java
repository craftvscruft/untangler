package ai.mender.commands;

import picocli.CommandLine;


@CommandLine.Command(name = "remove", mixinStandardHelpOptions = true,
        description = "Remove resources",
    aliases = "rm",
    subcommands = {RemoveCommentsCmd.class})
public class RemoveCmd {
}
