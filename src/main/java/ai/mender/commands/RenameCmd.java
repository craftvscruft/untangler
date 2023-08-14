package ai.mender.commands;

import picocli.CommandLine;


@CommandLine.Command(name = "rename", mixinStandardHelpOptions = true,
        description = "Rename a resource",
        subcommands = {RenameFunctionCmd.class},
        aliases = "ren")
public class RenameCmd {


}
