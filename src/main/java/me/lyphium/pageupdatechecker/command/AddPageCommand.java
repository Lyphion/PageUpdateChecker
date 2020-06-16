package me.lyphium.pageupdatechecker.command;

import me.lyphium.pageupdatechecker.Bot;
import me.lyphium.pageupdatechecker.checker.PageUpdate;
import me.lyphium.pageupdatechecker.database.DatabaseConnection;
import me.lyphium.pageupdatechecker.utils.Command;
import me.lyphium.pageupdatechecker.utils.CommandInfo;

import java.net.URL;
import java.util.Collections;

@CommandInfo(
        description = "Adds a new page to the database",
        shortUsage = "addpage <name> <url>",
        usage = "addpage <name> <url> [mail]",
        aliases = "add"
)
public class AddPageCommand extends Command {

    public AddPageCommand() {
        super("addpage");
    }

    @Override
    public boolean onCommand(String label, String[] args) {
        if (args.length < 2 || args.length > 3) {
            return false;
        }

        final Bot bot = Bot.getInstance();
        final DatabaseConnection database = bot.getDatabase();

        // Checking if the connection to the database is available, otherwise can't add page
        if (!database.isConnected()) {
            System.err.println("No connection available");
            return true;
        }

        final String name = args[0];
        final String url = args[1];

        // Check if url is valid
        try {
            new URL(url).toURI();
        } catch (Exception e) {
            System.err.println("Not a valid url!");
            return true;
        }

        final String mail;
        if (args.length == 3) {
            if (args[2].matches("^[\\w!#$%&’*+/=?`{|}~^.-]+@[\\w.-]+$")) {
                mail = args[2];
            } else {
                System.err.println("Not a valid mail!");
                return true;
            }
        } else {
            mail = null;
        }

        // Create page based on input
        PageUpdate page = new PageUpdate(-1, name, url, mail);

        // Add page to database, success will be true, if the pages was added
        final boolean success = database.addPage(page);

        // Check if page was added
        if (success) {
            final String doc = bot.getChecker().loadPage(url);
            if (doc == null) {
                System.err.println("Page can't be accessed. Please check the page and retry");
                database.removePage(page);
                return true;
            }

            page = database.getPageByName(name);
            page.setLastUpdate(System.currentTimeMillis());
            page.setContent(doc);

            database.savePages(Collections.singletonList(page));

            System.out.println("New page added to database");
        } else {
            System.err.println("Page couldn't added to database");
        }

        return true;
    }

}