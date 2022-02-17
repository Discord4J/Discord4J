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

package discord4j.common.store.action.read;

import discord4j.common.store.action.read.CountInGuildAction.InGuildEntity;
import discord4j.common.store.action.read.CountTotalAction.CountableEntity;
import discord4j.common.store.api.StoreAction;
import discord4j.common.store.api.object.ExactResultNotAvailableException;

/**
 * Provides static factories to obtain {@link StoreAction} instances that enable reading data from a store.
 */
public class ReadActions {

    private ReadActions() {
        throw new AssertionError("No discord4j.common.store.action.read.ReadActions instances for you!");
    }

    /**
     * Creates an action to count the number of channels present in a store.
     *
     * @return a new {@link CountTotalAction}
     */
    public static CountTotalAction countChannels() {
        return new CountTotalAction(CountableEntity.CHANNELS);
    }

    /**
     * Creates an action to count the number of channels present in a store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return a new {@link CountInGuildAction}
     */
    public static CountInGuildAction countChannelsInGuild(long guildId) {
        return new CountInGuildAction(InGuildEntity.CHANNELS, guildId);
    }

    /**
     * Creates an action to count the number of emojis present in a store.
     *
     * @return a new {@link CountTotalAction}
     */
    public static CountTotalAction countEmojis() {
        return new CountTotalAction(CountableEntity.EMOJIS);
    }

    /**
     * Creates an action to count the number of emojis present in a store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return a new {@link CountInGuildAction}
     */
    public static CountInGuildAction countEmojisInGuild(long guildId) {
        return new CountInGuildAction(InGuildEntity.EMOJIS, guildId);
    }

    /**
     * Creates an action to count the number of guilds present in a store.
     *
     * @return a new {@link CountTotalAction}
     */
    public static CountTotalAction countGuilds() {
        return new CountTotalAction(CountableEntity.GUILDS);
    }

    /**
     * Creates an action to count the number of members present in a store.
     *
     * @return a new {@link CountTotalAction}
     */
    public static CountTotalAction countMembers() {
        return new CountTotalAction(CountableEntity.MEMBERS);
    }

    /**
     * Creates an action to count the number of members present in a store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return a new {@link CountInGuildAction}
     */
    public static CountInGuildAction countMembersInGuild(long guildId) {
        return new CountInGuildAction(InGuildEntity.MEMBERS, guildId);
    }

    /**
     * Creates an action to count the exact number of members for the given guild ID. If some members are not present
     * in the store and thus is not able to return an accurate count, executing this action will error with
     * {@link ExactResultNotAvailableException}.
     *
     * @param guildId the guild ID
     * @return a new {@link CountInGuildAction}
     */
    public static CountInGuildAction countExactMembersInGuild(long guildId) {
        return new CountInGuildAction(InGuildEntity.MEMBERS_EXACT, guildId);
    }

    /**
     * Creates an action to count the number of messages present in a store.
     *
     * @return a new {@link CountTotalAction}
     */
    public static CountTotalAction countMessages() {
        return new CountTotalAction(CountableEntity.MESSAGES);
    }

    /**
     * Creates an action to count the number of messages present in a store for the given channel ID.
     *
     * @param channelId the channel ID
     * @return a new {@link CountMessagesInChannelAction}
     */
    public static CountMessagesInChannelAction countMessagesInChannel(long channelId) {
        return new CountMessagesInChannelAction(channelId);
    }

    /**
     * Creates an action to count the number of presences present in a store.
     *
     * @return a new {@link CountTotalAction}
     */
    public static CountTotalAction countPresences() {
        return new CountTotalAction(CountableEntity.PRESENCES);
    }

    /**
     * Creates an action to count the number of presences present in a store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return a new {@link CountInGuildAction}
     */
    public static CountInGuildAction countPresencesInGuild(long guildId) {
        return new CountInGuildAction(InGuildEntity.PRESENCES, guildId);
    }

    /**
     * Creates an action to count the number of roles present in a store.
     *
     * @return a new {@link CountTotalAction}
     */
    public static CountTotalAction countRoles() {
        return new CountTotalAction(CountableEntity.ROLES);
    }

    /**
     * Creates an action to count the number of roles present in a store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return a new {@link CountInGuildAction}
     */
    public static CountInGuildAction countRolesInGuild(long guildId) {
        return new CountInGuildAction(InGuildEntity.ROLES, guildId);
    }

    /**
     * Creates an action to count the number of users present in a store.
     *
     * @return a new {@link CountTotalAction}
     */
    public static CountTotalAction countUsers() {
        return new CountTotalAction(CountableEntity.USERS);
    }

    /**
     * Creates an action to count the number of voice states present in a store.
     *
     * @return a new {@link CountTotalAction}
     */
    public static CountTotalAction countVoiceStates() {
        return new CountTotalAction(CountableEntity.VOICE_STATES);
    }

    /**
     * Creates an action to count the number of voice states present in a store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return a new {@link CountInGuildAction}
     */
    public static CountInGuildAction countVoiceStatesInGuild(long guildId) {
        return new CountInGuildAction(InGuildEntity.VOICE_STATES, guildId);
    }

    /**
     * Creates an action to count the number of voice states present in a store for the given guild ID and channel ID.
     *
     * @param guildId the guild ID
     * @param channelId the channel ID
     * @return a new {@link CountMessagesInChannelAction}
     */
    public static CountVoiceStatesInChannelAction countVoiceStatesInChannel(long guildId, long channelId) {
        return new CountVoiceStatesInChannelAction(guildId, channelId);
    }

    /**
     * Creates an action to retrieve data for all channels present in a store.
     *
     * @return a new {@link GetChannelsAction}
     */
    public static GetChannelsAction getChannels() {
        return new GetChannelsAction();
    }

    /**
     * Creates an action to retrieve data for all channels present in a store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return a new {@link GetChannelsInGuildAction}
     */
    public static GetChannelsInGuildAction getChannelsInGuild(long guildId) {
        return new GetChannelsInGuildAction(guildId);
    }

    /**
     * Creates an action to retrieve data for the channel corresponding to the given channel ID.
     *
     * @param channelId the channel ID
     * @return a new {@link GetChannelByIdAction}
     */
    public static GetChannelByIdAction getChannelById(long channelId) {
        return new GetChannelByIdAction(channelId);
    }

    /**
     * Creates an action to retrieve data for all emojis present in a store.
     *
     * @return a new {@link GetEmojisAction}
     */
    public static GetEmojisAction getEmojis() {
        return new GetEmojisAction();
    }

    /**
     * Creates an action to retrieve data for all emojis present in a store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return a new {@link GetEmojisInGuildAction}
     */
    public static GetEmojisInGuildAction getEmojisInGuild(long guildId) {
        return new GetEmojisInGuildAction(guildId);
    }

    /**
     * Creates an action to retrieve data for the emoji corresponding to the given guild ID and emoji ID.
     *
     * @param guildId the guild ID
     * @param emojiId the emoji ID
     * @return a new {@link GetEmojiByIdAction}
     */
    public static GetEmojiByIdAction getEmojiById(long guildId, long emojiId) {
        return new GetEmojiByIdAction(guildId, emojiId);
    }

    /**
     * Creates an action to retrieve data for all guilds present in a store.
     *
     * @return a new {@link GetGuildsAction}
     */
    public static GetGuildsAction getGuilds() {
        return new GetGuildsAction();
    }

    /**
     * Creates an action to retrieve data for the guild corresponding to the given guild ID.
     *
     * @param guildId the guild ID
     * @return a new {@link GetGuildByIdAction}
     */
    public static GetGuildByIdAction getGuildById(long guildId) {
        return new GetGuildByIdAction(guildId);
    }

    /**
     * Creates an action to retrieve data for all members present in a store.
     *
     * @return a new {@link GetMembersAction}
     */
    public static GetMembersAction getMembers() {
        return new GetMembersAction();
    }

    /**
     * Creates an action to retrieve data for all members present in a store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return a new {@link GetMembersInGuildAction}
     */
    public static GetMembersInGuildAction getMembersInGuild(long guildId) {
        return new GetMembersInGuildAction(guildId);
    }

    /**
     * Creates an action to retrieve data for all members for the given guild ID. If some members are not present in
     * the store and thus is not able to return the full member list of the guild, executing this action will error
     * with {@link ExactResultNotAvailableException}.
     *
     * @param guildId the guild ID
     * @return a new {@link GetExactMembersInGuildAction}
     */
    public static GetExactMembersInGuildAction getExactMembersInGuild(long guildId) {
        return new GetExactMembersInGuildAction(guildId);
    }

    /**
     * Creates an action to retrieve data for the member corresponding to the given guild ID and user ID.
     *
     * @param guildId the guild ID
     * @param userId  the user ID
     * @return a new {@link GetMemberByIdAction}
     */
    public static GetMemberByIdAction getMemberById(long guildId, long userId) {
        return new GetMemberByIdAction(guildId, userId);
    }

    /**
     * Creates an action to retrieve data for all messages present in a store.
     *
     * @return a new {@link GetMessagesAction}
     */
    public static GetMessagesAction getMessages() {
        return new GetMessagesAction();
    }

    /**
     * Creates an action to retrieve data for all messages present in a store for the given channel ID.
     *
     * @param channelId the channel ID
     * @return a new {@link GetMessagesInChannelAction}
     */
    public static GetMessagesInChannelAction getMessagesInChannel(long channelId) {
        return new GetMessagesInChannelAction(channelId);
    }

    /**
     * Creates an action to retrieve data for the message corresponding to the given channel ID and message ID.
     *
     * @param channelId the channel ID
     * @param messageId the message ID
     * @return a new {@link GetMessageByIdAction}
     */
    public static GetMessageByIdAction getMessageById(long channelId, long messageId) {
        return new GetMessageByIdAction(channelId, messageId);
    }

    /**
     * Creates an action to retrieve data for all presences present in a store.
     *
     * @return a new {@link GetPresencesAction}
     */
    public static GetPresencesAction getPresences() {
        return new GetPresencesAction();
    }

    /**
     * Creates an action to retrieve data for all presences present in a store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return a new {@link GetPresencesInGuildAction}
     */
    public static GetPresencesInGuildAction getPresencesInGuild(long guildId) {
        return new GetPresencesInGuildAction(guildId);
    }

    /**
     * Creates an action to retrieve data for the presence corresponding to the given guild ID and user ID.
     *
     * @param guildId the guild ID
     * @param userId  the user ID
     * @return a new {@link GetPresenceByIdAction}
     */
    public static GetPresenceByIdAction getPresenceById(long guildId, long userId) {
        return new GetPresenceByIdAction(guildId, userId);
    }

    /**
     * Creates an action to retrieve data for all roles present in a store.
     *
     * @return a new {@link GetRolesAction}
     */
    public static GetRolesAction getRoles() {
        return new GetRolesAction();
    }

    /**
     * Creates an action to retrieve data for all roles present in a store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return a new {@link GetRolesInGuildAction}
     */
    public static GetRolesInGuildAction getRolesInGuild(long guildId) {
        return new GetRolesInGuildAction(guildId);
    }

    /**
     * Creates an action to retrieve data for the role corresponding to the given guild ID and role ID.
     *
     * @param guildId the guild ID
     * @param roleId  the role ID
     * @return a new {@link GetRoleByIdAction}
     */
    public static GetRoleByIdAction getRoleById(long guildId, long roleId) {
        return new GetRoleByIdAction(guildId, roleId);
    }

    /**
     * Creates an action to retrieve data for all users present in a store.
     *
     * @return a new {@link GetUsersAction}
     */
    public static GetUsersAction getUsers() {
        return new GetUsersAction();
    }

    /**
     * Creates an action to retrieve data for the user corresponding to the given user ID.
     *
     * @param userId the user ID
     * @return a new {@link GetUserByIdAction}
     */
    public static GetUserByIdAction getUserById(long userId) {
        return new GetUserByIdAction(userId);
    }

    /**
     * Creates an action to retrieve data for all voice states present in a store.
     *
     * @return a new {@link GetVoiceStatesAction}
     */
    public static GetVoiceStatesAction getVoiceStates() {
        return new GetVoiceStatesAction();
    }

    /**
     * Creates an action to retrieve data for all voice states present in a store for the given guild ID and channel ID.
     *
     * @param guildId the guild ID
     * @param channelId the channel ID
     * @return a new {@link GetVoiceStatesInChannelAction}
     */
    public static GetVoiceStatesInChannelAction getVoiceStatesInChannel(long guildId, long channelId) {
        return new GetVoiceStatesInChannelAction(guildId, channelId);
    }

    /**
     * Creates an action to retrieve data for all voice states present in a store for the given guild ID.
     *
     * @param guildId the guild ID
     * @return a new {@link GetVoiceStatesInGuildAction}
     */
    public static GetVoiceStatesInGuildAction getVoiceStatesInGuild(long guildId) {
        return new GetVoiceStatesInGuildAction(guildId);
    }

    /**
     * Creates an action to retrieve data for the voice state corresponding to the given guild ID and user ID.
     *
     * @param guildId the guild ID
     * @param userId  the user ID
     * @return a new {@link GetVoiceStateByIdAction}
     */
    public static GetVoiceStateByIdAction getVoiceStateById(long guildId, long userId) {
        return new GetVoiceStateByIdAction(guildId, userId);
    }

    /**
     * Creates an action to retrieve data for the stage instance corresponding to the given channel ID.
     *
     * @param channelId the channel ID
     * @return a new {@link GetStageInstanceByChannelIdAction}
     */
    public static GetStageInstanceByChannelIdAction getStageInstanceByChannelId(long channelId) {
        return new GetStageInstanceByChannelIdAction(channelId);
    }
}
