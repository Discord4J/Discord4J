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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.voice.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class VoiceGatewayPayload<T> {

    private final int op;
    @JsonProperty("d")
    private final T data;

    @JsonCreator
    public VoiceGatewayPayload(@JsonProperty("op") int op, @JsonProperty("d") T data) {
        this.op = op;
        this.data = data;
    }

    public int getOp() {
        return op;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "VoiceGatewayPayload{" +
                "op=" + op +
                ", data=" + data +
                '}';
    }
}
