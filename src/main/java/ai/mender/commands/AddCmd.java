package ai.mender.commands;

import picocli.CommandLine;


@CommandLine.Command(name = "add", mixinStandardHelpOptions = true,
        description = "Add resources",
    subcommands = {AddCommentCmd.class})
public class AddCmd {
}
