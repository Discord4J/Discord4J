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

package discord4j.voice.retry;

import discord4j.voice.VoiceConnection;

import java.time.Duration;

public class VoiceGatewayRetrySignal {

    private final Throwable failure;
    private final long iteration;
    private final Duration nextBackoff;
    private final VoiceConnection.State nextState;

    public VoiceGatewayRetrySignal(Throwable failure, long iteration, Duration nextBackoff,
                                   VoiceConnection.State nextState) {
        this.failure = failure;
        this.iteration = iteration;
        this.nextBackoff = nextBackoff;
        this.nextState = nextState;
    }

    public Throwable failure() {
        return failure;
    }

    public long iteration() {
        return iteration;
    }

    public Duration nextBackoff() {
        return nextBackoff;
    }

    public VoiceConnection.State nextState() {
        return nextState;
    }

    @Override
    public String toString() {
        return "GatewayRetrySignal{" +
                "failure=" + failure +
                ", iteration=" + iteration +
                ", nextBackoff=" + nextBackoff +
                ", nextState=" + nextState +
                '}';
    }
}
