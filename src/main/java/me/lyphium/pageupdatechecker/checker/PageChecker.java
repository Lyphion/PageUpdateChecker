package me.lyphium.pageupdatechecker.checker;

import lombok.Getter;
import lombok.Setter;
import me.lyphium.pageupdatechecker.Bot;
import me.lyphium.pageupdatechecker.database.DatabaseConnection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PageChecker extends Thread {

    public static final long DEFAULT_DELAY = 60 * 60 * 1000;

    private static final OutputSettings SETTINGS = new OutputSettings().prettyPrint(false);

    @Getter
    @Setter
    private long delay;

    public PageChecker(long delay) {
        this.delay = delay;

        setName("PageChecker");
        setDaemon(true);
    }

    @Override
    public void run() {
        if (delay < 0) {
            System.out.println("Automatic Page Checker disabled");
            return;
        }

        System.out.println("Started Page Checker");
        System.out.println("Checking Pages every " + (delay / 1000) + "sec");

        // Checking if the bot is still running
        while (Bot.getInstance().isRunning()) {
            try {
                long time = System.currentTimeMillis();

                // Checking if the connection to the database is available, otherwise don't check for update
                if (!Bot.getInstance().getDatabase().isConnected()) {
                    System.err.println("Can't update database! No connection available");
                } else {
                    System.out.println("Checking pages...");

                    // Check for page update
                    final List<PageUpdate> pages = update();
                    if (pages != null && pages.size() > 0) {
                        System.out.println(pages.stream().map(PageUpdate::getName).collect(Collectors.joining(", ", "Updates: ", "")));
                    }

                    System.out.println("Finished: Check complete (" + (System.currentTimeMillis() - time) + "ms)");
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

    public synchronized List<PageUpdate> update() {
        final DatabaseConnection database = Bot.getInstance().getDatabase();

        // Checking if the connection to the database is available
        if (!database.isConnected()) {
            System.err.println("Can't update database! No connection available");
            return null;
        }

        // All available pages
        final List<PageUpdate> pages = database.getPages();
        final long time = System.currentTimeMillis();

        final List<PageUpdate> updatedPages = new ArrayList<>();

        // Nothing to update if no pages are available
        if (pages.isEmpty()) {
            return updatedPages;
        }

        pages.parallelStream().forEach(page -> {
            // Load HTML-Page
            final Document doc = loadPage(page.getUrl());
            doc.outputSettings(SETTINGS);

            // Check if prices exists (HTML-Page correct and prices exist)
            if (doc == null) {
                System.err.println("Couldn't load page for: " + page.getName());
                return;
            }

            final Document old = Parser.parse(page.getContent(), page.getUrl());
            old.outputSettings(SETTINGS);

            if (hasChanged(old, doc)) {
                page.setLastUpdate(time);
                page.setContent(doc.toString());

                System.out.println(doc.toString());

                updatedPages.add(page);
            }
        });

        // Save prices in database
        database.savePages(updatedPages);
        System.gc();

        return updatedPages;
    }

    public synchronized void cancel() {
        interrupt();

        if (delay < 0) {
            return;
        }

        System.out.println("Shut down Page Checker");
    }

    private Document loadPage(String url) {
        try {
            // Load HTML-Page
            final Document doc = Jsoup.connect(url).get();
            doc.select("script").remove();
            return doc;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean hasChanged(Document oldPage, Document newPage) {
        return !oldPage.hasSameValue(newPage);
    }

}