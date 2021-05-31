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

package discord4j.gateway.retry;

import discord4j.common.close.CloseStatus;
import discord4j.common.close.DisconnectBehavior;

public class ClosingStateChange extends GatewayStateChange {
    private final DisconnectBehavior behavior;
    private final CloseStatus status;

    protected ClosingStateChange(DisconnectBehavior behavior, CloseStatus status) {
        super(State.DISCONNECTED, 0, null);
        this.behavior = behavior;
        this.status = status;
    }

    public DisconnectBehavior getBehavior() {
        return behavior;
    }

    public CloseStatus getStatus() {
        return status;
    }
}
