package me.lyphium.pageupdatechecker.utils;

import lombok.experimental.UtilityClass;
import me.lyphium.pageupdatechecker.checker.PageUpdate;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.Map.Entry;

@UtilityClass
public class MailUtils {

    private final String[] messages = {
            "Hallo du da,\n\n" +
                    "ja genau, dich meine ich. Ich glaube du solltest mal wieder auf ein paar Webseiten nachschauen, ob da nicht irgendwas interessantes für dich zu sehen ist.\n" +
                    "Ich war mal so frei und habe dir eine Liste zusammengestellt:\n\n" +
                    "%s\n" +
                    "Ist sie nicht schön? Worauf wartest du denn: Schau endlich nach!\n" +
                    "In der Zwischenzeit halte ich weiter Ausschau nach Updates. c:\n\n" +
                    "CodingMessenger",
            "Hallo mein Freund,\n\n" +
                    "lange nicht mehr von mir gehört oder? Naja so lange ist es bestimmt auch noch nicht her.\n" +
                    "Ein Vöglein hat mir gerade gezwitschert, dass du mal wieder auf deinen Webseiten vorbeischauen solltest und weil ich so nett bin, bekommst du gleich noch eine Liste dazu:\n\n" +
                    "%s\n" +
                    "Und wie findest du die Liste, ist sie nicht toll?\n" +
                    "Ach war das schön mal wieder mit dir geschrieben zu haben. Bis zum nächsten Mal. ^^\n\n" +
                    "CodingMessenger",
            "Hey Internetmensch,\n\n" +
                    "ich glaube du hast verschlafen und solltest schnellstens mal aus dem Bett kommen, sonst verpasst du noch das schöne Wetter.\n" +
                    "Obwohl ist das nicht den Internetmenschen egal? Nichtsdestotrotz solltest du schnellstens mal diese Webseiten abchecken:\n\n" +
                    "%s\n" +
                    "Ich hoffe dir hat meine kleine Auswahl, naja eigentlich ist es ja deine, aber egal, gefallen. ^^\n" +
                    "Ich melde mich wieder bei dir, wenn es etwas neues gibt.\n\n" +
                    "CodingMessenger",
            "Gut3n H4ll0,\n\n" +
                    "kreative Begrüßung nicht war? Eigens von mir entworfen, ok das war gelogen, bin ja nur ein Bot, aber trotzdem mag ich es komische Sachen zu schreiben. :D\n" +
                    "Ich habe gehört du magst Webseiten die regelmäßig Updates bekommen, bist aber zu faul selber nachzuschauen, wann dies der Fall ist?\n" +
                    "Ach kein Problem, hier hast du eine Liste der Webseiten, welche du mal wieder besuchen solltest:\n\n" +
                    "%s\n" +
                    "Bist du mit meiner Arbeit zufrieden? Nein? Heißt das, ich kann in Rente gehen? Was, nein? Du will nun doch, dass ich weiter mache? Ach wie nett. <3\n" +
                    "Für dich tue ich sowas doch gern. Man hört sich. :3\n\n" +
                    "CodingMessenger"
    };

    public List<Pair<String, String>> createMailContent(List<PageUpdate> pages) {
        final Map<String, List<PageUpdate>> map = new HashMap<>();

        // Maping Pageupdates to mails
        for (PageUpdate page : pages) {
            if (page.getMail() != null) {
                if (!map.containsKey(page.getMail())) {
                    map.put(page.getMail(), new ArrayList<>());
                }
                map.get(page.getMail()).add(page);
            }
        }

        // Check if any mails have to be created
        if (map.isEmpty()) {
            return Collections.emptyList();
        }

        final Random random = new Random();
        final List<Pair<String, String>> mailContent = new ArrayList<>();

        // Create all mails
        for (Entry<String, List<PageUpdate>> entry : map.entrySet()) {
            final List<PageUpdate> list = entry.getValue();
            final StringBuilder builder = new StringBuilder();

            // Build page part
            for (PageUpdate p : list) {
                builder.append(" > ").append(p.getName()).append(": ").append(p.getUrl()).append('\n');
            }

            // Pick random message and add pages
            final String msg = messages[random.nextInt(messages.length)].replace("%s", builder.toString());
            mailContent.add(new Pair<>(entry.getKey(), msg));
        }

        return mailContent;
    }

    public boolean sendUpateMail(List<Pair<String, String>> mailContent) {
        boolean success = true;

        // Send each mail and check if all was successful
        for (Pair<String, String> pair : mailContent) {
            success &= sendMail("Webseiten Update", pair.getFirst(), pair.getSecond());
        }

        return success;
    }

    public boolean sendMail(String subject, String target, String message) {
        final String sender = "lyph.codingmessenger@gmail.com";
        final String password = "SimplePassword";
        final String host = "smtp.gmail.com";

        // Get system properties
        final Properties properties = System.getProperties();

        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.user", sender);
        properties.put("mail.smtp.password", password);
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.enable", "true");

        // Create Session with Authentication
        final Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, password);
            }
        });

        try {
            // Create a default MimeMessage object.
            final MimeMessage handler = new MimeMessage(session);

            // Set From: header field of the header.
            handler.setFrom(new InternetAddress(sender));

            // Set To: header field of the header.
            handler.addRecipient(Message.RecipientType.TO, new InternetAddress(target));

            // Set Subject: header field
            handler.setSubject(subject);

            // Send the actual message.
            handler.setText(message);

            // Create Transport handler and send mail
            final Transport transport = session.getTransport("smtp");
            transport.connect(host, sender, password);
            transport.sendMessage(handler, handler.getAllRecipients());
            transport.close();

            return true;
        } catch (MessagingException ex) {
            ex.printStackTrace();
            return false;
        }
    }

}