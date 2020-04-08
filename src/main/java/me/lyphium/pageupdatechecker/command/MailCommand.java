package me.lyphium.pageupdatechecker.command;

import me.lyphium.pageupdatechecker.Bot;
import me.lyphium.pageupdatechecker.checker.PageChecker;
import me.lyphium.pageupdatechecker.utils.Command;
import me.lyphium.pageupdatechecker.utils.CommandInfo;
import me.lyphium.pageupdatechecker.utils.Utils;

@CommandInfo(
        description = "Get or change send mail status",
        usage = "mail [true/false]"
)
public class MailCommand extends Command {

    public MailCommand() {
        super("mail");
    }

    @Override
    public boolean onCommand(String label, String[] args) {
        if (args.length > 1) {
            return false;
        }

        final Bot bot = Bot.getInstance();
        final PageChecker checker = bot.getChecker();

        // Get current mail status
        boolean mail = checker.isSendingMails();

        // Check if only request or mail status change
        if (args.length == 0) {
            if (mail) {
                System.out.println("Mails are sent");
            } else {
                System.out.println("Mails aren't sent");
            }
        } else {
            // Parse new mail status
            mail = Utils.parseBoolean(args[0]);

            // Set new mail status
            checker.setSendingMails(mail);

            if (mail) {
                System.out.println("Mails are now sent");
            } else {
                System.out.println("Mails are no longer sent");
            }
        }

        return true;
    }

}