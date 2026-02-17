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

import org.jspecify.annotations.Nullable;
import reactor.util.Logger;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility to support logging details.
 */
public class LogUtil {

    public static final String KEY_BUCKET_ID = "discord4j.bucket";
    public static final String KEY_REQUEST_ID = "discord4j.request";
    public static final String KEY_GATEWAY_ID = "discord4j.gateway";
    public static final String KEY_SHARD_ID = "discord4j.shard";
    public static final String KEY_GUILD_ID = "discord4j.guild";

    /**
     * Format a message by unwrapping certain {@link ContextView} values as metadata, and if they exist, prepend them to
     * the given message.
     *
     * @param context a Reactor context to enrich the logging message
     * @param msg the logging message
     * @return a formatted log message
     */
    public static String format(ContextView context, String msg) {
        String header = Stream.of(
                context.getOrEmpty(KEY_BUCKET_ID).map(id -> "B:" + id),
                context.getOrEmpty(KEY_REQUEST_ID).map(id -> "R:" + id),
                context.getOrEmpty(KEY_GATEWAY_ID).map(id -> "G:" + id),
                context.getOrEmpty(KEY_SHARD_ID).map(id -> "S:" + id),
                context.getOrEmpty(KEY_GUILD_ID).map(id -> "guildId:" + id))
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

    /**
     * Format a given {@link Object} to a {@link String}, optionally limiting their length.
     *
     * @param value the value to format
     * @param maxLength maximum length of the formatted value if positive or impose no limit otherwise.
     * @return a formatted value
     */
    public static String formatValue(@Nullable Object value, int maxLength) {
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
        return (maxLength > 0 && str.length() > maxLength ? str.substring(0, maxLength) + "..." : str);
    }

    /**
     * Log a message depending on the enabled level for a given {@link Logger}. The supplied message factory will be
     * applied with {@code true} if the logger has trace level enabled, given by {@link Logger#isTraceEnabled()}, o
     * {@code false} otherwise.
     *
     * @param logger the logger instance used for logging a message
     * @param messageFactory a {@link Function} that takes {@code true} value if the given logger has trace level
     * enabled, {@code false} otherwise, and produces a message to log.
     */
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

    public static Function<Context, Context> clearContext() {
        return ctx -> ctx.delete(KEY_BUCKET_ID)
                .delete(KEY_REQUEST_ID)
                .delete(KEY_GATEWAY_ID)
                .delete(KEY_SHARD_ID)
                .delete(KEY_GUILD_ID);
    }
}
