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

import discord4j.common.json.response.WebhookResponse;
import discord4j.core.Client;
import discord4j.core.object.Snowflake;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

/**
 * A Discord webhook.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/webhook">Webhook Resource</a>
 */
public final class Webhook implements Entity {

	/** The Client associated to this object. */
	private final Client client;

	/** The raw data as represented by Discord. */
	private final WebhookResponse webhook;

	/**
	 * Constructs a {@code Webhook} with an associated client and Discord data.
	 *
	 * @param client The Client associated to this object, must be non-null.
	 * @param webhook The raw data as represented by Discord, must be non-null.
	 */
	public Webhook(final Client client, final WebhookResponse webhook) {
		this.client = Objects.requireNonNull(client);
		this.webhook = Objects.requireNonNull(webhook);
	}

	@Override
	public Client getClient() {
		return client;
	}

	@Override
	public Snowflake getId() {
		return Snowflake.of(webhook.getId());
	}

	/**
	 * Gets the ID of the guild this webhook is associated to.
	 *
	 * @return The ID of the guild this webhook is associated to.
	 */
	public Snowflake getGuildId() {
		return Snowflake.of(webhook.getGuildId());
	}

	/**
	 * Requests to retrieve the guild this webhook is associated to.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this webhook is
	 * associated to. If an error is received, it is emitted through the {@code Mono}.
	 */
	public Mono<Guild> getGuild() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}

	/**
	 * Gets the ID of the channel this webhook is associated to.
	 *
	 * @return The ID of the channel this webhook is associated to.
	 */
	public Snowflake getChannelId() {
		return Snowflake.of(webhook.getChannelId());
	}

	/**
	 * Requests to retrieve the channel this webhook is associated to.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} this webhook is
	 * associated to. If an error is received, it is emitted through the {@code Mono}.
	 */
	public Mono<TextChannel> getChannel() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}

	/**
	 * Gets the ID of the user this webhook was created by, if present.
	 *
	 * @return The ID of the user this webhook was created by, if present.
	 */
	public Optional<Snowflake> getCreatorId() {
		return Optional.ofNullable(webhook.getUser())
				.map(user -> Snowflake.of(user.getId()));
	}

	/**
	 * Requests to retrieve the user this webhook was created by, if present.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link User user} this webhook was created
	 * by, if present. If an error is received, it is emitted through the {@code Mono}.
	 */
	public Mono<User> getCreator() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}

	/**
	 * Gets the default name of the webhook.
	 *
	 * @return The default name of the webhook.
	 */
	public Optional<String> getName() {
		return Optional.ofNullable(webhook.getName());
	}

	/**
	 * Gets the avatar of this webhook, if present.
	 *
	 * @return The avatar of this webhook, if present.
	 */
	public Optional<String> getAvatar() {
		return Optional.ofNullable(webhook.getAvatar());
	}

	/**
	 * Gets the secure token of this webhook.
	 *
	 * @return The secure token of this webhook.
	 */
	public String getToken() {
		return webhook.getToken();
	}
}
