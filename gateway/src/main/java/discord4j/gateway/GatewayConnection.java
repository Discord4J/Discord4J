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

public class GatewayConnection {

    public enum State {
        /**
         * Propagated when a brand new connection is in the process of authenticating through IDENTIFY payload.
         */
        START_IDENTIFYING,
        /**
         * Propagated when a brand new connection is in the process of resuming a session.
         */
        START_RESUMING,
        /**
         * Propagated when a connection receives a READY or RESUMED event.
         */
        CONNECTED,
        /**
         * Propagated when a connection is closed but before a retry or stop process is started.
         */
        DISCONNECTING,
        /**
         * Propagated when a connection is being retried and the current session can be resumed.
         */
        RESUMING,
        /**
         * Propagated when a connection is being retried and a new session must be established.
         */
        RECONNECTING,
        /**
         * Propagated when a disconnection happens and is in the process or releasing its resources.
         */
        DISCONNECTED
    }
}
