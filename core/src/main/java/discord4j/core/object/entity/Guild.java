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

import discord4j.core.object.*;
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
	Set<Emoji> getEmojis();
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
}
