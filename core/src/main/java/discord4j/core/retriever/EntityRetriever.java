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
package discord4j.core.retriever;

import discord4j.common.util.Snowflake;
import discord4j.core.object.ScheduledEventUser;
import discord4j.core.object.VoiceState;
import discord4j.core.object.automod.AutoModRule;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.core.object.entity.GuildSticker;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.ScheduledEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.util.OrderUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Abstraction for entity retrieval.
 */
public interface EntityRetriever {

    /**
     * Requests to retrieve the channel represented by the supplied ID.
     *
     * @param channelId The ID of the channel.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Channel} as represented by the
     *         supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Channel> getChannelById(Snowflake channelId);

    /**
     * Requests to retrieve the guild represented by the supplied ID.
     *
     * @param guildId The ID of the guild.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} as represented by the supplied
     *         ID. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Guild> getGuildById(Snowflake guildId);

    /**
     * Requests to retrieve the guild sticker represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param stickerId The ID of the sticker.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildSticker} as represented by the
     *         supplied IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<GuildSticker> getGuildStickerById(Snowflake guildId, Snowflake stickerId);

    /**
     * Requests to retrieve the guild emoji represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param emojiId The ID of the emoji.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildEmoji} as represented by the
     *         supplied IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<GuildEmoji> getGuildEmojiById(Snowflake guildId, Snowflake emojiId);

    /**
     * Requests to retrieve the member represented by the supplied IDs. Depending on the implementation, it is possible
     * to lazily request member entities from the Gateway, or the REST API.
     *
     * @param guildId The ID of the guild.
     * @param userId  The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} as represented by the supplied
     *         IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Member> getMemberById(Snowflake guildId, Snowflake userId);

    /**
     * Requests to retrieve the message represented by the supplied IDs.
     *
     * @param channelId The ID of the channel.
     * @param messageId The ID of the message.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} as represented by the
     *         supplied IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Message> getMessageById(Snowflake channelId, Snowflake messageId);

    /**
     * Requests to retrieve the role represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param roleId  The ID of the role.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Role} as represented by the supplied
     *         IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Role> getRoleById(Snowflake guildId, Snowflake roleId);

    /**
     * Requests to retrieve the user represented by the supplied ID.
     *
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} as represented by the supplied
     *         ID. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<User> getUserById(Snowflake userId);

    /**
     * Requests to retrieve the guilds the current client is in.
     *
     * @return A {@link Flux} that continually emits the {@link Guild guilds} that the current client is in. If an error
     *         is received, it is emitted through the {@code Flux}.
     */
    Flux<Guild> getGuilds();

    /**
     * Requests to retrieve the bot user.
     *
     * @return A {@link Mono} where, upon successful completion, emits the bot {@link User user}. If an error is
     *         received, it is emitted through the {@code Mono}.
     */
    Mono<User> getSelf();

    /**
     * Requests to retrieve the bot user represented as a {@link Member member} of the guild with the supplied ID.
     * @param guildId The ID of the guild.
     * @return A {@link Mono} where, upon successful completion, emits the bot {@link Member member}. If an error is
     *         received, it is emitted through the {@code Mono}.
     */
    Mono<Member> getSelfMember(Snowflake guildId);

    /**
     * Requests to retrieve the guild's members.
     *
     * @param guildId   the ID of the guild.
     * @return A {@link Flux} that continually emits the {@link Member members} of the guild. If an error is received,
     *         it is emitted through the {@code Flux}.
     */
    Flux<Member> getGuildMembers(Snowflake guildId);

    /**
     * Requests to retrieve the guild's channels.
     * <p>
     * The order of items emitted by the returned {@code Flux} is unspecified. Use
     * {@link OrderUtil#orderGuildChannels(Flux)} to consistently order channels.
     *
     * @param guildId the ID of the guild.
     * @return A {@link Flux} that continually emits the guild's {@link GuildChannel channels}. If an error is received,
     *         it is emitted through the {@code Flux}.
     */
    Flux<GuildChannel> getGuildChannels(Snowflake guildId);

    /**
     * Requests to retrieve the guild's roles.
     * <p>
     * The order of items emitted by the returned {@code Flux} is unspecified. Use {@link OrderUtil#orderRoles(Flux)}
     * to consistently order roles.
     *
     * @param guildId The ID of the guild.
     * @return A {@link Flux} that continually emits the guild's {@link Role roles}. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    Flux<Role> getGuildRoles(Snowflake guildId);

    /**
     * Requests to retrieve the guild's emojis.
     *
     * @param guildId The ID of the guild.
     * @return A {@link Flux} that continually emits the guild's {@link GuildEmoji emojis}. If an error is received,
     * it is emitted through the {@code Flux}.
     */
    Flux<GuildEmoji> getGuildEmojis(Snowflake guildId);

    /**
     * Requests to retrieve the stage instance associated to the supplied channel ID.
     *
     * @param channelId The ID of the channel.
     * @return A {@link Mono} where, upon successful completion, emits the {@link StageInstance} associated to the supplied
     *         channel ID. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<StageInstance> getStageInstanceByChannelId(Snowflake channelId);

    /**
     * Requests to retrieve the guild's stickers.
     *
     * @param guildId The ID of the guild.
     * @return A {@link Flux} that continually emits the guild's {@link GuildSticker stickers}. If an error is received,
     * it is emitted through the {@code Flux}.
     */
    Flux<GuildSticker> getGuildStickers(Snowflake guildId);

    /**
     * Requests to retrieve the thread member associated to the supplied thread ID and user ID.
     *
     * @param threadId The ID of the thread.
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link ThreadMember} associated to the supplied
     *         thread ID and user ID. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<ThreadMember> getThreadMemberById(Snowflake threadId, Snowflake userId);

    /**
     * Requests to retrieve the thread's members.
     *
     * @param threadId The ID of the thread.
     * @return A {@link Flux} that continually emits the thread's {@link ThreadMember members}. If an error is received,
     * it is emitted through the {@code Flux}.
     */
    Flux<ThreadMember> getThreadMembers(Snowflake threadId);

    /**
     * Requests to retrieve the guild's automod rules.
     *
     * @return A {@link Flux} that continually emits the guild's {@link AutoModRule AutoModRule}. If an error is received,
     * it is emitted through the {@code Flux}.
     */
    Flux<AutoModRule> getGuildAutoModRules(Snowflake guildId);

    /**
     * Requests to retrieve the {@link ScheduledEvent} represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param eventId The ID of the scheduled event.
     * @return A {@link Mono} where, upon successful completion, emits the {@link ScheduledEvent} as represented by the
     * supplied IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<ScheduledEvent> getScheduledEventById(Snowflake guildId, Snowflake eventId);

    /**
     * Requests to retrieve the guild's scheduled events.
     *
     * @return A {@link Flux} that continually emits the guild's {@link ScheduledEvent events}. If an error is received,
     * it is emitted through the {@code Flux}.
     */
    Flux<ScheduledEvent> getScheduledEvents(Snowflake guildId);

    /**
     * Requests to retrieve the users that subscribed to the event represented by the supplied IDs.
     *
     * @return A {@link Flux} that continually emits the event's {@link ScheduledEventUser users}.
     * If an error is received, it is emitted through the {@code Flux}.
     */
    Flux<ScheduledEventUser> getScheduledEventUsers(Snowflake guildId, Snowflake eventId);

    /**
     * Requests to retrieve the Voice State of User in Guild represented by the supplied ID.
     *
     * @param guildId The ID of the guild.
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link VoiceState} as represented by the supplied
     *         ID of User. If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<VoiceState> getVoiceStateById(Snowflake guildId, Snowflake userId);

}
