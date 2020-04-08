package me.lyphium.pageupdatechecker.command;

import me.lyphium.pageupdatechecker.Bot;
import me.lyphium.pageupdatechecker.checker.PageChecker;
import me.lyphium.pageupdatechecker.utils.Command;
import me.lyphium.pageupdatechecker.utils.CommandInfo;
import me.lyphium.pageupdatechecker.utils.Utils;

@CommandInfo(
        description = "Get or change update delay",
        usage = "delay [value]"
)
public class DelayCommand extends Command {

    public DelayCommand() {
        super("delay");
    }

    @Override
    public boolean onCommand(String label, String[] args) {
        if (args.length > 1) {
            return false;
        }

        final Bot bot = Bot.getInstance();
        final PageChecker checker = bot.getChecker();

        // Get current delay
        long delay = checker.getDelay();

        // Check if only request or delay change
        if (args.length == 0) {
            if (delay < 0) {
                System.out.println("Page Checker is disabled");
            } else {
                System.out.println("Current delay: " + delay + "ms");
            }
        } else {
            if (delay < 0) {
                System.out.println("Page Checker is disabled. To change delay restart Bot");
                return true;
            }

            // Parse new delay as a number or time string
            delay = Utils.calculateDelay(args[0]);

            // Set new delay
            checker.setDelay(delay);

            if (delay < 0) {
                checker.cancel();
                System.out.println("Shut down Page Checker");
            } else {
                System.out.println("New delay: " + delay + "ms");
            }
        }

        return true;
    }

}