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

import discord4j.core.object.Presence;
import discord4j.core.object.Region;
import discord4j.core.object.Snowflake;
import discord4j.core.object.VoiceState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * A Discord guild.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/guild">Guild Resource</a>
 */
public interface Guild extends Entity {

	/**
	 * Gets the guild name.
	 *
	 * @return The guild name.
	 */
	String getName();

	/**
	 * Gets the icon hash.
	 *
	 * @return The icon hash.
	 */
	String getIconHash();

	/**
	 * Gets the splash hash.
	 *
	 * @return The splash hash.
	 */
	String getSplashHash();

	/**
	 * Gets the ID of the owner of the guild.
	 *
	 * @return The ID of the owner of the guild.
	 */
	Snowflake getOwnerId();

	/**
	 * Requests to retrieve the owner of the guild.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link Member owner} of the guild. If an
	 * error is received, it is emitted through the {@code Mono}.
	 */
	Mono<Member> getOwner();

	/**
	 * Gets the voice region ID for the guild.
	 *
	 * @return The voice region ID for the guild.
	 */
	String getRegionId();

	/**
	 * Requests to retrieve the voice region for the guild.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the voice {@link Region region} for the guild. If
	 * an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<Region> getRegion();

	/**
	 * Gets the ID of the AFK channel, if present.
	 *
	 * @return The ID of the AFK channel, if present.
	 */
	Optional<Snowflake> getAfkChannelId();

	/**
	 * Requests to retrieve the AFK channel, if present.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the AFK {@link VoiceChannel channel}, if present.
	 * If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<VoiceChannel> getAfkChannel();

	/**
	 * Gets the AFK timeout in seconds.
	 *
	 * @return The AFK timeout in seconds.
	 */
	int getAfkTimeout();

	/**
	 * Gets the ID of the embedded channel, if present.
	 *
	 * @return The ID of the embedded channel, if present.
	 */
	Optional<Snowflake> getEmbedChannelId();

	/**
	 * Requests to retrieve the embedded channel, if present.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the embedded {@link GuildChannel channel}, if
	 * present. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<GuildChannel> getEmbedChannel();

	/**
	 * Gets the level of verification required for the guild.
	 *
	 * @return The level of verification required for the guild.
	 */
	VerificationLevel getVerificationLevel();

	/**
	 * Gets the default message notification level.
	 *
	 * @return The default message notification level.
	 */
	NotificationLevel getNotificationLevel();

	/**
	 * Gets the default explicit content filter level.
	 *
	 * @return The default explicit content filter level.
	 */
	ContentFilterLevel getContentFilterLevel();

	/**
	 * Gets the guild's roles' IDs.
	 *
	 * @return The guild's roles' IDs.
	 */
	Set<Snowflake> getRoleIds();

	/**
	 * Requests to retrieve the guild's roles.
	 *
	 * @return A {@link Flux} that continually emits the guild's {@link Role roles}. If an error is received, it is
	 * emitted through the {@code Flux}.
	 */
	Flux<Role> getRoles();

	/**
	 * Gets the guild's emoji's IDs.
	 *
	 * @return The guild's emoji's IDs.
	 */
	Set<Snowflake> getEmojiIds();

	/**
	 * Requests to retrieve the guild's emojis.
	 *
	 * @return A {@link Flux} that continually emits guild's {@link GuildEmoji emojis}. If an error is received, it is
	 * emitted through the {@code Flux}.
	 */
	Flux<GuildEmoji> getEmojis();

	/**
	 * Gets the enabled guild features.
	 *
	 * @return The enabled guild features.
	 */
	Set<String> getFeatures();

	/**
	 * Gets the required MFA level for the guild.
	 *
	 * @return The required MFA level for the guild.
	 */
	MfaLevel getMfaLevel();

	/**
	 * Gets the application ID of the guild creator if it is bot-created.
	 *
	 * @return The application ID of the guild creator if it is bot-created.
	 */
	Optional<Snowflake> getApplicationId();

	/**
	 * Gets the channel ID for the server widget, if present.
	 *
	 * @return The channel ID for the server widget, if present.
	 */
	Optional<Snowflake> getWidgetChannelId();

	/**
	 * Requests to retrieve the channel for the server widget, if present.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link GuildChannel channel} for the server
	 * widget, if present. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<GuildChannel> getWidgetChannel();

	/**
	 * Gets the ID of the channel to which system messages are sent, if present.
	 *
	 * @return The ID of the channel to which system messages are sent, if present.
	 */
	Optional<Snowflake> getSystemChannelId();

	/**
	 * Requests to retrieve the channel to which system messages are sent, if present.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} to which system
	 * messages are sent, if present. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<TextChannel> getSystemChannel();

	/**
	 * Gets when this guild was joined at, if present.
	 *
	 * @return When this guild was joined at, if present.
	 */
	Optional<Instant> getJoinTime();

	/**
	 * Gets whether this guild is considered large, if present.
	 *
	 * @return If present, {@code true} if the guild is considered large, {@code false} otherwise.
	 */
	Optional<Boolean> isLarge();

	/**
	 * Gets the total number of members in the guild, if present.
	 *
	 * @return The total number of members in the guild, if present.
	 */
	Optional<Integer> getMemberCount();

	/**
	 * Requests to retrieve the voice states of the guild.
	 *
	 * @return A {@link Flux} that continually emits the {@link VoiceState voice states} of the guild. If an error is
	 * received, it is emitted through the {@code Flux}.
	 */
	Flux<VoiceState> getVoiceStates();

	/**
	 * Requests to retrieve the members of the guild.
	 *
	 * @return A {@link Flux} that continually emits the {@link Member members} of the guild. If an error is received,
	 * it is emitted through the {@code Flux}.
	 */
	Flux<Member> getMembers();

	/**
	 * Requests to retrieve the channels of the guild.
	 *
	 * @return A {@link Flux} that continually emits the {@link GuildChannel channels} of the guild. If an error is
	 * received, it is emitted through the {@code Flux}.
	 */
	Flux<GuildChannel> getChannels();

	/**
	 * Requests to retrieve the presences of the guild.
	 *
	 * @return A {@link Flux} that continually emits the {@link Presence presences} of the guild. If an error is
	 * received, it is emitted through the {@code Flux}.
	 */
	Flux<Presence> getPresences();

	/** Automatically scan and delete messages sent in the server that contain explicit content. */
	enum ContentFilterLevel {

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
	enum MfaLevel {

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
	enum NotificationLevel {

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
	enum VerificationLevel {

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
