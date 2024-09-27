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

package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.ScheduledEventEntityMetadata;
import discord4j.core.object.RecurrenceRule;
import discord4j.core.object.ScheduledEventUser;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.spec.ScheduledEventEditMono;
import discord4j.core.spec.ScheduledEventEditSpec;
import discord4j.core.util.ImageUtil;
import discord4j.discordjson.json.GuildScheduledEventData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A Discord Guild Scheduled Event
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild-scheduled-event">Guild Scheduled Event Resource</a>
 */
public class ScheduledEvent implements Entity {

    private static final String EVENT_COVER_IMAGE_PATH = "guild-events/%s/%s";

    /**
     * The gateway associated to this object
     */
    private final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final GuildScheduledEventData data;

    /**
     * Constructs a {@code ScheduledEvent} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ScheduledEvent(final GatewayDiscordClient gateway, final GuildScheduledEventData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the data of the scheduled event.
     *
     * @return The data of the scheduled event.
     */
    public GuildScheduledEventData getData() {
        return data;
    }

    /**
     * Gets the guild ID of the scheduled event.
     *
     * @return The guild ID of the scheduled event.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(data.guildId());
    }

    /**
     * Gets the ID of the creator of the event.
     *
     * @return The ID of the creator of the event.
     */
    public Optional<Snowflake> getCreatorId() {
        return Possible.flatOpt(data.creatorId()).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the creator if this event, if present.
     *
     * @return A {@link Mono} where, if the creator is present, emits the {@link Member creator} of the event,
     * otherwise emits an {@link Mono#empty() empty mono}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getCreator() {
        return Mono.justOrEmpty(getCreatorId()).flatMap(id -> gateway.getMemberById(getGuildId(), id));
    }

    /**
     * Gets the ID of the channel where the event will be hosted, if present.
     *
     * @return The ID of the channel where the event will be hosted, if present.
     */
    public Optional<Snowflake> getChannelId() {
        return data.channelId().map(Snowflake::of);
    }

    /**
     * Requests to retrieve the channel this event will be hosted in, if present.
     * <p>
     * Note: This channel could be a stage or voice channel, see {@link #getEntityType()} to determine the type safely.
     *
     * @return A {@link Mono} where, if the channel is present, emits the {@link GuildChannel channel} this event will
     * be hosted in, otherwise emits an {@link Mono#empty() empty mono}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildChannel> getChannel() {
        return Mono.justOrEmpty(getChannelId())
                .flatMap(gateway::getChannelById)
                .ofType(GuildChannel.class);
    }

    /**
     * Return the name of this scheduled event.
     *
     * @return the scheduled event name
     */
    public String getName() {
        return data.name();
    }

    /**
     * Return the description of this scheduled event.
     *
     * @return the scheduled event description, if present
     */
    public Optional<String> getDescription() {
        return Possible.flatOpt(data.description());
    }

    /**
     * Gets the scheduled start time of the event.
     *
     * @return The scheduled start time of the event.
     */
    public Instant getStartTime() {
        return data.scheduledStartTime();
    }

    /**
     * Gets the scheduled end time of the event, if present.
     * <p>
     * Note: This metadata will always be present when the entity type is {@link EntityType#EXTERNAL external}.
     *
     * @return The scheduled end time of the event, if present.
     */
    public Optional<Instant> getEndTime() {
        return data.scheduledEndTime();
    }

    /**
     * Gets the privacy level of the event
     *
     * @return The privacy level of the event
     */
    public PrivacyLevel getPrivacyLevel() {
        return PrivacyLevel.of(data.privacyLevel());
    }

    /**
     * Gets the status of the event.
     *
     * @return The status of the event.
     */
    public Status getStatus() {
        return Status.of(data.status());
    }

    /**
     * Returns the cover image URL of this scheduled event, if present.
     *
     * @param format the format for the URL
     * @return the cover image URL, if present.
     */
    public Optional<String> getImageUrl(Image.Format format) {
        return Possible.flatOpt(data.image())
                .map(hash -> ImageUtil.getUrl(String.format(EVENT_COVER_IMAGE_PATH, getId().asString(), hash), format));
    }

    /**
     * Gets the entity type of the event.
     *
     * @return The entity type of the event.
     */
    public EntityType getEntityType() {
        return EntityType.of(data.entityType());
    }

    /**
     * Gets the ID of the entity this event will be hosted in, if present.
     * <p>
     * Note: This property currently matches {@link #getChannelId()}, it is believed this is available for future
     * flexibility for Discord and should not be relied on.
     *
     * @return The ID of the entity this event will be hosted in, if present.
     */
    public Optional<Snowflake> getEntityId() {
        return getData().entityId().map(Snowflake::of);
    }

    /**
     * Gets the entity metadata of the event, if present.
     * <p>
     * Note: This metadata will always be present when the entity type is {@link EntityType#EXTERNAL external}.
     *
     * @return The entity metadata of the event, if present.
     */
    public Optional<ScheduledEventEntityMetadata> getEntityMetadata() {
        return data.entityMetadata().map(data -> new ScheduledEventEntityMetadata(gateway, data));
    }

    /**
     * Gets the location of the event, if present.
     * <p>
     * Note: This location is pulled from {@link ScheduledEventEntityMetadata#getLocation} if present.
     *
     * @return The location of the event, if present.
     */
    public Optional<String> getLocation() {
        return getEntityMetadata().flatMap(ScheduledEventEntityMetadata::getLocation);
    }

    /**
     * Gets the recurrence rule of the event, if present.
     *
     * @return The recurrence rule of the event, if present.
     */
    public Optional<RecurrenceRule> getRecurrenceRule() {
        return this.data.recurrenceRule().map(RecurrenceRule::new);
    }

    /**
     * Gets the count of users who have subscribed to the event, if present.
     *
     * @return The count of users who have subscribed to the event, if present.
     */
    public Optional<Integer> getSubscribedUserCount() {
        return data.userCount().toOptional();
    }

    /**
     * Requests to retrieve the users subscribed to this event.
     *
     * @param withMemberData requests to return member data along with user data
     * @return A {@link Flux} that continually emits <i>all</i> {@link ScheduledEventUser users} <i>after</i>
     * the specified ID. If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<ScheduledEventUser> getSubscribedUsers(boolean withMemberData) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("with_member", withMemberData);

        return gateway.getRestClient().getGuildService().getScheduledEventUsers(getGuildId().asLong(),
                        getId().asLong(), queryParams)
                .map(data -> new ScheduledEventUser(gateway, data, getGuildId()));
    }

    /**
     * Requests to retrieve the users subscribed to this event <li>before</li> the given user ID.
     *
     * @param userId the ID of the <li>newest</li> user to retrieve
     * @param withMemberData requests to return member data along with user data
     * @return A {@link Flux} that continually emits <i>all</i> {@link ScheduledEventUser users} <i>after</i>
     * the specified ID. If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<ScheduledEventUser> getSubscribedUsersBefore(Snowflake userId, boolean withMemberData) {
        return gateway.getRestClient().getScheduledEventById(getGuildId(), getId())
                .getSubscribedUsersBefore(userId, withMemberData)
                .map(data -> new ScheduledEventUser(gateway, data, getGuildId()));
    }

    /**
     * Requests to retrieve the users subscribed to this event <li>after</li> the given user ID.
     *
     * @param userId the ID of the <li>oldest</li> user to retrieve
     * @param withMemberData requests to return member data along with user data
     * @return A {@link Flux} that continually emits <i>all</i> {@link ScheduledEventUser users} <i>after</i>
     * the specified ID. If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<ScheduledEventUser> getSubscribedUsersAfter(Snowflake userId, boolean withMemberData) {
        return gateway.getRestClient().getScheduledEventById(getGuildId(), getId())
                .getSubscribedUsersAfter(userId, withMemberData)
                .map(data -> new ScheduledEventUser(gateway, data, getGuildId()));
    }

    /**
     * Requests to edit this scheduled event
     *
     * @return A {@link Mono} which, upon completion, emits the new {@link ScheduledEvent event} update. If an error
     * occurs, it is emitted through the {@link Mono}.
     */
    public ScheduledEventEditMono edit() {
        return ScheduledEventEditMono.of(this);
    }

    /**
     * Requests to edit this scheduled event with the provided spec.
     *
     * @param spec the parameters to update
     * @return A {@link Mono} which, upon completion, emits the new {@link ScheduledEvent event} update. If an error
     * occurs, it is emitted through the {@link Mono}.
     */
    public Mono<ScheduledEvent> edit(ScheduledEventEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(() -> gateway.getRestClient().getScheduledEventById(getGuildId(), getId()).edit(spec.asRequest(), spec.reason())
                .map(data -> new ScheduledEvent(gateway, data)));
    }

    /**
     * Requests to delete this event with the provided reason.
     *
     * @param reason the reason explaining why this event is being deleted
     * @return A {@link Mono} which completes when the event is deleted.
     */
    public Mono<Void> delete(@Nullable String reason) {
        return gateway.getRestClient().getScheduledEventById(getGuildId(), getId())
                .delete(reason);
    }

    /**
     * Requests to delete this event.
     *
     * @return A {@link Mono} which completes when the event is deleted.
     */
    public Mono<Void> delete() {
        return delete(null);
    }

    /**
     * Represents a scheduled event's privacy level.
     */
    public enum PrivacyLevel {
        UNKNOWN(-1),
        GUILD_ONLY(2);

        private final int value;

        PrivacyLevel(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static PrivacyLevel of(int value) {
            switch (value) {
                case 2:
                    return GUILD_ONLY;
                default:
                    return UNKNOWN;
            }
        }
    }

    /**
     * Represents a scheduled event's entity type
     */
    public enum EntityType {
        UNKNOWN(-1),
        STAGE_INSTANCE(1),
        VOICE(2),
        EXTERNAL(3);

        private final int value;

        EntityType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static EntityType of(int value) {
            switch (value) {
                case 1:
                    return STAGE_INSTANCE;
                case 2:
                    return VOICE;
                case 3:
                    return EXTERNAL;
                default:
                    return UNKNOWN;
            }
        }
    }

    /**
     * Represents a scheduled event's status
     */
    public enum Status {
        UNKNOWN(-1),
        SCHEDULED(1),
        ACTIVE(2),
        COMPLETED(3),
        CANCELED(4);

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Status of(int value) {
            switch (value) {
                case 1:
                    return SCHEDULED;
                case 2:
                    return ACTIVE;
                case 3:
                    return COMPLETED;
                case 4:
                    return CANCELED;
                default:
                    return UNKNOWN;
            }
        }
    }


}
