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

package discord4j.core.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

import java.util.ArrayList;
import java.util.List;

public class GatewayEventFilter extends TurboFilter {

    private final /*~~>*/List<String> loggers = new ArrayList<>();
    private final /*~~>*/List<String> includedEvents = new ArrayList<>();
    private final /*~~>*/List<String> excludedEvents = new ArrayList<>();

    @Override
    public FilterReply decide(Marker marker, Logger log, Level level, String format, Object[] params, Throwable t) {
        String logName = log.getName();
        if (loggers.contains(logName)) {
            if (format != null) {
                if (!excludedEvents.isEmpty() && excludedEvents.stream().anyMatch(format::contains)) {
                    return FilterReply.DENY;
                }
                if (!includedEvents.isEmpty() && includedEvents.stream().noneMatch(format::contains)) {
                    return FilterReply.DENY;
                }
            }
        }
        return FilterReply.NEUTRAL;
    }

    public void addLogger(String logger) {
        /*~~>*/this.loggers.add(logger);
    }

    public void addInclude(String include) {
        /*~~>*/this.includedEvents.add(include);
    }

    public void addExclude(String exclude) {
        /*~~>*/this.excludedEvents.add(exclude);
    }

}
