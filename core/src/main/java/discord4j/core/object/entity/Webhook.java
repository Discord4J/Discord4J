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

import discord4j.core.object.Snowflake;
import discord4j.core.trait.Deletable;
import discord4j.core.trait.Renameable;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * A Discord webhook.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/webhook">Webhook Resource</a>
 */
public interface Webhook extends Deletable, Entity, Renameable<Webhook> {

	Snowflake getGuildId();
	Mono<Guild> getGuild();
	Snowflake getChannelId();
	Mono<TextChannel> getChannel();
	User getCreator();
	Optional<String> getAvatar();
	String getToken();
}
