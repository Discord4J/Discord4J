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

package discord4j.gateway;

import reactor.netty.ConnectionObserver;

/**
 * Event listeners for gateway connection lifecycle.
 */
@FunctionalInterface
public interface GatewayObserver {

    GatewayObserver NOOP_LISTENER = (newState, identifyOptions) -> {};

    static GatewayObserver emptyListener() {
        return NOOP_LISTENER;
    }

    /**
     * React on websocket state change
     *
     * @param newState the new State
     * @param identifyOptions the current shard session and sequence
     */
    void onStateChange(ConnectionObserver.State newState, IdentifyOptions identifyOptions);

    /**
     * Chain together another {@link GatewayObserver}
     *
     * @param other the next {@link GatewayObserver}
     * @return a new composite {@link GatewayObserver}
     */
    default GatewayObserver then(GatewayObserver other) {
        return CompositeGatewayObserver.compose(this, other);
    }

    ConnectionObserver.State CONNECTED = new ConnectionObserver.State() {
        @Override
        public String toString() {
            return "[gateway_connected]";
        }
    };

    ConnectionObserver.State DISCONNECTED = new ConnectionObserver.State() {
        @Override
        public String toString() {
            return "[gateway_disconnected]";
        }
    };

    ConnectionObserver.State RETRY_RESUME_STARTED = new ConnectionObserver.State() {
        @Override
        public String toString() {
            return "[retry_resume_started]";
        }
    };

    ConnectionObserver.State RETRY_STARTED = new ConnectionObserver.State() {
        @Override
        public String toString() {
            return "[retry_started]";
        }
    };

    ConnectionObserver.State RETRY_SUCCEEDED = new ConnectionObserver.State() {
        @Override
        public String toString() {
            return "[retry_succeeded]";
        }
    };

    ConnectionObserver.State RETRY_FAILED = new ConnectionObserver.State() {
        @Override
        public String toString() {
            return "[retry_failed]";
        }
    };

    ConnectionObserver.State SEQUENCE = new ConnectionObserver.State() {
        @Override
        public String toString() {
            return "[sequence]";
        }
    };
}
