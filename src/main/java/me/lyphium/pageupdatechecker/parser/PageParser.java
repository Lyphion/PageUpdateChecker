package me.lyphium.pageupdatechecker.parser;

import lombok.Getter;
import lombok.Setter;
import me.lyphium.pageupdatechecker.Bot;
import me.lyphium.pageupdatechecker.database.DatabaseConnection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class PageParser extends Thread {

    public static final long DEFAULT_DELAY = 60 * 60 * 1000;

    @Getter
    @Setter
    private long delay;

    public PageParser(long delay) {
        this.delay = delay;

        setName("PageParser");
        setDaemon(true);
    }

    @Override
    public void run() {
        if (delay < 0) {
            System.out.println("Automatic Page Parser disabled");
            return;
        }

        System.out.println("Started Page Parser");
        System.out.println("Checking Pages every " + (delay / 1000) + "sec");

        // Checking if the bot is still running
        while (Bot.getInstance().isRunning()) {
            try {
                long time = System.currentTimeMillis();

                // Checking if the connection to the database is available, otherwise don't update prices
                if (!Bot.getInstance().getDatabase().isConnected()) {
                    System.err.println("Can't update database! No connection available");
                } else {
                    System.out.println("Updating Prices...");

                    // Update prices
                    update();

                    System.out.println("Finished: Updated the Prices (" + (System.currentTimeMillis() - time) + "ms)");
                }

                // Calculate sleeping time from delay
                time = delay - (System.currentTimeMillis() - time);
                if (time > 0) {
                    Thread.sleep(time);
                }
            } catch (InterruptedException e) {
                // Thrown when PageParseThread is shutting down
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void update() {
        final DatabaseConnection database = Bot.getInstance().getDatabase();

        // Checking if the connection to the database is available
        if (!database.isConnected()) {
            System.err.println("Can't update database! No connection available");
            return;
        }


        System.gc();
    }

    public synchronized void cancel() {
        interrupt();

        if (delay < 0) {
            return;
        }

        System.out.println("Shut down Page Parser");
    }

    private Document loadPage(String url) {
        try {
            // Load HTML-Page
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
