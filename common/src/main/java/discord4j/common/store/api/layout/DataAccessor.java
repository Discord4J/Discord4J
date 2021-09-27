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

package discord4j.common.store.api.layout;

import discord4j.common.store.api.object.ExactResultNotAvailableException;
import discord4j.discordjson.json.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Defines methods to handle read operations on a store.
 */
public interface DataAccessor {

    /**
     * Counts the number of channels present in the store.
     *
     * @return A {@link Mono} emitting the channel count
     */
    Mono<Long> countChannels();

    /**
     * Counts the number of channels present in the store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return A {@link Mono} emitting the channel count
     */
    Mono<Long> countChannelsInGuild(long guildId);

    /**
     * Counts the number of emojis present in the store.
     *
     * @return A {@link Mono} emitting the emoji count
     */
    Mono<Long> countEmojis();

    /**
     * Counts the number of emojis present in the store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return A {@link Mono} emitting the emoji count
     */
    Mono<Long> countEmojisInGuild(long guildId);

    /**
     * Counts the number of guilds present in the store.
     *
     * @return A {@link Mono} emitting the guild count
     */
    Mono<Long> countGuilds();

    /**
     * Counts the number of members present in the store.
     *
     * @return A {@link Mono} emitting the member count
     */
    Mono<Long> countMembers();

    /**
     * Counts the number of members present in the store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return A {@link Mono} emitting the member count
     */
    Mono<Long> countMembersInGuild(long guildId);

    /**
     * Counts the exact number of members for the given guild ID. If some members are not present in the store and thus
     * is not able to return an accurate count, it will error with {@link ExactResultNotAvailableException}.
     *
     * @param guildId the guild ID
     * @return A {@link Mono} emitting the member count
     */
    Mono<Long> countExactMembersInGuild(long guildId);

    /**
     * Counts the number of messages present in the store.
     *
     * @return A {@link Mono} emitting the message count
     */
    Mono<Long> countMessages();

    /**
     * Counts the number of messages present in the store for the given channel ID.
     *
     * @param channelId the channel ID
     * @return A {@link Mono} emitting the message count
     */
    Mono<Long> countMessagesInChannel(long channelId);

    /**
     * Counts the number of presences present in the store.
     *
     * @return A {@link Mono} emitting the presence count
     */
    Mono<Long> countPresences();

    /**
     * Counts the number of presences present in the store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return A {@link Mono} emitting the presence count
     */
    Mono<Long> countPresencesInGuild(long guildId);

    /**
     * Counts the number of roles present in the store.
     *
     * @return A {@link Mono} emitting the role count
     */
    Mono<Long> countRoles();

    /**
     * Counts the number of roles present in the store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return A {@link Mono} emitting the role count
     */
    Mono<Long> countRolesInGuild(long guildId);

    /**
     * Counts the number of users present in the store.
     *
     * @return A {@link Mono} emitting the user count
     */
    Mono<Long> countUsers();

    /**
     * Counts the number of voice states present in the store.
     *
     * @return A {@link Mono} emitting the voice state count
     */
    Mono<Long> countVoiceStates();

    /**
     * Counts the number of voice states present in the store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return A {@link Mono} emitting the voice state count
     */
    Mono<Long> countVoiceStatesInGuild(long guildId);

    /**
     * Counts the number of voice states present in the store for the given channel ID.
     *
     * @param guildId the guild ID
     * @param channelId the channel ID
     * @return A {@link Mono} emitting the voice state count
     */
    Mono<Long> countVoiceStatesInChannel(long guildId, long channelId);

    /**
     * Retrieves data for all channels present in the store.
     *
     * @return A {@link Flux} emitting the channels, or empty if none is present
     */
    Flux<ChannelData> getChannels();

    /**
     * Retrieves data for all channels present in the store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return A {@link Flux} emitting the channels, or empty if none is present
     */
    Flux<ChannelData> getChannelsInGuild(long guildId);

    /**
     * Retrieves data for the channel corresponding to the given channel ID.
     *
     * @param channelId the channel ID
     * @return A {@link Mono} emitting the channel, or empty if not found
     */
    Mono<ChannelData> getChannelById(long channelId);

    /**
     * Retrieves data for all emojis present in the store.
     *
     * @return A {@link Flux} emitting the emojis, or empty if none is present
     */
    Flux<EmojiData> getEmojis();

    /**
     * Retrieves data for all emojis present in the store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return A {@link Flux} emitting the emojis, or empty if none is present
     */
    Flux<EmojiData> getEmojisInGuild(long guildId);

    /**
     * Retrieves data for the emoji corresponding to the given guild ID and emoji ID.
     *
     * @param guildId the guild ID
     * @param emojiId the emoji ID
     * @return A {@link Mono} emitting the emoji, or empty if not found
     */
    Mono<EmojiData> getEmojiById(long guildId, long emojiId);

    /**
     * Retrieves data for all guilds present in the store.
     *
     * @return A {@link Flux} emitting the guilds, or empty if none is present
     */
    Flux<GuildData> getGuilds();

    /**
     * Retrieves data for the guild corresponding to the given guild ID.
     *
     * @param guildId the guild ID
     * @return A {@link Mono} emitting the guild, or empty if not found
     */
    Mono<GuildData> getGuildById(long guildId);

    /**
     * Retrieves data for all members present in the store.
     *
     * @return A {@link Flux} emitting the members, or empty if none is present
     */
    Flux<MemberData> getMembers();

    /**
     * Retrieves data for all members present in the store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return A {@link Flux} emitting the members, or empty if none is present
     */
    Flux<MemberData> getMembersInGuild(long guildId);

    /**
     * Retrieves data for all members for the given guild ID. If some members are not present in the store and thus
     * is not able to return the full member list of the guild, it will error with
     * {@link ExactResultNotAvailableException}.
     *
     * @param guildId the guild ID
     * @return A {@link Flux} emitting the members, or {@link ExactResultNotAvailableException} if not all are present
     */
    Flux<MemberData> getExactMembersInGuild(long guildId);

    /**
     * Retrieves data for the member corresponding to the given guild ID and user ID.
     *
     * @param guildId the guild ID
     * @param userId  the user ID
     * @return A {@link Mono} emitting the member, or empty if not found
     */
    Mono<MemberData> getMemberById(long guildId, long userId);

    /**
     * Retrieves data for all messages present in the store.
     *
     * @return A {@link Flux} emitting the messages, or empty if none is present
     */
    Flux<MessageData> getMessages();

    /**
     * Retrieves data for all messages present in the store for the given channel ID.
     *
     * @param channelId the channel ID
     * @return A {@link Flux} emitting the messages, or empty if none is present
     */
    Flux<MessageData> getMessagesInChannel(long channelId);

    /**
     * Retrieves data for the message corresponding to the given channel ID and message ID.
     *
     * @param channelId the channel ID
     * @param messageId the message ID
     * @return A {@link Mono} emitting the message, or empty if not found
     */
    Mono<MessageData> getMessageById(long channelId, long messageId);

    /**
     * Retrieves data for all presences present in the store.
     *
     * @return A {@link Flux} emitting the presences, or empty if none is present
     */
    Flux<PresenceData> getPresences();

    /**
     * Retrieves data for all presences present in the store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return A {@link Flux} emitting the presences, or empty if none is present
     */
    Flux<PresenceData> getPresencesInGuild(long guildId);

    /**
     * Retrieves data for the presence corresponding to the given guild ID and user ID.
     *
     * @param guildId the guild ID
     * @param userId  the user ID
     * @return A {@link Mono} emitting the presence, or empty if not found
     */
    Mono<PresenceData> getPresenceById(long guildId, long userId);

    /**
     * Retrieves data for all roles present in the store.
     *
     * @return A {@link Flux} emitting the roles, or empty if none is present
     */
    Flux<RoleData> getRoles();

    /**
     * Retrieves data for all roles present in the store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return A {@link Flux} emitting the roles, or empty if none is present
     */
    Flux<RoleData> getRolesInGuild(long guildId);

    /**
     * Retrieves data for the role corresponding to the given guild ID and role ID.
     *
     * @param guildId the guild ID
     * @param roleId  the role ID
     * @return A {@link Mono} emitting the role, or empty if not found
     */
    Mono<RoleData> getRoleById(long guildId, long roleId);

    /**
     * Retrieves data for all users present in the store.
     *
     * @return A {@link Flux} emitting the users, or empty if none is present
     */
    Flux<UserData> getUsers();

    /**
     * Retrieves data for the user corresponding to the given user ID.
     *
     * @param userId the user ID
     * @return A {@link Mono} emitting the user, or empty if not found
     */
    Mono<UserData> getUserById(long userId);

    /**
     * Retrieves data for all voice states present in the store.
     *
     * @return A {@link Flux} emitting the voice states, or empty if none is present
     */
    Flux<VoiceStateData> getVoiceStates();

    /**
     * Retrieves data for all voice states present in the store for the given guild ID and channel ID.
     *
     * @param guildId the guild ID
     * @param channelId the channel ID
     * @return A {@link Flux} emitting the voice states, or empty if none is present
     */
    Flux<VoiceStateData> getVoiceStatesInChannel(long guildId, long channelId);

    /**
     * Retrieves data for all voice states present in the store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return A {@link Flux} emitting the voice states, or empty if none is present
     */
    Flux<VoiceStateData> getVoiceStatesInGuild(long guildId);

    /**
     * Retrieves data for the voice state corresponding to the given guild ID and user ID.
     *
     * @param guildId the guild ID
     * @param userId  the user ID
     * @return A {@link Mono} emitting the voice state, or empty if not found
     */
    Mono<VoiceStateData> getVoiceStateById(long guildId, long userId);

    Mono<StageInstanceData> getStageInstanceByChannelId(long channelId);
}
