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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.common;

import reactor.util.Logger;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogUtil {

    public static final String KEY_BUCKET_ID = "discord4j.bucket";
    public static final String KEY_REQUEST_ID = "discord4j.request";
    public static final String KEY_GATEWAY_ID = "discord4j.gateway";
    public static final String KEY_SHARD_ID = "discord4j.shard";

    public static String format(Context context, String msg) {
        String header = Stream.of(
                context.getOrEmpty(KEY_BUCKET_ID).map(id -> "B:" + id),
                context.getOrEmpty(KEY_REQUEST_ID).map(id -> "R:" + id),
                context.getOrEmpty(KEY_GATEWAY_ID).map(id -> "G:" + id),
                context.getOrEmpty(KEY_SHARD_ID).map(id -> "S:" + id))
                .map(opt -> opt.orElse(""))
                .filter(str -> !str.isEmpty())
                .collect(Collectors.joining(", "));
        StringBuilder builder = new StringBuilder();
        if (!header.isEmpty()) {
            return builder.append('[')
                    .append(header)
                    .append("] ")
                    .append(msg)
                    .toString();
        } else {
            return msg;
        }
    }

    public static String formatValue(@Nullable Object value, boolean limitLength) {
        if (value == null) {
            return "";
        }
        String str;
        if (value instanceof CharSequence) {
            str = String.valueOf(value);
        } else {
            try {
                str = value.toString();
            } catch (Throwable ex) {
                str = ex.toString();
            }
        }
        return (limitLength && str.length() > 100 ? str.substring(0, 100) + "..." : str);
    }

    public static void traceDebug(Logger logger, Function<Boolean, String> messageFactory) {
        if (logger.isDebugEnabled()) {
            boolean traceEnabled = logger.isTraceEnabled();
            String logMessage = messageFactory.apply(traceEnabled);
            if (traceEnabled) {
                logger.trace(logMessage);
            } else {
                logger.debug(logMessage);
            }
        }
    }
}
