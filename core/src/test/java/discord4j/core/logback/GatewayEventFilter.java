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
import discord4j.gateway.DefaultGatewayClient;
import org.slf4j.Marker;

import java.util.Arrays;
import java.util.List;

public class GatewayEventFilter extends TurboFilter {

    private String include;
    private String exclude;
    private List<String> includedEvents;
    private List<String> excludedEvents;

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if (logger.getName().startsWith(DefaultGatewayClient.class.getName())) {
            if (format != null && format.contains("\"t\"")) {
                if (excludedEvents != null) {
                    if (excludedEvents.stream().anyMatch(format::contains)) {
                        return FilterReply.DENY;
                    }
                } else if (includedEvents != null) {
                    if (includedEvents.stream().noneMatch(format::contains)) {
                        return FilterReply.DENY;
                    }
                }
            }
        }
        return FilterReply.NEUTRAL;
    }

    public void setInclude(String include) {
        this.include = include;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    @Override
    public void start() {
        if (exclude != null && exclude.trim().length() > 0) {
            excludedEvents = Arrays.asList(exclude.split("[;,]"));
            super.start();
        } else if (include != null && include.trim().length() > 0) {
            includedEvents = Arrays.asList(include.split("[;,]"));
            super.start();
        }
    }
}
