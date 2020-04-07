package me.lyphium.pageupdatechecker.command;

import me.lyphium.pageupdatechecker.Bot;
import me.lyphium.pageupdatechecker.checker.PageUpdate;
import me.lyphium.pageupdatechecker.database.DatabaseConnection;
import me.lyphium.pageupdatechecker.utils.Command;
import me.lyphium.pageupdatechecker.utils.CommandInfo;

@CommandInfo(
        description = "Removes a page and data from the database",
        usage = "removepage <id/name> [value]",
        aliases = "remove"
)
public class RemovePageCommand extends Command {

    private long removeTime = 0;
    private PageUpdate toBeRemoved = null;

    public RemovePageCommand() {
        super("removepage");
    }

    @Override
    public boolean onCommand(String label, String[] args) {
        if (args.length == 0 || args.length == 1 && !args[0].equalsIgnoreCase("confirm") || args.length > 2) {
            return false;
        }

        final DatabaseConnection database = Bot.getInstance().getDatabase();

        // Checking if the connection to the database is available, otherwise can't remove page
        if (!database.isConnected()) {
            System.err.println("No connection available");
            return true;
        }

        // Parse input, check if first argument is a number
        if (args[0].equalsIgnoreCase("id")) {
            final int id = Integer.parseUnsignedInt(args[1]);

            // Create Temp-PriceData
            final PageUpdate page = database.getPageByID(id);

            // Check if page exists
            if (page == null) {
                System.err.println("No data found with id: " + id);
                return true;
            }

            // Create Delete Confirm
            System.out.println("Do you really want to delete '" + page.getName() + "'? Confirm with: 'removepage confirm'");

            toBeRemoved = page;
            removeTime = System.currentTimeMillis();
        } else if (args[0].equalsIgnoreCase("name")) {
            final String name = args[1];

            // Create Temp-PriceData with name
            final PageUpdate data = database.getPageByName(name);

            // Check if page exists
            if (data == null) {
                System.err.println("No data found with name: " + name);
                return true;
            }

            // Create Delete Confirm
            System.out.println("Do you really want to delete '" + data.getName() + "'? Confirm with: 'removepage confirm'");

            toBeRemoved = data;
            removeTime = System.currentTimeMillis();
        } else if (args[0].equalsIgnoreCase("confirm")) {
            // Check if samething should be removed
            if (toBeRemoved == null) {
                System.err.println("Nothing to remove");
                return true;
            }

            // Check if Confirm Time is smaller than 10sec
            if (removeTime + 15 * 1000 < System.currentTimeMillis()) {
                System.err.println("Confirm time expired");
                toBeRemoved = null;
                return true;
            }

            // Remove page and data to database, success will be true, if the pages was removed
            final boolean success = database.removePage(toBeRemoved);
            toBeRemoved = null;

            // Check if page and data was removed
            if (success) {
                System.out.println("Page and data removed from database");
            } else {
                System.err.println("Page couldn't removed from database");
            }
        } else {
            return false;
        }

        return true;
    }

}
