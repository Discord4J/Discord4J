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
package discord4j.core.event.domain;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.StageInstance;
import discord4j.gateway.ShardInfo;

/**
 * Dispatched when a {@link StageInstance} is created.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#stage-instance-create">Stage Instance Create</a>
 */
public class StageInstanceCreateEvent extends Event {

    private final StageInstance stageInstance;

    public StageInstanceCreateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, final StageInstance stageInstance) {
        super(gateway, shardInfo);
        this.stageInstance = stageInstance;
    }

    /**
     * Get the created {@link StageInstance}
     *
     * @return The created {@link StageInstance}
     */
    public StageInstance getStageInstance() {
        return stageInstance;
    }

    @Override
    public String toString() {
        return "StageInstanceCreateEvent{" +
                "stageInstance=" + stageInstance +
                '}';
    }
}
