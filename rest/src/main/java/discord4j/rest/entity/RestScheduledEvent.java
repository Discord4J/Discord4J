/*
 *  This file is part of Discord4J.
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
import discord4j.discordjson.json.GuildScheduledEventData;
import discord4j.discordjson.json.GuildScheduledEventModifyRequest;
import discord4j.discordjson.json.GuildScheduledEventUserData;
import discord4j.rest.RestClient;
import discord4j.rest.util.PaginationUtil;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a guild scheduled event within Discord.
 */
public class RestScheduledEvent {

    private final RestClient restClient;

    private final long guildId;

    private final long id;

    private RestScheduledEvent(RestClient restClient, long guildId, long id) {
        this.restClient = restClient;
        this.guildId = guildId;
        this.id = id;
    }

    /**
     * Create a {@link RestScheduledEvent} for the given parameters. This method does not perform any API request.
     *
     * @param restClient The client to make API requests.
     * @param guildId The ID of the guild this entity belongs to.
     * @param id the ID of this entity.
     * @return A {@code RestGuildScheduledEvent} represented by the given parameters.
     */
    public static RestScheduledEvent create(RestClient restClient, Snowflake guildId, Snowflake id) {
        return new RestScheduledEvent(restClient, guildId.asLong(), id.asLong());
    }

    static RestScheduledEvent create(RestClient restClient, long guildId, long id) {
        return new RestScheduledEvent(restClient, guildId, id);
    }

    /**
     * Returns the ID of the guild this scheduled event belongs to.
     *
     * @return The ID of the guild this scheduled event belongs to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Returns the ID of this scheduled event.
     *
     * @return The ID of this scheduled event.
     */
    public Snowflake getId() {
        return Snowflake.of(id);
    }

    /**
     * Return the guild tied to thi role as a REST operations handle.
     *
     * @return The parent guild for this scheduled event.
     */
    public RestGuild guild() {
        return RestGuild.create(restClient, guildId);
    }

    /**
     * Requests to edit this event.
     *
     * @param request A {@link GuildScheduledEventModifyRequest} to parameterize this request.
     * @param reason The reason, if present
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link GuildScheduledEventData}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildScheduledEventData> edit(final GuildScheduledEventModifyRequest request, @Nullable String reason) {
        return restClient.getGuildService().modifyScheduledEvent(guildId,id, request, reason);
    }

    /**
     * Requests to delete this event while optionally specifying the reason.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the event was deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(@Nullable final String reason) {
        return restClient.getGuildService().deleteScheduledEvent(guildId, id, reason);
    }

    /**
     * Request to retrieve <i>all</i> subscribed users <i>before</i> the specified ID.
     * <p>
     * The returned {@code Flux} will emit items in <i>reverse-</i>chronological order (newest to oldest). It is
     * recommended to limit the emitted items by invoking either {@link Flux#takeWhile(Predicate)} (to retrieve IDs
     * within a specified range) or {@link Flux#take(long)} (to retrieve a specific amount of IDs).
     * <p>
     * The following example will get <i>all</i> users from {@code userId} to {@code myOtherUserId}:
     * {@code getSubscribedUsersBefore(userId).takeWhile(user -> user.getId().compareTo(myOtherUserId) >= 0)}
     *
     * @param userId The ID of the <i>newest</i> user to retrieve.
     * @param withMember Whether to optionally include the member object in the returned data (if the user is a member).
     * @return A {@link Flux} that continually emits <i>all</i> {@link GuildScheduledEventUserData users} <i>before</i>
     * the specified ID. If an error is received, it is emitted through the {@code Flux}.
     * @see
     * <a href="https://discord.com/developers/docs/resources/guild-scheduled-event#get-guild-scheduled-event-users">
     * Get Guild Scheduled Event Users</a>
     */
    public Flux<GuildScheduledEventUserData> getSubscribedUsersBefore(Snowflake userId, @Nullable Boolean withMember) {
        Function<Map<String, Object>, Flux<GuildScheduledEventUserData>> doRequest = params -> {
            Optional.ofNullable(withMember).ifPresent(value -> params.put("with_member", value));
            return restClient.getGuildService().getScheduledEventUsers(guildId, id, params);
        };
        return PaginationUtil.paginateBefore(doRequest, data -> Snowflake.asLong(data.user().id()), userId.asLong(), 100);
    }

    /**
     * Request to retrieve <i>all</i> subscribed users <i>after</i> the specified ID.
     * <p>
     * The returned {@code Flux} will emit items in chronological order (older to newest). It is recommended to limit
     * the emitted items by invoking either {@link Flux#takeWhile(Predicate)} (to retrieve IDs within a specified range)
     * or {@link Flux#take(long)} (to retrieve a specific amount of IDs).
     * <p>
     * The following example will get <i>all</i> users from {@code userId} to {@code myOtherUserId}:
     * {@code getSubscribedUsersAfter(userId).takeWhile(user -> user.getId().compareTo(myOtherUserId) <= 0)}
     *
     * @param userId The ID of the <i>oldest</i> user to retrieve.
     * @param withMember Whether to optionally include the member object in the returned data (if the user is a member).
     * @return A {@link Flux} that continually emits <i>all</i> {@link GuildScheduledEventUserData users} <i>after</i>
     * the specified ID. If an error is received, it is emitted through the {@code Flux}.
     * @see
     * <a href="https://discord.com/developers/docs/resources/guild-scheduled-event#get-guild-scheduled-event-users">
     * Get Guild Scheduled Event Users</a>
     */
    public Flux<GuildScheduledEventUserData> getSubscribedUsersAfter(Snowflake userId, @Nullable Boolean withMember) {
        Function<Map<String, Object>, Flux<GuildScheduledEventUserData>> doRequest = params -> {
            Optional.ofNullable(withMember).ifPresent(value -> params.put("with_member", value));
            return restClient.getGuildService().getScheduledEventUsers(guildId, id, params);
        };
        return PaginationUtil.paginateAfter(doRequest, data -> Snowflake.asLong(data.user().id()), userId.asLong(), 100);
    }

    /**
     * Retrieve this scheduled event's data upon subscription.
     *
     * @param withUserCount Whether to optionally include the subscribed user count in the returned data.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildScheduledEventData} belonging to
     * this scheduled event. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildScheduledEventData> getData(@Nullable Boolean withUserCount) {
        Map<String, Object> queryParams = new HashMap<>();
        Optional.ofNullable(withUserCount).ifPresent(value -> queryParams.put("with_user_count", value));
        return restClient.getGuildService().getScheduledEvent(guildId, id, queryParams);
    }
}
