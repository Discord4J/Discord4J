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

import discord4j.core.StateHolder;
import discord4j.gateway.GatewayObserver;
import discord4j.gateway.IdentifyOptions;
import reactor.netty.ConnectionObserver;
import reactor.util.Logger;
import reactor.util.Loggers;

public class StoreInvalidator implements GatewayObserver {

    private static final Logger log = Loggers.getLogger(StoreInvalidator.class);

    private final StateHolder stateHolder;

    public StoreInvalidator(StateHolder stateHolder) {
        this.stateHolder = stateHolder;
    }

    @Override
    public void onStateChange(ConnectionObserver.State newState, IdentifyOptions identifyOptions) {
        if (GatewayObserver.RETRY_STARTED.equals(newState)
                || GatewayObserver.RETRY_FAILED.equals(newState)
                || GatewayObserver.DISCONNECTED.equals(newState)) {
            log.debug("Invalidating stores");
            stateHolder.invalidateStores().subscribe();
        }
    }
}
