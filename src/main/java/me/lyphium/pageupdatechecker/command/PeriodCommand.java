package me.lyphium.pageupdatechecker.command;

import me.lyphium.pageupdatechecker.Bot;
import me.lyphium.pageupdatechecker.checker.PageChecker;
import me.lyphium.pageupdatechecker.utils.Command;
import me.lyphium.pageupdatechecker.utils.CommandInfo;

@CommandInfo(
        description = "Get page check period time",
        usage = "period"
)
public class PeriodCommand extends Command {

    public PeriodCommand() {
        super("period");
    }

    @Override
    public boolean onCommand(String label, String[] args) {
        if (args.length > 0) {
            return false;
        }

        final Bot bot = Bot.getInstance();
        final PageChecker checker = bot.getChecker();

        // Get current delay
        final long period = checker.getPeriod();

        // Check if only request or delay change
        if (period < 0) {
            System.out.println("Page Checker is disabled");
        } else if (period < 1000) {
            System.out.println("Checking Pages every " + period + "ms");
        } else {
            System.out.println("Checking Pages every " + (period / 1000) + "sec");
        }

        return true;
    }

}