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
package discord4j.gateway.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import discord4j.discordjson.json.gateway.*;
import discord4j.gateway.json.jackson.PayloadDeserializer;
import reactor.util.annotation.Nullable;

import java.util.Objects;

/**
 * Basic structure a Discord gateway payload object.
 *
 * @param <T> the type of the event object
 */
@JsonDeserialize(using = PayloadDeserializer.class)
public class GatewayPayload<T extends PayloadData> {

    @JsonSerialize(converter = OpcodeConverter.class)
    private Opcode<T> op;
    @JsonProperty("d")
    @Nullable
    private T data;
    @JsonProperty("s")
    @Nullable
    private Integer sequence;
    @JsonProperty("t")
    @Nullable
    private String type;

    public GatewayPayload(Opcode<T> op, @Nullable T data, @Nullable Integer sequence, @Nullable String type) {
        this.op = op;
        this.data = data;
        this.sequence = sequence;
        this.type = type;
    }

    public static GatewayPayload<Heartbeat> heartbeat(Heartbeat data) {
        return new GatewayPayload<>(Opcode.HEARTBEAT, data, null, null);
    }

    public static GatewayPayload<Identify> identify(Identify data) {
        return new GatewayPayload<>(Opcode.IDENTIFY, data, null, null);
    }

    public static GatewayPayload<StatusUpdate> statusUpdate(StatusUpdate data) {
        return new GatewayPayload<>(Opcode.STATUS_UPDATE, data, null, null);
    }

    public static GatewayPayload<VoiceStateUpdate> voiceStateUpdate(VoiceStateUpdate data) {
        return new GatewayPayload<>(Opcode.VOICE_STATE_UPDATE, data, null, null);
    }

    public static GatewayPayload<Resume> resume(Resume data) {
        return new GatewayPayload<>(Opcode.RESUME, data, null, null);
    }

    public static GatewayPayload<RequestGuildMembers> requestGuildMembers(RequestGuildMembers data) {
        return new GatewayPayload<>(Opcode.REQUEST_GUILD_MEMBERS, data, null, null);
    }

    public Opcode<T> getOp() {
        return op;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public Integer getSequence() {
        return sequence;
    }

    @Nullable
    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, data, sequence, type);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || obj.getClass() != GatewayPayload.class) {
            return false;
        }

        GatewayPayload<?> other = (GatewayPayload<?>) obj;

        return this.op == other.op
                && Objects.equals(this.data, other.data)
                && Objects.equals(this.sequence, other.sequence)
                && Objects.equals(this.type, other.type);
    }

    @Override
    public String toString() {
        return "GatewayPayload{" +
                "op=" + op +
                ", data=" + data +
                ", sequence=" + sequence +
                ", type='" + type + '\'' +
                '}';
    }
}
