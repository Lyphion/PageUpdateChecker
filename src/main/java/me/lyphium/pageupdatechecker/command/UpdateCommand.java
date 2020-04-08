package me.lyphium.pageupdatechecker.command;

import me.lyphium.pageupdatechecker.Bot;
import me.lyphium.pageupdatechecker.utils.Command;
import me.lyphium.pageupdatechecker.utils.CommandInfo;

@CommandInfo(
        description = "Checks pages for updates now",
        usage = "update"
)
public class UpdateCommand extends Command {

    public UpdateCommand() {
        super("update");
    }

    @Override
    public boolean onCommand(String label, String[] args) {
        if (args.length > 0) {
            return false;
        }

        final long time = Bot.getInstance().getChecker().handleUpdate();

        if (time > -1) {
            System.out.println("Finished: Check complete (" + time + "ms)");
        }

        return true;
    }

}