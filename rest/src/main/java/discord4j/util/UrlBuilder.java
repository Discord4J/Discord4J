package discord4j.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public static String expand(String template, Map<String, ?> values) {
        StringBuffer buf = new StringBuffer();
        Matcher matcher = VARIABLE.matcher(template);
        Set<String> matchedKeys = new HashSet<>();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = values.get(key);
            if (value != null) {
                matchedKeys.add(key);
                matcher.appendReplacement(buf, value.toString());
            }
        }
        matcher.appendTail(buf);
        if (values.size() != matchedKeys.size()) {
            buf.append("?").append(values.entrySet().stream()
                    .filter(entry -> !matchedKeys.contains(entry.getKey()))
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&")));
        }
        return buf.toString();
    }

    public static String expand(String template, Map<String, ?> values, Object... variables) {
        StringBuffer buf = new StringBuffer();
        Matcher matcher = VARIABLE.matcher(template);
        int index = 0;
        while (matcher.find()) {
            matcher.appendReplacement(buf, variables[index++].toString());
        }
        matcher.appendTail(buf);
        if (values != null && !values.isEmpty()) {
            buf.append("?").append(values.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&")));
        }
        return buf.toString();
    }
}
