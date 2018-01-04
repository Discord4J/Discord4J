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
import discord4j.core.trait.Deletable;
import discord4j.core.trait.Renameable;
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
public interface Guild extends Deletable, Entity, Renameable<Guild> {

	String getIconHash();
	String getSplashHash();
	Snowflake getOwnerId();
	Mono<Member> getOwner();
	String getRegionId();
	Mono<Region> getRegion();
	Optional<Snowflake> getAfkChannelId();
	Mono<VoiceChannel> getAfkChannel();
	int getAfkTimeout();
	boolean isEmbedEnabled();
	Optional<Snowflake> getEmbedChannelId();
	Mono<GuildChannel<?>> getEmbedChannel();
	VerificationLevel getVerificationLevel();
	NotificationLevel getNotificationLevel();
	ContentFilterLevel getContentFilterLevel();
	Set<Role> getRoles();
	Set<GuildEmoji> getEmojis();
	Set<String> getFeatures();
	MfaLevel getMfaLevel();
	Optional<Snowflake> getApplicationId();
	boolean isWidgetEnabled();
	Optional<Snowflake> getWidgetChannelId();
	Mono<GuildChannel<?>> getWidgetChannel();
	Optional<Snowflake> getSystemChannelId();
	Mono<TextChannel> getSystemChannel();

	// TODO: Note for Panda
	// The reason these are Monos is because this data isn't always provided (in fact, it's never provided except for
	// create). So when, for say, we retrieve a Guild object from REST, we need to point towards a "GuildCreateData"
	// *store*, not the data itself. So all these fields below have to come from a cache, not from an already given
	// data object (because that is not provided 99% of the time)
	Mono<Instant> getJoinTime();
	Mono<Boolean> isLarge();
	Mono<Boolean> isAvailable();
	Mono<Integer> getMemberCount();
	Flux<VoiceState> getVoiceStates();
	Flux<Member> getMembers();
	Flux<GuildChannel<?>> getChannels();
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
		 * @param value THe underlying value as represented by Discord.
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
