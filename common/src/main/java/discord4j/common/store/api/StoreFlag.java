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

package discord4j.common.store.api;

/**
 * Flag for signaling which store actions will be enabled or disabled.
 */
public enum StoreFlag {

    /**
     * Flag affecting operations related to channels.
     * <p>
     * Updates:
     * <ul>
     *     <li>onChannelCreate</li>
     *     <li>onChannelDelete</li>
     *     <li>onChannelUpdate</li>
     * </ul>
     * Queries:
     * <ul>
     *     <li>countChannels</li>
     *     <li>countChannelsInGuild</li>
     *     <li>getChannels</li>
     *     <li>getChannelsInGuild</li>
     *     <li>getChannelById</li>
     * </ul>
     */
    CHANNEL,

    /**
     * Flag affecting operations related to guild emojis.
     * <p>
     * Updates:
     * <ul>
     *     <li>onGuildEmojisUpdate</li>
     * </ul>
     * Queries:
     * <ul>
     *     <li>countEmojis</li>
     *     <li>countEmojisInGuild</li>
     *     <li>getEmojis</li>
     *     <li>getEmojisInGuild</li>
     *     <li>getEmojiById</li>
     * </ul>
     */
    EMOJI,

    /**
     * Flag affecting operations related to guilds.
     * <p>
     * Updates:
     * <ul>
     *     <li>onGuildCreate</li>
     *     <li>onGuildDelete</li>
     *     <li>onGuildUpdate</li>
     * </ul>
     * Queries:
     * <ul>
     *     <li>countGuilds</li>
     *     <li>getGuilds</li>
     *     <li>getGuildById</li>
     * </ul>
     */
    GUILD,

    /**
     * Flag affecting operations related to guild members.
     * <p>
     * Updates:
     * <ul>
     *     <li>onGuildMemberAdd</li>
     *     <li>onGuildMemberRemove</li>
     *     <li>onGuildMembersChunk</li>
     *     <li>onGuildMemberUpdate</li>
     *     <li>onGuildMembersCompletion</li>
     * </ul>
     * Queries:
     * <ul>
     *     <li>countMembers</li>
     *     <li>countMembersInGuild</li>
     *     <li>countExactMembersInGuild</li>
     *     <li>getMembers</li>
     *     <li>getMembersInGuild</li>
     *     <li>getExactMembersInGuild</li>
     *     <li>getMemberById</li>
     * </ul>
     */
    MEMBER,

    /**
     * Flag affecting operations related to messages.
     * <p>
     * Updates:
     * <ul>
     *     <li>onMessageCreate</li>
     *     <li>onMessageDelete</li>
     *     <li>onMessageDeleteBulk</li>
     *     <li>onMessageReactionAdd</li>
     *     <li>onMessageReactionRemove</li>
     *     <li>onMessageReactionRemoveAll</li>
     *     <li>onMessageReactionRemoveEmoji</li>
     *     <li>onMessageUpdate</li>
     * </ul>
     * Queries:
     * <ul>
     *     <li>countMessages</li>
     *     <li>countMessagesInChannel</li>
     *     <li>getMessages</li>
     *     <li>getMessagesInChannel</li>
     *     <li>getMessageById</li>
     * </ul>
     */
    MESSAGE,

    /**
     * Flag affecting operations related to presences.
     * <p>
     * Updates:
     * <ul>
     *     <li>onPresenceUpdate</li>
     * </ul>
     * Queries:
     * <ul>
     *     <li>countPresences</li>
     *     <li>countPresencesInGuild</li>
     *     <li>getPresences</li>
     *     <li>getPresencesInGuild</li>
     *     <li>getPresenceById</li>
     * </ul>
     */
    PRESENCE,

    /**
     * Flag affecting operations related to guild roles.
     * <p>
     * Updates:
     * <ul>
     *     <li>onGuildRoleCreate</li>
     *     <li>onGuildRoleDelete</li>
     *     <li>onGuildRoleUpdate</li>
     * </ul>
     * Queries:
     * <ul>
     *     <li>countRoles</li>
     *     <li>countRolesInGuild</li>
     *     <li>getRoles</li>
     *     <li>getRolesInGuild</li>
     *     <li>getRoleById</li>
     * </ul>
     */
    ROLE,

    /**
     * Flag affecting operations related to users.
     * <p>
     * Updates:
     * <ul>
     *     <li>onUserUpdate</li>
     * </ul>
     * Queries:
     * <ul>
     *     <li>countUsers</li>
     *     <li>getUsers</li>
     *     <li>getUserById</li>
     * </ul>
     */
    USER,

    /**
     * Flag affecting operations related to voice states.
     * <p>
     * Updates:
     * <ul>
     *     <li>onVoiceStateUpdateDispatch</li>
     * </ul>
     * Queries:
     * <ul>
     *     <li>countVoiceStates</li>
     *     <li>countVoiceStatesInGuild</li>
     *     <li>countVoiceStatesInChannel</li>
     *     <li>getVoiceStates</li>
     *     <li>getVoiceStatesInChannel</li>
     *     <li>getVoiceStatesInGuild</li>
     *     <li>getVoiceStateById</li>
     * </ul>
     */
    VOICE_STATE,

    /**
     * Flag affecting operations related to guild stickers.
     * <p>
     * Updates:
     * <ul>
     *     <li>onGuildStickersUpdate</li>
     * </ul>
     * Queries:
     * <ul>
     *     <li>countStickers</li>
     *     <li>countStickersInGuild</li>
     *     <li>getStickers</li>
     *     <li>getStickersInGuild</li>
     *     <li>getStickerById</li>
     * </ul>
     */
    STICKER,

    /**
     * Flag affecting operations related to guild scheduled events.
     * <p>
     * Updates:
     * <ul>
     *     <li>onGuildScheduledEventCreate</li>
     *     <li>onGuildScheduledEventUpdate</li>
     *     <li>onGuildScheduledEventDelete</li>
     *     <li>onGuildScheduledEventUserAdd</li>
     *     <li>onGuildScheduledEventUserRemove</li>
     * </ul>
     * Queries:
     * <ul>
     *     <li>getScheduledEventsInGuild</li>
     *     <li>getScheduledEventById</li>
     *     <li>getScheduledEventUsersInEvent</li>
     * </ul>
     */
    SCHEDULED_EVENT,

    /**
     * Flag affecting operations related to threads.
     * Updates:
     * <ul>
     *     <li>onThreadCreate</li>
     *     <li>onThreadUpdate</li>
     *     <li>onThreadDelete</li>
     *     <li>onThreadListSync</li>
     *     <li>onThreadMemberUpdate</li>
     *     <li>onThreadMembersUpdate</li>
     * </ul>
     * Queries:
     * <ul>
     *     <li>getThreadMemberById</li>
     *     <li>getMembersInThread</li>
     * </ul>
     * @since 3.3.0
     */
    THREAD,

    /**
     * Flag affecting operations related to stage instances.
     * Updates:
     * <ul>
     *     <li>onStageInstanceCreate</li>
     *     <li>onStageInstanceUpdate</li>
     *     <li>onStageInstanceDelete</li>
     * </ul>
     * Queries:
     * <ul>
     *     <li>getStageInstanceByChannelId</li>
     * </ul>
     * @since 3.3.0
     */
    STAGE_INSTANCE
}
