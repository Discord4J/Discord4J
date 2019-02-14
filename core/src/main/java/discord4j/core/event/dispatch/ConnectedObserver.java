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

package discord4j.core.event.dispatch;

import discord4j.gateway.GatewayObserver;
import discord4j.gateway.IdentifyOptions;
import reactor.netty.ConnectionObserver;

import java.util.function.Consumer;

/**
 * Allows notifying an externally managed sink for {@link GatewayObserver#CONNECTED} events.
 */
public class ConnectedObserver implements GatewayObserver {

    private final Consumer<IdentifyOptions> task;

    public ConnectedObserver(Consumer<IdentifyOptions> task) {
        this.task = task;
    }

    @Override
    public void onStateChange(ConnectionObserver.State newState, IdentifyOptions identifyOptions) {
        if (GatewayObserver.CONNECTED.equals(newState)) {
            task.accept(identifyOptions);
        }
    }
}
