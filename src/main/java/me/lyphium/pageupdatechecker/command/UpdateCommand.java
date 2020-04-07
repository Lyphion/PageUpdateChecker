package me.lyphium.pageupdatechecker.command;

import me.lyphium.pageupdatechecker.Bot;
import me.lyphium.pageupdatechecker.checker.PageUpdate;
import me.lyphium.pageupdatechecker.utils.Command;
import me.lyphium.pageupdatechecker.utils.CommandInfo;
import me.lyphium.pageupdatechecker.utils.MailUtils;
import me.lyphium.pageupdatechecker.utils.Pair;

import java.util.List;
import java.util.stream.Collectors;

@CommandInfo(
        description = "Updates the prices now",
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

        final Bot bot = Bot.getInstance();

        // Checking if the connection to the database is available, otherwise can't update
        if (!bot.getDatabase().isConnected()) {
            System.err.println("No connection available");
            return true;
        }

        long time = System.currentTimeMillis();
        System.out.println("Updating Prices...");

        // Check for page update
        final List<PageUpdate> pages = bot.getChecker().update();
        if (pages != null && pages.size() > 0) {
            System.out.println(pages.stream().map(PageUpdate::getName).collect(Collectors.joining(", ", "Updates: ", "")));

            // Creating mails
            final List<Pair<String, String>> list = MailUtils.createMailContent(pages);
            final boolean success = true; //MailUtils.sendUpateMail(list);

            // Printing if mailing was successful
            if (success) {
                System.out.println("Sendet " + list.size() + " mails");
            } else {
                System.err.println("It looks like at least one mail had a problem will sending");
            }
        }

        time = System.currentTimeMillis() - time;
        System.out.println("Finished: Updated the Prices (" + time + "ms)");

        return true;
    }

}