package me.lyphium.pageupdatechecker.checker;

import lombok.Getter;
import lombok.Setter;
import me.lyphium.pageupdatechecker.Bot;
import me.lyphium.pageupdatechecker.database.DatabaseConnection;
import me.lyphium.pageupdatechecker.utils.MailUtils;
import me.lyphium.pageupdatechecker.utils.Pair;
import me.lyphium.pageupdatechecker.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PageChecker {

    public static final long DEFAULT_PERIOD = 60 * 60 * 1000;

    private static final OutputSettings SETTINGS = new OutputSettings().prettyPrint(false);

    @Getter
    private final long period;
    @Getter
    private final long startTime;
    @Getter @Setter
    private boolean sendingMails;

    private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

    public PageChecker(long period, long startTime, boolean sendingMails) {
        this.period = period;
        this.startTime = startTime;
        this.sendingMails = sendingMails;
    }

    public void start() {
        if (period < 0) {
            System.out.println("Automatic Page Checker disabled");
            return;
        }

        System.out.println("Started Page Checker");

        if (period < 1000) {
            System.out.println("Checking Pages every " + period + "ms");
        } else {
            System.out.println("Checking Pages every " + (period / 1000) + "sec");
        }

        if (sendingMails) {
            System.out.println("Mails are sent");
        } else {
            System.out.println("Mails aren't sent");
        }

        final long startDelay;
        if (startTime > System.currentTimeMillis()) {
            System.out.println("First Check: " + Utils.toString(new Date(startTime)));
            startDelay = startTime - System.currentTimeMillis();
        } else {
            startDelay = 0;
        }

        service.scheduleAtFixedRate(
                this::update,
                startDelay,
                period,
                TimeUnit.MILLISECONDS
        );
    }

    public synchronized void update() {
        long time = System.currentTimeMillis();

        try {
            // Checking if the connection to the database is available, otherwise don't check for update
            if (!Bot.getInstance().getDatabase().isConnected()) {
                System.err.println("Can't update database! No connection available");
                return;
            }

            System.out.println("Checking pages...");

            // Check for page update
            final List<PageUpdate> pages = handleUpdate();
            if (pages != null && pages.size() > 0) {
                System.out.println(pages.stream().map(PageUpdate::getName).collect(Collectors.joining(", ", "Updates: ", "")));

                // Check if mails should be sended
                if (sendingMails) {
                    // Creating mails
                    final List<Pair<String, String>> list = MailUtils.createMailContent(pages);
                    final boolean success = MailUtils.sendUpdateMail(list);

                    // Printing if mailing was successful
                    if (success) {
                        System.out.println("Sended " + list.size() + " mails");
                    } else {
                        System.err.println("It looks like at least one mail had a problem will sending");
                    }
                }
            }

            time = System.currentTimeMillis() - time;
            System.out.println("Finished: Check complete (" + time + "ms)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized List<PageUpdate> handleUpdate() {
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
            final String doc = loadPage(page.getUrl());

            // Check if page exists
            if (doc == null) {
                System.err.println("Couldn't load page for: " + page.getName());
                return;
            }

            // Check if pagecontent has changed
            if (!doc.equals(page.getContent())) {
                page.setLastUpdate(time);
                page.setContent(doc);

                updatedPages.add(page);
            }
        });

        // Save pages in database
        database.savePages(updatedPages);
        System.gc();

        return updatedPages;
    }

    public synchronized void cancel() {
        service.shutdown();

        if (period < 0) {
            return;
        }

        System.out.println("Shut down Page Checker");
    }

    public String loadPage(String url) {
        try {
            // Load HTML-Page
            final Document doc = Jsoup.connect(url).get();

            // Trim page and string structure
            if (doc != null) {
                doc.outputSettings(SETTINGS);
                doc.select("script").remove();

                return doc.outerHtml();
            }

            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}