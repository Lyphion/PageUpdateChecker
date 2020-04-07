package me.lyphium.pageupdatechecker.command;

import me.lyphium.pageupdatechecker.utils.Command;
import me.lyphium.pageupdatechecker.utils.CommandInfo;

import java.util.Arrays;

@CommandInfo(
        description = "Lists all commands",
        usage = "help [command]",
        aliases = "?"
)
public class HelpCommand extends Command {

    public HelpCommand() {
        super("help");
    }

    @Override
    public boolean onCommand(String label, String[] args) {
        final StringBuilder builder = new StringBuilder("------------- Help Page -------------\n");

        // Check input length: 0 -> General Information; 1 -> Specific Information
        if (args.length == 0) {
            // Build Short Information of each command: usage/name and description
            for (Command command : getCommands()) {
                builder.append("» ");
                if (command.getUsage().isEmpty()) {
                    builder.append(command.getName());
                } else {
                    builder.append(command.getUsage());
                }
                if (!command.getDescription().isEmpty()) {
                    builder.append(" | ").append(command.getDescription());
                }
                builder.append('\n');
            }
        } else if (args.length == 1) {
            // Build Full Information of requestes command

            final String cmdLabel = args[0];
            final Command command = getCommand(cmdLabel);

            // Check if command exists
            if (command == null) {
                builder.append("Command '").append(cmdLabel).append("' not found!\n");
            } else {
                // Create Information: Main-Command, description, usage and aliases
                builder.append("» Command: ").append(command.getName()).append('\n');
                if (!command.getDescription().isEmpty()) {
                    builder.append("» Description: ").append(command.getDescription()).append('\n');
                }
                if (!command.getUsage().isEmpty()) {
                    builder.append("» Usage: ").append(command.getUsage()).append('\n');
                }
                if (command.getAliases().length > 0) {
                    builder.append("» Aliases: ").append(Arrays.toString(command.getAliases())).append('\n');
                }
            }
        } else {
            return false;
        }

        // Print Information
        System.out.print(builder.toString());

        return true;
    }

}