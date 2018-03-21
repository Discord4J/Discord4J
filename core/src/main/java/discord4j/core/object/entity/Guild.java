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
package discord4j.core.object.entity;

import discord4j.core.Client;
import discord4j.core.Shard;
import discord4j.core.object.Presence;
import discord4j.core.object.Region;
import discord4j.core.object.Snowflake;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.bean.GuildBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A Discord guild.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/guild">Guild Resource</a>
 */
public final class Guild implements Entity {

    /** The Client associated to this object. */
    private final Client client;

    /** The raw data as represented by Discord. */
    private final GuildBean data;

    /**
     * Constructs an {@code Guild} with an associated client and Discord data.
     *
     * @param client The Client associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Guild(final Client client, final GuildBean data) {
        this.client = Objects.requireNonNull(client);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public Shard getShard() {
        return client.getShard();
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.getId());
    }

    /**
     * Gets the guild name.
     *
     * @return The guild name.
     */
    public String getName() {
        return data.getName();
    }

    /**
     * Gets the icon hash, if present.
     *
     * @return The icon hash, if present.
     */
    public Optional<String> getIconHash() {
        return Optional.ofNullable(data.getIcon());
    }

    /**
     * Gets the splash hash, if present.
     *
     * @return The splash hash, if present.
     */
    public Optional<String> getSplashHash() {
        return Optional.ofNullable(data.getSplash());
    }

    /**
     * Gets the ID of the owner of the guild.
     *
     * @return The ID of the owner of the guild.
     */
    public Snowflake getOwnerId() {
        return Snowflake.of(data.getOwnerId());
    }

    /**
     * Requests to retrieve the owner of the guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member owner} of the guild. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getOwner() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets the voice region ID for the guild.
     *
     * @return The voice region ID for the guild.
     */
    public String getRegionId() {
        return data.getRegion();
    }

    /**
     * Requests to retrieve the voice region for the guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits the voice {@link Region region} for the guild. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Region> getRegion() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets the ID of the AFK channel, if present.
     *
     * @return The ID of the AFK channel, if present.
     */
    public Optional<Snowflake> getAfkChannelId() {
        return Optional.ofNullable(data.getAfkChannelId()).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the AFK channel, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the AFK {@link VoiceChannel channel}, if present.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> getAfkChannel() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets the AFK timeout in seconds.
     *
     * @return The AFK timeout in seconds.
     */
    public int getAfkTimeout() {
        return data.getAfkTimeout();
    }

    /**
     * Gets the ID of the embedded channel, if present.
     *
     * @return The ID of the embedded channel, if present.
     */
    public Optional<Snowflake> getEmbedChannelId() {
        return Optional.ofNullable(data.getEmbedChannelId()).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the embedded channel, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the embedded {@link GuildChannel channel}, if
     * present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildChannel> getEmbedChannel() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets the level of verification required for the guild.
     *
     * @return The level of verification required for the guild.
     */
    public VerificationLevel getVerificationLevel() {
        return Arrays.stream(VerificationLevel.values())
                .filter(level -> level.value == data.getVerificationLevel())
                .findFirst() // If this throws Discord added something
                .orElseThrow(UnsupportedOperationException::new);
    }

    /**
     * Gets the default message notification level.
     *
     * @return The default message notification level.
     */
    public NotificationLevel getNotificationLevel() {
        return Arrays.stream(NotificationLevel.values())
                .filter(level -> level.value == data.getDefaultMessageNotifications())
                .findFirst() // If this throws Discord added something
                .orElseThrow(UnsupportedOperationException::new);
    }

    /**
     * Gets the default explicit content filter level.
     *
     * @return The default explicit content filter level.
     */
    public ContentFilterLevel getContentFilterLevel() {
        return Arrays.stream(ContentFilterLevel.values())
                .filter(level -> level.value == data.getExplicitContentFilter())
                .findFirst() // If this throws Discord added something
                .orElseThrow(UnsupportedOperationException::new);
    }

    /**
     * Gets the guild's roles' IDs.
     *
     * @return The guild's roles' IDs.
     */
    public Set<Snowflake> getRoleIds() {
        return Arrays.stream(data.getRoles())
                .mapToObj(Snowflake::of)
                .collect(Collectors.toSet());
    }

    /**
     * Requests to retrieve the guild's roles.
     *
     * @return A {@link Flux} that continually emits the guild's {@link Role roles}. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<Role> getRoles() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets the guild's emoji's IDs.
     *
     * @return The guild's emoji's IDs.
     */
    public Set<Snowflake> getEmojiIds() {
        return Arrays.stream(data.getEmojis())
                .mapToObj(Snowflake::of)
                .collect(Collectors.toSet());
    }

    /**
     * Requests to retrieve the guild's emojis.
     *
     * @return A {@link Flux} that continually emits guild's {@link GuildEmoji emojis}. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<GuildEmoji> getEmojis() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets the enabled guild features.
     *
     * @return The enabled guild features.
     */
    public Set<String> getFeatures() {
        return Arrays.stream(data.getFeatures()).collect(Collectors.toSet());
    }

    /**
     * Gets the required MFA level for the guild.
     *
     * @return The required MFA level for the guild.
     */
    public MfaLevel getMfaLevel() {
        return Arrays.stream(MfaLevel.values())
                .filter(level -> level.value == data.getMfaLevel())
                .findFirst() // If this throws Discord added something
                .orElseThrow(UnsupportedOperationException::new);
    }

    /**
     * Gets the application ID of the guild creator if it is bot-created.
     *
     * @return The application ID of the guild creator if it is bot-created.
     */
    public Optional<Snowflake> getApplicationId() {
        return Optional.ofNullable(data.getApplicationId()).map(Snowflake::of);
    }

    /**
     * Gets the channel ID for the server widget, if present.
     *
     * @return The channel ID for the server widget, if present.
     */
    public Optional<Snowflake> getWidgetChannelId() {
        return Optional.ofNullable(data.getWidgetChannelId()).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the channel for the server widget, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildChannel channel} for the server
     * widget, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildChannel> getWidgetChannel() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets the ID of the channel to which system messages are sent, if present.
     *
     * @return The ID of the channel to which system messages are sent, if present.
     */
    public Optional<Snowflake> getSystemChannelId() {
        return Optional.ofNullable(data.getSystemChannelId()).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the channel to which system messages are sent, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} to which system
     * messages are sent, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> getSystemChannel() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets when this guild was joined at, if present.
     *
     * @return When this guild was joined at, if present.
     */
    public Optional<Instant> getJoinTime() {
        return Optional.ofNullable(data.getJoinedAt()).map(Instant::parse);
    }

    /**
     * Gets whether this guild is considered large, if present.
     *
     * @return If present, {@code true} if the guild is considered large, {@code false} otherwise.
     */
    public Optional<Boolean> isLarge() {
        return Optional.ofNullable(data.getLarge());
    }

    /**
     * Gets the total number of members in the guild, if present.
     *
     * @return The total number of members in the guild, if present.
     */
    public OptionalInt getMemberCount() {
        final Integer count = data.getMemberCount();
        return (count == null) ? OptionalInt.empty() : OptionalInt.of(count);
    }

    /**
     * Requests to retrieve the voice states of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link VoiceState voice states} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<VoiceState> getVoiceStates() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Requests to retrieve the members of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link Member members} of the guild. If an error is received,
     * it is emitted through the {@code Flux}.
     */
    public Flux<Member> getMembers() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Requests to retrieve the channels of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link GuildChannel channels} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<GuildChannel> getChannels() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Requests to retrieve the presences of the guild.
     *
     * @return A {@link Flux} that continually emits the {@link Presence presences} of the guild. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<Presence> getPresences() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /** Automatically scan and delete messages sent in the server that contain explicit content. */
    public enum ContentFilterLevel {

        /** Don't scan any messages. */
        DISABLED(0),

        /** Scan messages from members without a role. */
        MEMBERS_WITHOUT_ROLES(1),

        /** Scan messages sent by all members. */
        ALL_MEMBERS(2);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code Guild.ContentFilterLevel}.
         *
         * @param value The underlying value as represented by Discord.
         */
        ContentFilterLevel(final int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * Prevent potentially dangerous administrative actions for users without two-factor authentication enabled. This
     * setting can only be changed by the server owner if they have 2FA enabled on their account.
     */
    public enum MfaLevel {

        /** Disabled 2FA requirement. */
        NONE(0),

        /** Enabled 2FA requirement. */
        ELEVATED(1);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code Guild.MfaLevel}.
         *
         * @param value The underlying value as represented by Discord.
         */
        MfaLevel(final int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * Determines whether {@link Member Members} who have not explicitly set their notification settings receive a
     * notification for every message sent in the server or not.
     */
    public enum NotificationLevel {

        /** Receive a notification for all messages. */
        ALL_MESSAGES(0),

        /** Receive a notification only for mentions. */
        ONLY_MENTIONS(1);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code Guild.NotificationLevel}.
         *
         * @param value The underlying value as represented by Discord.
         */
        NotificationLevel(final int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * {@link Member Members} of the server must meet the following criteria before they can send messages in text
     * channels or initiate a direct message conversation. If a member has an assigned role this does not apply.
     */
    public enum VerificationLevel {

        /** Unrestricted. */
        NONE(0),

        /** Must have verified email on account. */
        LOW(1),

        /** Must be registered on Discord for longer than 5 minutes. */
        MEDIUM(2),

        /** (╯°□°）╯︵ ┻━┻ - Must be a member of the server for longer than 10 minutes. */
        HIGH(3),

        /** ┻━┻ミヽ(ಠ益ಠ)ﾉ彡┻━┻ - Must have a verified phone number. */
        VERY_HIGH(4);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code Guild.VerificationLevel}.
         *
         * @param value The underlying value as represented by Discord.
         */
        VerificationLevel(final int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }
    }
}
