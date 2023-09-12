package ai.mender.commands;

import picocli.CommandLine;


@CommandLine.Command(name = "get", mixinStandardHelpOptions = true,
        description = "Display one or many resources",
    subcommands = {GetFunctionsCmd.class, GetReferencesCmd.class, GetCommentsCmd.class, GetExpressions.class})
public class GetCmd {
}
