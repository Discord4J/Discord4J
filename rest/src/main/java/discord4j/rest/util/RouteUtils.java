/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.rest.util;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RouteUtils {

    private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{([\\w.]+)}");

    private RouteUtils() {
    }

    public static String expand(String template, Object... variables) {
        StringBuffer buf = new StringBuffer();
        Matcher matcher = PARAMETER_PATTERN.matcher(template);
        int index = 0;
        while (matcher.find()) {
            matcher.appendReplacement(buf, variables[index++].toString());
        }
        matcher.appendTail(buf);
        return buf.toString();
    }

    public static String expandQuery(String uri, @Nullable Map<String, ?> values) {
        if (values != null && !uri.contains("?")) {
            uri += "?" + values.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));
        }
        return uri;
    }

    @Nullable
    public static String getMajorParam(String template, String complete) {
        int start = template.indexOf('{');
        if (start == -1) {
            return null;
        }

        int end = complete.indexOf('/', start);
        if (end == -1) {
            end = complete.length();
        }

        return complete.substring(start, end);
    }
}
