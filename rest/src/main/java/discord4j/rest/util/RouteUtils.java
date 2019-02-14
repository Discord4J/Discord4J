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

import reactor.util.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RouteUtils {

    private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{([\\w.]+)}");
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private RouteUtils() {
    }

    public static String expand(String template, Object... variables) {
        if (variables.length == 0) {
            return template;
        }
        StringBuffer buf = new StringBuffer();
        Matcher matcher = PARAMETER_PATTERN.matcher(template);
        int index = 0;
        while (matcher.find()) {
            matcher.appendReplacement(buf, encodeUriComponent(variables[index++].toString(), Type.PATH_SEGMENT));
        }
        matcher.appendTail(buf);
        return buf.toString();
    }

    public static String expandQuery(String uri, @Nullable Map<String, ?> values) {
        if (values != null && !uri.contains("?")) {
            uri += "?" + values.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .map(queryParam -> encodeUriComponent(queryParam, Type.QUERY))
                    .collect(Collectors.joining("&"));
        }
        return uri;
    }

    @Nullable
    public static String getMajorParam(String template, String complete) {
        // Currently, the only major parameters are channel.id, guild.id, and webhook.id
        if (template.contains("{channel.id}") || template.contains("{guild.id}") || template.contains("{webhook.id}")) {
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
        return null;
    }

    private static String encodeUriComponent(@Nullable String source, Type type) {
        if (source == null || source.isEmpty()) {
            return "";
        }

        byte[] bytes = source.getBytes(UTF_8);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);
        boolean changed = false;
        for (byte b : bytes) {
            if (b < 0) {
                b += 256;
            }
            if (type.isAllowed(b)) {
                bos.write(b);
            } else {
                bos.write('%');
                char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
                char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
                bos.write(hex1);
                bos.write(hex2);
                changed = true;
            }
        }
        return (changed ? new String(bos.toByteArray(), UTF_8) : source);
    }

    /**
     * Used to identify allowed characters per URI component. This is a simplified implementation partially covering
     * all component types.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986</a>
     */
    enum Type {
        PATH {
            @Override
            public boolean isAllowed(int c) {
                return isPchar(c) || '/' == c;
            }
        },
        PATH_SEGMENT {
            @Override
            public boolean isAllowed(int c) {
                return isPchar(c);
            }
        },
        QUERY {
            @Override
            public boolean isAllowed(int c) {
                return isPchar(c) || '/' == c || '?' == c;
            }
        },
        QUERY_PARAM {
            @Override
            public boolean isAllowed(int c) {
                if ('=' == c || '&' == c) {
                    return false;
                } else {
                    return isPchar(c) || '/' == c || '?' == c;
                }
            }
        };

        public abstract boolean isAllowed(int c);

        protected boolean isAlpha(int c) {
            return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z');
        }

        protected boolean isDigit(int c) {
            return (c >= '0' && c <= '9');
        }

        protected boolean isGenericDelimiter(int c) {
            return (':' == c || '/' == c || '?' == c || '#' == c || '[' == c || ']' == c || '@' == c);
        }

        protected boolean isSubDelimiter(int c) {
            return ('!' == c || '$' == c || '&' == c || '\'' == c || '(' == c || ')' == c || '*' == c || '+' == c ||
                    ',' == c || ';' == c || '=' == c);
        }

        protected boolean isReserved(int c) {
            return (isGenericDelimiter(c) || isSubDelimiter(c));
        }

        protected boolean isUnreserved(int c) {
            return (isAlpha(c) || isDigit(c) || '-' == c || '.' == c || '_' == c || '~' == c);
        }

        protected boolean isPchar(int c) {
            return (isUnreserved(c) || isSubDelimiter(c) || ':' == c || '@' == c);
        }
    }
}
