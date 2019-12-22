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

import com.darichey.discordjson.json.gateway.PayloadData;
import com.darichey.discordjson.json.gateway.RequestGuildMembers;
import com.darichey.discordjson.json.gateway.StatusUpdate;
import com.darichey.discordjson.json.gateway.VoiceStateUpdate;
import discord4j.gateway.GatewayClient;

/**
 * Represents a unicast {@link GatewayPayload} meant to execute a Gateway operation targeting a single shard. The
 * routing information is contained in {@link #getShardIndex()} and can be read by {@link GatewayClient}
 * implementations.
 *
 * @param <T> the type of the event object
 */
public class ShardGatewayPayload<T extends PayloadData> extends GatewayPayload<T> {

    private final int shardIndex;

    public ShardGatewayPayload(GatewayPayload<T> payload, int shardIndex) {
        super(payload.getOp(), payload.getData(), payload.getSequence(), payload.getType());
        this.shardIndex = shardIndex;
    }

    public static ShardGatewayPayload<StatusUpdate> statusUpdate(StatusUpdate data, int shardId) {
        return new ShardGatewayPayload<>(GatewayPayload.statusUpdate(data), shardId);
    }

    public static ShardGatewayPayload<VoiceStateUpdate> voiceStateUpdate(VoiceStateUpdate data, int shardId) {
        return new ShardGatewayPayload<>(GatewayPayload.voiceStateUpdate(data), shardId);
    }

    public static ShardGatewayPayload<RequestGuildMembers> requestGuildMembers(RequestGuildMembers data, int shardId) {
        return new ShardGatewayPayload<>(GatewayPayload.requestGuildMembers(data), shardId);
    }

    /**
     * Return the shard index this payload is targeted at.
     *
     * @return a 0-based shard index
     */
    public int getShardIndex() {
        return shardIndex;
    }
}
