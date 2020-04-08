package me.lyphium.pageupdatechecker;

import lombok.Getter;
import me.lyphium.pageupdatechecker.checker.PageChecker;
import me.lyphium.pageupdatechecker.command.*;
import me.lyphium.pageupdatechecker.database.DatabaseConnection;
import me.lyphium.pageupdatechecker.utils.Command;
import me.lyphium.pageupdatechecker.utils.Utils;

import java.util.Scanner;

@Getter
public class Bot {

    @Getter
    private static Bot instance;

    private boolean running;

    private PageChecker checker;
    private DatabaseConnection database;

    public Bot() {
        instance = this;
    }

    public void init(String[] args) {
        // Register all available commands
        registerCommands();

        // Parsing start arguments
        long delay = PageChecker.DEFAULT_DELAY;
        boolean sendingMails = true;
        for (int i = 0; i < args.length; i++) {
            final String part = args[i];

            // Parsing the delay time between checks
            if (part.equals("-d") && i < args.length - 1) {
                delay = Utils.calculateDelay(args[i + 1]);
                i++;
            } else if (part.equals("--nm")) {
                sendingMails = false;
            }
        }

        // Setting up Database Connection
        this.database = new DatabaseConnection("127.0.0.1", 3306, "PageUpdate", "root", "");

        // Creating Checker Thread
        this.checker = new PageChecker(delay, sendingMails);
    }

    public void start() {
        this.running = true;
        System.out.println("Starting Bot...");

        // Test for valid database connection
        if (!database.isConnected()) {
            System.err.println("No database connection currently available. Please check");
        }

        // Starting Checker Thread
        checker.start();

        // Handle Console commands
        handleInput();
    }

    public void stop() {
        this.running = false;
        System.out.println("Stopping Bot...");

        // Shutting down databaseconnection
        database.stop();

        // Shutting down Checker Thread
        checker.cancel();

        System.out.println("Goodbye. See you soon! c:");
    }

    private void handleInput() {
        final Scanner scanner = new Scanner(System.in);

        String line;
        while (running && scanner.hasNextLine()) {
            line = scanner.nextLine();

            // Checking if the bot should quit
            if (line.toLowerCase().startsWith("exit")) {
                break;
            }

            // Executing the command
            final boolean result = Command.execute(line);

            // Checking if a command was found, otherwise send Help Message
            if (!result) {
                System.out.println("Unknown command! Type 'help' for help.");
            }
        }

        stop();
    }

    private void registerCommands() {
        // Register all commands
        Command.registerCommand(new AddPageCommand());
        Command.registerCommand(new DelayCommand());
        Command.registerCommand(new HelpCommand());
        Command.registerCommand(new MailCommand());
        Command.registerCommand(new PrintCommand());
        Command.registerCommand(new RemovePageCommand());
        Command.registerCommand(new UpdateCommand());
    }

}