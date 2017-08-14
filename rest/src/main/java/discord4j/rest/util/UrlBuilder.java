package discord4j.rest.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlBuilder {

    private static final Pattern VARIABLE = Pattern.compile("\\{([\\w.]+)}");

    private UrlBuilder() {
    }

    public static String expand(String template, Object... variables) {
        StringBuffer buf = new StringBuffer();
        Matcher matcher = VARIABLE.matcher(template);
        int index = 0;
        while (matcher.find()) {
            matcher.appendReplacement(buf, variables[index++].toString());
        }
        matcher.appendTail(buf);
        return buf.toString();
    }
}
