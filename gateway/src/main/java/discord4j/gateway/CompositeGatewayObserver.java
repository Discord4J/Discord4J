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

class CompositeGatewayObserver implements GatewayObserver {

    private final GatewayObserver[] observers;

    private CompositeGatewayObserver(GatewayObserver[] observers) {
        this.observers = observers;
    }

    @Override
    public void onStateChange(ConnectionObserver.State newState, GatewayClient gatewayClient) {
        for (GatewayObserver observer : observers) {
            observer.onStateChange(newState, gatewayClient);
        }
    }

    static GatewayObserver compose(GatewayObserver first, GatewayObserver second) {
        if (first == GatewayObserver.NOOP_LISTENER) {
            return second;
        }

        if (second == GatewayObserver.NOOP_LISTENER) {
            return first;
        }

        final GatewayObserver[] combinedObservers;
        final GatewayObserver[] firstObservers;
        final GatewayObserver[] secondObservers;
        int length = 2;

        if (first instanceof CompositeGatewayObserver) {
            firstObservers = ((CompositeGatewayObserver) first).observers;
            length += firstObservers.length - 1;
        } else {
            firstObservers = null;
        }

        if (second instanceof CompositeGatewayObserver) {
            secondObservers = ((CompositeGatewayObserver) second).observers;
            length += secondObservers.length - 1;
        } else {
            secondObservers = null;
        }

        combinedObservers = new GatewayObserver[length];

        int pos;
        if (firstObservers != null) {
            pos = firstObservers.length;
            System.arraycopy(firstObservers, 0, combinedObservers, 0, pos);
        } else {
            pos = 1;
            combinedObservers[0] = first;
        }

        if (secondObservers != null) {
            System.arraycopy(secondObservers, 0, combinedObservers, pos, secondObservers.length);
        } else {
            combinedObservers[pos] = second;
        }

        return new CompositeGatewayObserver(combinedObservers);
    }
}
