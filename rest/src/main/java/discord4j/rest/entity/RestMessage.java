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
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.rest.RestClient;
import discord4j.rest.util.MultipartRequest;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Represents a message within Discord.
 */
public class RestMessage {

    private final RestClient restClient;
    private final long channelId;
    private final long id;

    private RestMessage(RestClient restClient, long channelId, long id) {
        this.restClient = restClient;
        this.channelId = channelId;
        this.id = id;
    }

    /**
     * Create a {@link RestMessage} with the given parameters. This method does not perform any API request.
     *
     * @param restClient REST API resources
     * @param channelId the ID of the channel this messages belongs to
     * @param id the ID of this message
     * @return a {@code RestMessage} represented by the given parameters.
     */
    public static RestMessage create(RestClient restClient, Snowflake channelId, Snowflake id) {
        return new RestMessage(restClient, channelId.asLong(), id.asLong());
    }

    static RestMessage create(RestClient restClient, long channelId, long id) {
        return new RestMessage(restClient, channelId, id);
    }

    /**
     * Returns the ID of the channel this message belongs to.
     *
     * @return The ID of the channel this message belongs to
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Returns the ID of this message.
     *
     * @return The ID of this message
     */
    public Snowflake getId() {
        return Snowflake.of(id);
    }

    public RestChannel channel() {
        return RestChannel.create(restClient, channelId);
    }

    /**
     * Retrieve this messages' data upon subscription.
     *
     * @return a {@link Mono} where, upon successful completion, emits the {@link MessageData} belonging to this
     * channel. If an error is received, it is emitted through the {@code Mono}.
     * @see <a href="https://discord.com/developers/docs/resources/channel#get-channel-message">Get Message</a>
     */
    public Mono<MessageData> getData() {
        return restClient.getChannelService().getMessage(channelId, id);
    }

    /**
     * Requests to add a reaction on this message.
     *
     * @param emoji The reaction to add on this message. emoji takes the form of name:id for custom guild emoji, or
     * Unicode characters.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the reaction was added on
     * this message. If an error is received, it is emitted through the {@code Mono}.
     * @see <a href="https://discord.com/developers/docs/resources/channel#create-reaction">Create Reaction</a>
     */
    public Mono<Void> createReaction(String emoji) {
        return restClient.getChannelService().createReaction(channelId, id, emoji);
    }

    /**
     * Requests to remove a reaction from the current user on this message.
     *
     * @param emoji The reaction to remove on this message.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the reaction from the current
     * user was removed on this message. If an error is received, it is emitted through the {@code Mono}.
     * @see <a href="https://discord.com/developers/docs/resources/channel#delete-own-reaction">Delete Own Reaction</a>
     */
    public Mono<Void> deleteOwnReaction(String emoji) {
        return restClient.getChannelService().deleteOwnReaction(channelId, id, emoji);
    }

    /**
     * Requests to remove a reaction from a specified user on this message.
     *
     * @param emoji The reaction to remove on this message.
     * @param userId The user to remove the reaction on this message.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the reaction from the
     * specified user was removed on this message. If an error is received, it is emitted through the {@code Mono}.
     * @see
     * <a href="https://discord.com/developers/docs/resources/channel#delete-user-reaction">Delete User Reaction</a>
     */
    public Mono<Void> deleteUserReaction(String emoji, Snowflake userId) {
        return restClient.getChannelService().deleteReaction(channelId, id, emoji, userId.asLong());
    }

    /**
     * Requests to remove all the reactions on this message.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating all the reactions on this
     * message were removed. If an error is received, it is emitted through the {@code Mono}.
     * @see
     * <a href="https://discord.com/developers/docs/resources/channel#delete-all-reactions">Delete All Reactions</a>
     */
    public Mono<Void> deleteAllReactions() {
        return restClient.getChannelService().deleteAllReactions(channelId, id);
    }

    /**
     * Requests to remove a specified reaction on this message.
     *
     * @param emoji The reaction to remove on this message.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the reaction from the
     * specified user was removed on this message. If an error is received, it is emitted through the {@code Mono}.
     * @see
     * <a href="https://discord.com/developers/docs/resources/channel#delete-all-reactions-for-emoji">Delete All Reactions for Emoji</a>
     */
    public Mono<Void> deleteReactions(String emoji) {
        return restClient.getChannelService().deleteReactions(channelId, id, emoji);
    }

    /**
     * Requests to edit this message.
     *
     * @param request The request body used to create a new message.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link MessageData}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @see <a href="https://discord.com/developers/docs/resources/channel#edit-message">Edit Message</a>
     */
    public Mono<MessageData> edit(MessageEditRequest request) {
        return restClient.getChannelService().editMessage(channelId, id, MultipartRequest.ofRequest(request));
    }

    /**
     * Requests to delete this message while optionally specifying a reason.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the message has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     * @see <a href="https://discord.com/developers/docs/resources/channel#delete-message">Delete Message</a>
     */
    public Mono<Void> delete(@Nullable String reason) {
        return restClient.getChannelService().deleteMessage(channelId, id, reason);
    }

    /**
     * Requests to publish (crosspost) this message if the {@code channel} is of type 'news'.
     * Requires 'SEND_MESSAGES' permission if the current user sent the message, or additionally the 'MANAGE_MESSAGES' permission, for all other messages, to be present for the current user.
     *
     * @return A {@link Mono} where, upon successful completion, emits the published {@link MessageData}
     * (crossposted) in the guilds. If an error is received, it is emitted through the {@code Mono}.
     * @see <a href="https://discord.com/developers/docs/resources/channel#crosspost-message">Crosspost Message</a>
     */
    public Mono<MessageData> publish() {
        return restClient.getChannelService().publishMessage(channelId, id);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RestMessage that = (RestMessage) o;
        return channelId == that.channelId && id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, id);
    }
}
