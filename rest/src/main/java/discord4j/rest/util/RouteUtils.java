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

import discord4j.common.annotations.Experimental;
import io.netty.handler.codec.http.QueryStringEncoder;
import reactor.util.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.forDigit;
import static java.lang.Character.toUpperCase;

public class RouteUtils {

    @SuppressWarnings("RegExpRedundantEscape")
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{([\\w.]+)\\}");
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private RouteUtils() {
    }

    /**
     * "Overrides" Object#toString behaviour for some objects, when we need to format them in a special form
     * For instance, this function on its creation handle long printing to make them unsigned only
     *
     * @param obj Object to handle
     * @return If handled, custom string value or else the object's toString return value
     */
    public static String handleStringConversion(Object obj) {
        if (obj instanceof Long) {
            Long number = (Long) obj;

            return Long.toUnsignedString(number);
        }

        return obj.toString();
    }

    public static String expand(String template, Object... variables) {
        if (variables.length == 0) {
            return template;
        }
        StringBuffer buf = new StringBuffer();
        Matcher matcher = PARAMETER_PATTERN.matcher(template);
        int index = 0;
        while (matcher.find()) {
            matcher.appendReplacement(buf, encodeUriPathSegment(handleStringConversion(variables[index++])));
        }
        matcher.appendTail(buf);
        return buf.toString();
    }

    @Experimental
    public static Map<String, String> createVariableMap(String template, Object... variables) {
        Map<String, String> variableMap = new LinkedHashMap<>();
        if (variables.length == 0) {
            return variableMap;
        }
        Matcher matcher = PARAMETER_PATTERN.matcher(template);
        int index = 0;
        while (matcher.find()) {
            variableMap.put(matcher.group().replaceAll("[{}]", ""), variables[index++].toString());
        }
        return variableMap;
    }

    public static String expandQuery(String uri, @Nullable Multimap<String, Object> values) {
        if (values == null) {
            return uri;
        }
        QueryStringEncoder encoder = new QueryStringEncoder(uri);
        values.forEach((key, value) -> value.forEach(o -> encoder.addParam(key, String.valueOf(o))));
        return encoder.toString();
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

    private static String encodeUriPathSegment(@Nullable String source) {
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
            if (isPathSegmentAllowed(b)) {
                bos.write(b);
            } else {
                bos.write('%');
                char hex1 = toUpperCase(forDigit((b >> 4) & 0xF, 16));
                char hex2 = toUpperCase(forDigit(b & 0xF, 16));
                bos.write(hex1);
                bos.write(hex2);
                changed = true;
            }
        }
        return (changed ? new String(bos.toByteArray(), UTF_8) : source);
    }

    private static boolean isPathSegmentAllowed(int c) {
        // unreserved      | alpha + digit
        // unreserved      | - . _ ~
        // sub-delimiter   | ! $ & ' ( )
        // sub-delimiter   | * + , ; =
        // other permitted | : @
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9'
                || c == '-' || c == '.' || c == '_' || c == '~'
                || c == '!' || c == '$' || c == '&' || c == '\'' || c == '(' || c == ')'
                || c == '*' || c == '+' || c == ',' || c == ';' || c == '='
                || c == ':' || c == '@';
    }
}
