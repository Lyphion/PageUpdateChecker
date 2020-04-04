package me.lyphium.pageupdatechecker.utils;

import lombok.experimental.UtilityClass;
import me.lyphium.pageupdatechecker.parser.PageParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class Utils {

    private final SimpleDateFormat format = new SimpleDateFormat("dd.MM.YYYY HH:mm:ss");

    private final Map<String, Long> delayTable = new HashMap<String, Long>() {{
        put("[\\d]+(?=s)", 1L);
        put("[\\d]+(?=m)", 60L);
        put("[\\d]+(?=h)", 3600L);
        put("[\\d]+(?=d)", 86400L);
    }};

    public long calculateDelay(String s) {
        try {
            if (s.matches("(-)?(\\d)+")) {
                return Long.parseLong(s);
            }

            long delay = 0;
            for (Entry<String, Long> entry : delayTable.entrySet()) {
                Pattern p = Pattern.compile(entry.getKey());
                Matcher m = p.matcher(s);

                if (m.find()) {
                    delay += Long.parseUnsignedLong(s.substring(m.start(), m.end())) * entry.getValue();
                }
            }
            return delay * 1000;
        } catch (Exception e) {
            return PageParser.DEFAULT_DELAY;
        }
    }

    public String getDomain(String url) {
        try {
            final String domain = new URI(url).getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public Timestamp toTimestamp(String s) {
        try {
            if (s.equalsIgnoreCase("now")) {
                return new Timestamp(System.currentTimeMillis());
            } else if (s.matches("(-)?(\\d)+")) {
                return new Timestamp(Long.parseLong(s));
            }
            return Timestamp.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }

    public String toString(Date date) {
        return format.format(date);
    }

}