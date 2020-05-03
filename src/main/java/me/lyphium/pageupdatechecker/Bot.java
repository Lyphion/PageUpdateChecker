package me.lyphium.pageupdatechecker;

import lombok.Getter;
import me.lyphium.pageupdatechecker.checker.PageChecker;
import me.lyphium.pageupdatechecker.command.*;
import me.lyphium.pageupdatechecker.database.DatabaseConnection;
import me.lyphium.pageupdatechecker.utils.Command;
import me.lyphium.pageupdatechecker.utils.PrettyPrintStream;
import me.lyphium.pageupdatechecker.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
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
            }
            // Disable update mails
            else if (part.equals("--nm")) {
                sendingMails = false;
            }
            // Disable log file
            else if (part.equals("-nl")) {
                PrettyPrintStream.setLog(false);
            }
        }

        // Creating Checker Thread
        this.checker = new PageChecker(delay, sendingMails);
    }

    public void start() {
        this.running = true;
        System.out.println("Starting Bot...");

        if (!PrettyPrintStream.isLog()) {
            System.out.println("Log disabled");
        }

        // Setting up Database Connection
        this.database = loadDatabase();

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

    private DatabaseConnection loadDatabase() {
        final File databaseFile = new File("database.properties");

        // Check if file doesn't exists
        if (!databaseFile.exists()) {
            System.out.println("No database file found. Using default settings");

            // Copy deafult config file to change it manually
            try {
                Files.copy(getClass().getResourceAsStream("/database.properties"), databaseFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Using default values
            return new DatabaseConnection("127.0.0.1", 3306, "pageupdate", "root", "password");
        } else {
            System.out.println("Database file found. Using custom settings");

            // Reading config file
            try (BufferedReader reader = Files.newBufferedReader(databaseFile.toPath())) {
                // Loading properties
                final Properties props = new Properties();
                props.load(reader);

                final String host = props.getProperty("host", "127.0.0.1");

                final String portString = props.getProperty("port", "3306");

                final int port;
                // Check if port is valid
                if (portString.matches("[0-9]{1,5}")) {
                    port = Integer.parseUnsignedInt(portString);
                } else {
                    System.err.println("Invalid port. Using port '3306' instead");
                    port = 3306;
                }

                final String database = props.getProperty("database", "pageupdate");
                final String username = props.getProperty("username", "root");
                final String password = props.getProperty("password", "password");

                // Creating and return DatabaseConnection Object
                return new DatabaseConnection(host, port, database, username, password);
            } catch (IOException e) {
                System.err.println("Invalid database file. Please check");
                e.printStackTrace();
                return null;
            }
        }
    }

}