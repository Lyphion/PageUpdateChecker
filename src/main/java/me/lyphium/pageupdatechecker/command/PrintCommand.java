package me.lyphium.pageupdatechecker.command;

import me.lyphium.pageupdatechecker.Bot;
import me.lyphium.pageupdatechecker.checker.PageUpdate;
import me.lyphium.pageupdatechecker.database.DatabaseConnection;
import me.lyphium.pageupdatechecker.utils.Command;
import me.lyphium.pageupdatechecker.utils.CommandInfo;

import java.util.List;

@CommandInfo(
        description = "Print information from the database",
        usage = "print pages [pattern]"
)
public class PrintCommand extends Command {

    public PrintCommand() {
        super("print");
    }

    @Override
    public boolean onCommand(String label, String[] args) {
        if (args.length < 1 || args.length > 2) {
            return false;
        }

        final DatabaseConnection database = Bot.getInstance().getDatabase();

        // Checking if the connection to the database is available, otherwise can't print Informations
        if (!database.isConnected()) {
            System.err.println("No connection available");
            return true;
        }

        if (args[0].equalsIgnoreCase("pages")) {
            if (args.length > 2) {
                return false;
            }

            // Getting all pages from database
            final List<PageUpdate> pages = database.getPages();

            // Filtering all pages with don't match the optimal pattern
            if (args.length == 2) {
                final String pattern = args[1];
                pages.removeIf(s -> !s.getName().matches(pattern));
            }

            // Check if there are still some pages
            if (pages.isEmpty()) {
                System.out.println("No pages found");
                return true;
            }

            /*
             *  ID:   -id-
             *  Name: -name-
             *  URL:  -url-
             *  Mail: -mail-
             *
             *  ID:   -id-
             *  Name: -name-
             *  ...
             */

            final StringBuilder builder = new StringBuilder("---------- Available Pages ----------");

            // Creating the Information section for each page
            for (PageUpdate page : pages) {
                builder.append('\n');
                builder.append(String.format("ID:   %d\n", page.getId()));
                builder.append(String.format("Name: %s\n", page.getName()));
                builder.append(String.format("URL:  %s\n", page.getUrl()));
                builder.append(String.format("Mail: %s\n", page.getMail()));
            }

            // Printing Information page
            System.out.print(builder.toString());
        } else {
            return false;
        }

        System.gc();
        return true;
    }

}
