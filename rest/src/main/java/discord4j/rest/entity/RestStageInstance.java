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

package discord4j.rest.entity;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.StageInstanceData;
import discord4j.discordjson.json.StageInstanceModifyRequest;
import discord4j.rest.RestClient;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

/**
 * Represents a stage instance entity in Discord.
 */
public class RestStageInstance {

    private final RestClient restClient;
    private final long channelId;

    private RestStageInstance(RestClient restClient, long channelId) {
        this.restClient = restClient;
        this.channelId = channelId;
    }

    /**
     * Create a {@link RestStageInstance} for a given channel ID. This method does not perform any API request.
     *
     * @param restClient the client to make API requests
     * @param channelId the channel ID of this entity
     * @return a {@link RestStageInstance} represented by this {@param channelId}.
     */
    public static RestStageInstance create(RestClient restClient, Snowflake channelId) {
        return new RestStageInstance(restClient, channelId.asLong());
    }

    static RestStageInstance create(RestClient restClient, long channelId) {
        return new RestStageInstance(restClient, channelId);
    }

    /**
     * Returns the channel ID.
     *
     * @return The channel ID of this stage instance
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Retrieve this stage instance's data upon subscription.
     *
     * @return a {@link Mono} where, upon successful completion, emits the {@link StageInstanceData} belonging to this stage instance.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<StageInstanceData> getData() {
        return restClient.getStageInstanceService().getStageInstance(channelId);
    }

    /**
     * Requests to edit this stage instance.
     *
     * @param request A {@link StageInstanceModifyRequest} to parameterize this request.
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link StageInstanceData}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<StageInstanceData> edit(final StageInstanceModifyRequest request, @Nullable String reason) {
        return restClient.getStageInstanceService().modifyStageInstance(channelId, request, reason);
    }

}
