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
package discord4j.rest.route;

import discord4j.common.annotations.Experimental;
import discord4j.common.json.*;
import discord4j.rest.json.response.*;

/**
 * A collection of {@link discord4j.rest.route.Route} object definitions.
 *
 * @since 3.0
 */
public abstract class Routes {

    /**
     * The base URL for all API requests.
     *
     * @see <a href="https://discordapp.com/developers/docs/reference#base-url">
     *         https://discordapp.com/developers/docs/reference#base-url</a>
     */
    public static final String BASE_URL = "https://discordapp.com/api/v6";

    //////////////////////////////////////////////
    ////////////// Gateway Resource //////////////
    //////////////////////////////////////////////

    /**
     * Returns an object with a single valid WSS URL, which the client can use as a basis for Connecting. Clients
     * should cache this value and only call this endpoint to retrieve a new URL if they are unable to properly
     * establish a connection using the cached version of the URL.
     *
     * @see <a href="https://discordapp.com/developers/docs/topics/gateway#get-gateway">
     *         https://discordapp.com/developers/docs/topics/gateway#get-gateway</a>
     */
    public static final Route<GatewayResponse> GATEWAY_GET = Route.get("/gateway", GatewayResponse.class);

    /**
     * Returns an object with the same information as Get Gateway, plus a shards key, containing the recommended number
     * of shards to connect with (as an integer). Bots that want to dynamically/automatically spawn shard processes
     * should use this endpoint to determine the number of processes to run. This route should be called once when
     * starting up numerous shards, with the response being cached and passed to all sub-shards/processes. Unlike the
     * Get Gateway, this route should not be cached for extended periods of time as the value is not guaranteed to be
     * the same per-call, and changes as the bot joins/leaves guilds.
     *
     * @see <a href="https://discordapp.com/developers/docs/topics/gateway#get-gateway-bot">
     *         https://discordapp.com/developers/docs/topics/gateway#get-gateway-bot</a>
     */
    public static final Route<GatewayResponse> GATEWAY_BOT_GET = Route.get("/gateway/bot", GatewayResponse.class);

    //////////////////////////////////////////////
    ////////////// Audit Log Resource ////////////
    //////////////////////////////////////////////

    /**
     * Returns an audit log object for the guild. Requires the 'VIEW_AUDIT_LOG' permission.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/audit-log#get-guild-audit-log">
     *         https://discordapp.com/developers/docs/resources/audit-log#get-guild-audit-log</a>
     */
    public static final Route<AuditLogResponse> AUDIT_LOG_GET = Route.get("/guilds/{guild.id}/audit-logs",
            AuditLogResponse.class);

    //////////////////////////////////////////////
    ////////////// Channel Resource //////////////
    //////////////////////////////////////////////

    /**
     * Get a channel by ID. Returns a guild channel or dm channel object.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#get-channel">
     *         https://discordapp.com/developers/docs/resources/channel#get-channel</a>
     */
    public static final Route<ChannelResponse> CHANNEL_GET = Route.get("/channels/{channel.id}", ChannelResponse.class);

    /**
     * Update a channels settings. Requires the 'MANAGE_CHANNELS' permission for the guild. Returns a guild channel on
     * success, and a 400 BAD REQUEST on invalid parameters. Fires a Channel Update Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#modify-channel">
     *         https://discordapp.com/developers/docs/resources/channel#modify-channel</a>
     */
    public static final Route<ChannelResponse> CHANNEL_MODIFY = Route.put("/channels/{channel.id}",
            ChannelResponse.class);

    /**
     * Update a channels settings. Requires the 'MANAGE_CHANNELS' permission for the guild. Returns a guild channel on
     * success, and a 400 BAD REQUEST on invalid parameters. Fires a Channel Update Gateway event. All the JSON Params
     * are optional.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#modify-channel">
     *         https://discordapp.com/developers/docs/resources/channel#modify-channel</a>
     */
    public static final Route<ChannelResponse> CHANNEL_MODIFY_PARTIAL = Route.patch("/channels/{channel.id}",
            ChannelResponse.class);

    /**
     * Delete a guild channel, or close a private message. Requires the 'MANAGE_CHANNELS' permission for the guild.
     * Returns a guild channel or dm channel object on success. Fires a Channel Delete Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#deleteclose-channel">
     *         https://discordapp.com/developers/docs/resources/channel#deleteclose-channel</a>
     */
    public static final Route<ChannelResponse> CHANNEL_DELETE = Route.delete("/channels/{channel.id}",
            ChannelResponse.class);

    /**
     * Returns the messages for a channel. If operating on a guild channel, this endpoint requires the 'READ_MESSAGES'
     * permission to be present on the current user. Returns an array of message objects on success.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#get-channel-messages">
     *         https://discordapp.com/developers/docs/resources/channel#get-channel-messages</a>
     */
    public static final Route<MessageResponse[]> MESSAGES_GET = Route.get("/channels/{channel.id}/messages",
            MessageResponse[].class);

    /**
     * Returns a specific message in the channel. If operating on a guild channel, this endpoints requires the
     * 'READ_MESSAGE_HISTORY' permission to be present on the current user. Returns a message object on success.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#get-channel-message">
     *         https://discordapp.com/developers/docs/resources/channel#get-channel-message</a>
     */
    public static final Route<MessageResponse> MESSAGE_GET = Route.get("/channels/{channel.id}/messages/{message.id}",
            MessageResponse.class);

    /**
     * Post a message to a guild text or DM channel. If operating on a guild channel, this endpoint requires the
     * 'SEND_MESSAGES' permission to be present on the current user. Returns a message object. Fires a Message Create
     * Gateway event. See message formatting for more information on how to properly format messages.
     * <p>
     * This endpoint supports both JSON and form data bodies. It does require multipart/form-data requests instead
     * of the normal JSON request type when uploading files. Make sure you set your Content-Type to multipart/form-data
     * if you're doing that. Note that in that case, the embed field cannot be used, but you can pass an url-encoded
     * JSON body as a form value for payload_json.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#create-message">
     *         https://discordapp.com/developers/docs/resources/channel#create-message</a>
     */
    public static final Route<MessageResponse> MESSAGE_CREATE = Route.post("/channels/{channel.id}/messages",
            MessageResponse.class);

    /**
     * Create a reaction for the message. This endpoint requires the 'READ_MESSAGE_HISTORY' permission to be present on
     * the current user. Additionally, if nobody else has reacted to the message using this emoji, this endpoint
     * requires the 'ADD_REACTIONS' permission to be present on the current user. Returns a 204 empty response on
     * success.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#create-reaction">
     *         https://discordapp.com/developers/docs/resources/channel#create-reaction</a>
     */
    public static final Route<Void> REACTION_CREATE = Route.put(
            "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/@me", Void.class);

    /**
     * Delete a reaction the current user has made for the message. Returns a 204 empty response on success.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#delete-own-reaction">
     *         https://discordapp.com/developers/docs/resources/channel#delete-own-reaction</a>
     */
    public static final Route<Void> REACTION_DELETE_OWN = Route.delete(
            "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/@me", Void.class);

    /**
     * Deletes another user's reaction. This endpoint requires the 'MANAGE_MESSAGES' permission to be present on the
     * current user. Returns a 204 empty response on success.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#delete-user-reaction">
     *         https://discordapp.com/developers/docs/resources/channel#delete-user-reaction</a>
     */
    public static final Route<Void> REACTION_DELETE = Route.delete(
            "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/{user.id}", Void.class);

    /**
     * Get a list of users that reacted with this emoji. Returns an array of user objects on success.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#get-reactions">
     *         https://discordapp.com/developers/docs/resources/channel#get-reactions</a>
     */
    public static final Route<UserResponse[]> REACTIONS_GET = Route.get(
            "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}", UserResponse[].class);

    /**
     * Deletes all reactions on a message. This endpoint requires the 'MANAGE_MESSAGES' permission to be present on the
     * current user.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#delete-all-reactions">
     *         https://discordapp.com/developers/docs/resources/channel#delete-all-reactions</a>
     */
    public static final Route<Void> REACTIONS_DELETE_ALL = Route.delete(
            "/channels/{channel.id}/messages/{message.id}/reactions", Void.class);

    /**
     * Edit a previously sent message. You can only edit messages that have been sent by the current user. Returns a
     * message object. Fires a Message Update Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#edit-message">
     *         https://discordapp.com/developers/docs/resources/channel#edit-message</a>
     */
    public static final Route<MessageResponse> MESSAGE_EDIT = Route.patch(
            "/channels/{channel.id}/messages/{message.id}", MessageResponse.class);

    /**
     * Delete a message. If operating on a guild channel and trying to delete a message that was not sent by the
     * current user, this endpoint requires the 'MANAGE_MESSAGES' permission. Returns a 204 empty response on success.
     * Fires a Message Delete Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#delete-message">
     *         https://discordapp.com/developers/docs/resources/channel#delete-message</a>
     */
    public static final Route<Void> MESSAGE_DELETE = Route.delete("/channels/{channel.id}/messages/{message.id}",
            Void.class);

    /**
     * Delete multiple messages in a single request. This endpoint can only be used on guild channels and requires the
     * 'MANAGE_MESSAGES' permission. Returns a 204 empty response on success. Fires multiple Message Delete Gateway
     * events.
     * <p>
     * The gateway will ignore any individual messages that do not exist or do not belong to this channel, but these
     * will count towards the minimum and maximum message count. Duplicate snowflakes will only be counted once for
     * these limits.
     * <p>
     * This endpoint will not delete messages older than 2 weeks, and will fail if any message provided is older than
     * that. An endpoint will be added in the future to prune messages older than 2 weeks from a channel.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#bulk-delete-messages">
     *         https://discordapp.com/developers/docs/resources/channel#bulk-delete-messages</a>
     */
    public static final Route<Void> MESSAGE_DELETE_BULK = Route.post("/channels/{channel.id}/messages/bulk-delete",
            Void.class);

    /**
     * Enable/disable suppression of embeds on a Message. This endpoint requires the 'MANAGE_MESSAGES' permission to
     * be present for the current user.
     * <p>
     * Returns a 204 empty response on success. Fires a Message Update Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#suppress-message-embeds">
     *         https://discordapp.com/developers/docs/resources/channel#suppress-message-embeds</a>
     */
    @Experimental
    public static final Route<Void> MESSAGE_SUPPRESS_EMBEDS = Route.post(
            "/channels/{channel.id}/messages/{message.id}/suppress-embeds", Void.class);

    /**
     * Crosspost a Message into all guilds what follow the news channel indicated. This endpoint requires the 'DISCOVERY' feature to
     * be present for the guild.
     * <p>
     * Returns a 204 empty response on success.
     */
    @Experimental
    public static final Route<Void> CROSSPOST_MESSAGE = Route.post(
        "/channels/{channel.id}/messages/{message.id}/crosspost", Void.class);

    /**
     * Edit the channel permission overwrites for a user or role in a channel. Only usable for guild channels. Requires
     * the 'MANAGE_ROLES' permission. Returns a 204 empty response on success. For more information about permissions,
     * see permissions.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#edit-channel-permissions">
     *         https://discordapp.com/developers/docs/resources/channel#edit-channel-permissions</a>
     */
    public static final Route<Void> CHANNEL_PERMISSIONS_EDIT = Route.put(
            "/channels/{channel.id}/permissions/{overwrite.id}", Void.class);

    /**
     * Returns a list of invite objects (with invite metadata) for the channel. Only usable for guild channels.
     * Requires the 'MANAGE_CHANNELS' permission.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#get-channel-invites">
     *         https://discordapp.com/developers/docs/resources/channel#get-channel-invites</a>
     */
    public static final Route<InviteResponse[]> CHANNEL_INVITES_GET = Route.get("/channels/{channel.id}/invites",
            InviteResponse[].class);

    /**
     * Create a new invite object for the channel. Only usable for guild channels. Requires the CREATE_INSTANT_INVITE
     * permission. All JSON parameters for this route are optional, however the request body is not. If you are not
     * sending any fields, you still have to send an empty JSON object ({}). Returns an invite object.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#create-channel-invite">
     *         https://discordapp.com/developers/docs/resources/channel#create-channel-invite</a>
     */
    public static final Route<InviteResponse> CHANNEL_INVITE_CREATE = Route.post("/channels/{channel.id}/invites",
            InviteResponse.class);

    /**
     * Delete a channel permission overwrite for a user or role in a channel. Only usable for guild channels. Requires
     * the 'MANAGE_ROLES' permission. Returns a 204 empty response on success. For more information about permissions,
     * see permissions.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#delete-channel-permission">
     *         https://discordapp.com/developers/docs/resources/channel#delete-channel-permission</a>
     */
    public static final Route<Void> CHANNEL_PERMISSION_DELETE = Route.delete(
            "/channels/{channel.id}/permissions/{overwrite.id}", Void.class);

    /**
     * Post a typing indicator for the specified channel. Generally bots should not implement this route. However, if a
     * bot is responding to a command and expects the computation to take a few seconds, this endpoint may be called to
     * let the user know that the bot is processing their message. Returns a 204 empty response on success. Fires a
     * Typing Start Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#trigger-typing-indicator">
     *         https://discordapp.com/developers/docs/resources/channel#trigger-typing-indicator</a>
     */
    public static final Route<Void> TYPING_INDICATOR_TRIGGER = Route.post("/channels/{channel.id}/typing", Void.class);

    /**
     * Returns all pinned messages in the channel as an array of message objects.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#get-pinned-messages">
     *         https://discordapp.com/developers/docs/resources/channel#get-pinned-messages</a>
     */
    public static final Route<MessageResponse[]> MESSAGES_PINNED_GET = Route.get("/channels/{channel.id}/pins",
            MessageResponse[].class);

    /**
     * Pin a message in a channel. Requires the 'MANAGE_MESSAGES' permission. Returns a 204 empty response on success.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#add-pinned-channel-message">
     *         https://discordapp.com/developers/docs/resources/channel#add-pinned-channel-message</a>
     */
    public static final Route<Void> MESSAGES_PINNED_ADD = Route.put("/channels/{channel.id}/pins/{message.id}",
            Void.class);

    /**
     * Delete a pinned message in a channel. Requires the 'MANAGE_MESSAGES' permission. Returns a 204 empty response on
     * success.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#delete-pinned-channel-message">
     *         https://discordapp.com/developers/docs/resources/channel#delete-pinned-channel-message</a>
     */
    public static final Route<Void> MESSAGES_PINNED_DELETE = Route.delete("/channels/{channel.id}/pins/{message.id}",
            Void.class);

    /**
     * Adds a recipient to a Group DM using their access token.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#group-dm-add-recipient">
     *         https://discordapp.com/developers/docs/resources/channel#group-dm-add-recipient</a>
     */
    public static final Route<Void> GROUP_DM_RECIPIENT_ADD = Route.put("/channels/{channel.id}/recipients/{user.id}",
            Void.class);

    /**
     * Removes a recipient from a Group DM.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/channel#group-dm-remove-recipient">
     *         https://discordapp.com/developers/docs/resources/channel#group-dm-remove-recipient</a>
     */
    public static final Route<Void> GROUP_DM_RECIPIENT_DELETE = Route.delete(
            "/channels/{channel.id}/recipients/{user.id}", Void.class);

    ////////////////////////////////////////////
    ////////////// Emoji Resource //////////////
    ////////////////////////////////////////////

    /**
     * Returns a list of emoji objects for the given guild.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/emoji#list-guild-emojis">
     *         https://discordapp.com/developers/docs/resources/emoji#list-guild-emojis</a>
     */
    public static final Route<GuildEmojiResponse[]> GUILD_EMOJIS_GET = Route.get("/guilds/{guild.id}/emojis",
            GuildEmojiResponse[].class);

    /**
     * Returns an emoji object for the given guild and emoji IDs.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/emoji#get-guild-emoji">
     *         https://discordapp.com/developers/docs/resources/emoji#get-guild-emoji</a>
     */
    public static final Route<GuildEmojiResponse> GUILD_EMOJI_GET = Route.get("/guilds/{guild.id}/emojis/{emoji.id}",
            GuildEmojiResponse.class);

    /**
     * Create a new emoji for the guild. Returns the new emoji object on success. Fires a Guild Emojis Update Gateway
     * event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/emoji#create-guild-emoji">
     *         https://discordapp.com/developers/docs/resources/emoji#create-guild-emoji</a>
     */
    public static final Route<GuildEmojiResponse> GUILD_EMOJI_CREATE = Route.post("/guilds/{guild.id}/emojis",
            GuildEmojiResponse.class);

    /**
     * Modify the given emoji. Returns the updated emoji object on success. Fires a Guild Emojis Update Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/emoji#modify-guild-emoji">
     *         https://discordapp.com/developers/docs/resources/emoji#modify-guild-emoji</a>
     */
    public static final Route<GuildEmojiResponse> GUILD_EMOJI_MODIFY = Route.patch(
            "/guilds/{guild.id}/emojis/{emoji.id}", GuildEmojiResponse.class);

    /**
     * Delete the given emoji. Returns 204 No Content on success. Fires a Guild Emojis Update Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/emoji#delete-guild-emoji">
     *         https://discordapp.com/developers/docs/resources/emoji#delete-guild-emoji</a>
     */
    public static final Route<Void> GUILD_EMOJI_DELETE = Route.delete("/guilds/{guild.id}/emojis/{emoji.id}",
            Void.class);

    ////////////////////////////////////////////
    ////////////// Guild Resource //////////////
    ////////////////////////////////////////////

    /**
     * Create a new guild. Returns a guild object on success. Fires a Guild Create Gateway event.
     * <p>
     * By default this endpoint is limited to 10 active guilds. These limits are raised for whitelisted GameBridge
     * applications.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#create-guild">
     *         https://discordapp.com/developers/docs/resources/guild#create-guild</a>
     */
    public static final Route<GuildResponse> GUILD_CREATE = Route.post("/guilds", GuildResponse.class);

    /**
     * Returns the guild object for the given id.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#get-guild">
     *         https://discordapp.com/developers/docs/resources/guild#get-guild</a>
     */
    public static final Route<GuildResponse> GUILD_GET = Route.get("/guilds/{guild.id}", GuildResponse.class);

    /**
     * Modify a guild's settings. Returns the updated guild object on success. Fires a Guild Update Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#modify-guild">
     *         https://discordapp.com/developers/docs/resources/guild#modify-guild</a>
     */
    public static final Route<GuildResponse> GUILD_MODIFY = Route.patch("/guilds/{guild.id}", GuildResponse.class);

    /**
     * Delete a guild permanently. User must be owner. Returns 204 No Content on success. Fires a Guild Delete Gateway
     * event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#delete-guild">
     *         https://discordapp.com/developers/docs/resources/guild#delete-guild</a>
     */
    public static final Route<Void> GUILD_DELETE = Route.delete("/guilds/{guild.id}", Void.class);

    /**
     * Returns a list of guild channel objects.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#get-guild-channels">
     *         https://discordapp.com/developers/docs/resources/guild#get-guild-channels</a>
     */
    public static final Route<ChannelResponse[]> GUILD_CHANNELS_GET = Route.get("/guilds/{guild.id}/channels",
            ChannelResponse[].class);

    /**
     * Create a new channel object for the guild. Requires the 'MANAGE_CHANNELS' permission. Returns the new channel
     * object on success. Fires a Channel Create Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#create-guild-channel">
     *         https://discordapp.com/developers/docs/resources/guild#create-guild-channel</a>
     */
    public static final Route<ChannelResponse> GUILD_CHANNEL_CREATE = Route.post("/guilds/{guild.id}/channels",
            ChannelResponse.class);

    /**
     * Modify the positions of a set of role objects for the guild. Requires the 'MANAGE_ROLES' permission. Returns a
     * list of all of the guild's role objects on success. Fires multiple Guild Role Update Gateway events.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#modify-guild-channel-positions">
     *         https://discordapp.com/developers/docs/resources/guild#modify-guild-channel-positions</a>
     */
    public static final Route<RoleResponse[]> GUILD_CHANNEL_POSITIONS_MODIFY = Route.patch(
            "/guilds/{guild.id}/channels", RoleResponse[].class);

    /**
     * Returns a guild member object for the specified user.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#get-guild-member">
     *         https://discordapp.com/developers/docs/resources/guild#get-guild-member</a>
     */
    public static final Route<GuildMemberResponse> GUILD_MEMBER_GET = Route.get(
            "/guilds/{guild.id}/members/{user.id}", GuildMemberResponse.class);

    /**
     * Returns a list of guild member objects that are members of the guild.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#list-guild-members">
     *         https://discordapp.com/developers/docs/resources/guild#list-guild-members</a>
     */
    public static final Route<GuildMemberResponse[]> GUILD_MEMBERS_LIST = Route.get("/guilds/{guild.id}/members",
            GuildMemberResponse[].class);

    /**
     * Adds a user to the guild, provided you have a valid oauth2 access token for the user with the guilds.join scope.
     * Returns a 201 Created with the guild member as the body. Fires a Guild Member Add Gateway event. Requires the
     * bot to have the CREATE_INSTANT_INVITE permission.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#add-guild-member">
     *         https://discordapp.com/developers/docs/resources/guild#add-guild-member</a>
     */
    public static final Route<GuildMemberResponse> GUILD_MEMBER_ADD = Route.put(
            "/guilds/{guild.id}/members/{user.id}", GuildMemberResponse.class);

    /**
     * Modify attributes of a guild member. Returns a 204 empty response on success. Fires a Guild Member Update
     * Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#modify-guild-member">
     *         https://discordapp.com/developers/docs/resources/guild#modify-guild-member</a>
     */
    public static final Route<Void> GUILD_MEMBER_MODIFY = Route.patch("/guilds/{guild.id}/members/{user.id}",
            Void.class);

    /**
     * Modifies the nickname of the current user in a guild. Returns a 200 with the nickname on success. Fires a Guild
     * Member Update Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#modify-current-user-nick">
     *         https://discordapp.com/developers/docs/resources/guild#modify-current-user-nick</a>
     */
    public static final Route<NicknameModifyResponse> NICKNAME_MODIFY_OWN = Route.patch(
            "/guilds/{guild.id}/members/@me/nick", NicknameModifyResponse.class);

    /**
     * Adds a role to a guild member. Requires the 'MANAGE_ROLES' permission. Returns a 204 empty response on success.
     * Fires a Guild Member Update Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#add-guild-member-role">
     *         https://discordapp.com/developers/docs/resources/guild#add-guild-member-role</a>
     */
    public static final Route<Void> GUILD_MEMBER_ROLE_ADD = Route.put(
            "/guilds/{guild.id}/members/{user.id}/roles/{role.id}", Void.class);

    /**
     * Removes a role from a guild member. Requires the 'MANAGE_ROLES' permission. Returns a 204 empty response on
     * success. Fires a Guild Member Update Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#remove-guild-member-role">
     *         https://discordapp.com/developers/docs/resources/guild#remove-guild-member-role</a>
     */
    public static final Route<Void> GUILD_MEMBER_ROLE_REMOVE = Route.delete(
            "/guilds/{guild.id}/members/{user.id}/roles/{role.id}", Void.class);

    /**
     * Remove a member from a guild. Requires 'KICK_MEMBERS' permission. Returns a 204 empty response on success. Fires
     * a Guild Member Remove Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#remove-guild-member">
     *         https://discordapp.com/developers/docs/resources/guild#remove-guild-member</a>
     */
    public static final Route<Void> GUILD_MEMBER_REMOVE = Route.delete("/guilds/{guild.id}/members/{user.id}",
            Void.class);

    /**
     * Returns a list of ban objects for the users banned from this guild. Requires the 'BAN_MEMBERS' permission.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#get-guild-bans">
     *         https://discordapp.com/developers/docs/resources/guild#get-guild-bans</a>
     */
    public static final Route<BanResponse[]> GUILD_BANS_GET = Route.get("/guilds/{guild.id}/bans", BanResponse[].class);

    /**
     * Returns a ban object for the given user or a 404 not found if the ban cannot be found. Requires the 'BAN_MEMBERS'
     * permission.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#get-guild-ban">
     *     https://discordapp.com/developers/docs/resources/guild#get-guild-ban</a>
     */
    public static final Route<BanResponse> GUILD_BAN_GET = Route.get("/guilds/{guild.id}/bans/{user.id}",
            BanResponse.class);

    /**
     * Create a guild ban, and optionally delete previous messages sent by the banned user. Requires the 'BAN_MEMBERS'
     * permission. Returns a 204 empty response on success. Fires a Guild Ban Add Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#create-guild-ban">
     *         https://discordapp.com/developers/docs/resources/guild#create-guild-ban</a>
     */
    public static final Route<Void> GUILD_BAN_CREATE = Route.put("/guilds/{guild.id}/bans/{user.id}", Void.class);

    /**
     * Remove the ban for a user. Requires the 'BAN_MEMBERS' permissions. Returns a 204 empty response on success.
     * Fires a Guild Ban Remove Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#remove-guild-ban">
     *         https://discordapp.com/developers/docs/resources/guild#remove-guild-ban</a>
     */
    public static final Route<Void> GUILD_BAN_REMOVE = Route.delete("/guilds/{guild.id}/bans/{user.id}", Void.class);

    /**
     * Returns a list of role objects for the guild. Requires the 'MANAGE_ROLES' permission.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#get-guild-roles">
     *         https://discordapp.com/developers/docs/resources/guild#get-guild-roles</a>
     */
    public static final Route<RoleResponse[]> GUILD_ROLES_GET = Route.get("/guilds/{guild.id}/roles",
            RoleResponse[].class);

    /**
     * Create a new role for the guild. Requires the 'MANAGE_ROLES' permission. Returns the new role object on success.
     * Fires a Guild Role Create Gateway event. All JSON params are optional.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#create-guild-role">
     *         https://discordapp.com/developers/docs/resources/guild#create-guild-role</a>
     */
    public static final Route<RoleResponse> GUILD_ROLE_CREATE = Route.post("/guilds/{guild.id}/roles",
            RoleResponse.class);

    /**
     * Modify the positions of a set of role objects for the guild. Requires the 'MANAGE_ROLES' permission. Returns a
     * list of all of the guild's role objects on success. Fires multiple Guild Role Update Gateway events.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#modify-guild-role-positions">
     *         https://discordapp.com/developers/docs/resources/guild#modify-guild-role-positions</a>
     */
    public static final Route<RoleResponse[]> GUILD_ROLE_POSITIONS_MODIFY = Route.patch("/guilds/{guild.id}/roles",
            RoleResponse[].class);

    /**
     * Modify a guild role. Requires the 'MANAGE_ROLES' permission. Returns the updated role on success. Fires a Guild
     * Role Update Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#modify-guild-role">
     *         https://discordapp.com/developers/docs/resources/guild#modify-guild-role</a>
     */
    public static final Route<RoleResponse> GUILD_ROLE_MODIFY = Route.patch("/guilds/{guild.id}/roles/{role.id}",
            RoleResponse.class);

    /**
     * Delete a guild role. Requires the 'MANAGE_ROLES' permission. Returns a 204 empty response on success. Fires a
     * Guild Role Delete Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#delete-guild-role">
     *         https://discordapp.com/developers/docs/resources/guild#delete-guild-role</a>
     */
    public static final Route<Void> GUILD_ROLE_DELETE = Route.delete("/guilds/{guild.id}/roles/{role.id}", Void.class);

    /**
     * Returns an object with one 'pruned' key indicating the number of members that would be removed in a prune
     * operation. Requires the 'KICK_MEMBERS' permission.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#get-guild-prune-count">
     *         https://discordapp.com/developers/docs/resources/guild#get-guild-prune-count</a>
     */
    public static final Route<PruneResponse> GUILD_PRUNE_COUNT_GET = Route.get("/guilds/{guild.id}/prune",
            PruneResponse.class);

    /**
     * Begin a prune operation. Requires the 'KICK_MEMBERS' permission. Returns an object with one 'pruned' key
     * indicating the number of members that were removed in the prune operation. Fires multiple Guild Member Remove
     * Gateway events.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#begin-guild-prune">
     *         https://discordapp.com/developers/docs/resources/guild#begin-guild-prune</a>
     */
    public static final Route<PruneResponse> GUILD_PRUNE_BEGIN = Route.post("/guilds/{guild.id}/prune",
            PruneResponse.class);

    /**
     * Returns a list of voice region objects for the guild. Unlike the similar /voice route, this returns VIP servers
     * when the guild is VIP-enabled.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#get-guild-voice-regions">
     *         https://discordapp.com/developers/docs/resources/guild#get-guild-voice-regions</a>
     */
    public static final Route<VoiceRegionResponse[]> GUILD_VOICE_REGIONS_GET = Route.get(
            "/guilds/{guild.id}/regions", VoiceRegionResponse[].class);

    /**
     * Returns a list of invite objects (with invite metadata) for the guild. Requires the 'MANAGE_GUILD' permission.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#get-guild-invites">
     *         https://discordapp.com/developers/docs/resources/guild#get-guild-invites</a>
     */
    public static final Route<InviteResponse[]> GUILD_INVITES_GET = Route.get("/guilds/{guild.id}/invites",
            InviteResponse[].class);

    /**
     * Returns a list of integration objects for the guild. Requires the 'MANAGE_GUILD' permission.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#get-guild-integrations">
     *         https://discordapp.com/developers/docs/resources/guild#get-guild-integrations</a>
     */
    public static final Route<IntegrationResponse[]> GUILD_INTEGRATIONS_GET = Route.get(
            "/guilds/{guild.id}/integrations", IntegrationResponse[].class);

    /**
     * Attach an integration object from the current user to the guild. Requires the 'MANAGE_GUILD' permission. Returns
     * a 204 empty response on success. Fires a Guild Integrations Update Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#create-guild-integration">
     *         https://discordapp.com/developers/docs/resources/guild#create-guild-integration</a>
     */
    public static final Route<Void> GUILD_INTEGRATION_CREATE = Route.post("/guilds/{guild.id}/integrations",
            Void.class);

    /**
     * Modify the behavior and settings of a integration object for the guild. Requires the 'MANAGE_GUILD' permission.
     * Returns a 204 empty response on success. Fires a Guild Integrations Update Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#modify-guild-integration">
     *         https://discordapp.com/developers/docs/resources/guild#modify-guild-integration</a>
     */
    public static final Route<Void> GUILD_INTEGRATION_MODIFY = Route.patch(
            "/guilds/{guild.id}/integrations/{integration.id}", Void.class);

    /**
     * Delete the attached integration object for the guild. Requires the 'MANAGE_GUILD' permission. Returns a 204
     * empty response on success. Fires a Guild Integrations Update Gateway event.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#delete-guild-integration">
     *         https://discordapp.com/developers/docs/resources/guild#delete-guild-integration</a>
     */
    public static final Route<Void> GUILD_INTEGRATION_DELETE = Route.delete(
            "/guilds/{guild.id}/integrations/{integration.id}", Void.class);

    /**
     * Sync an integration. Requires the 'MANAGE_GUILD' permission. Returns a 204 empty response on success.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#sync-guild-integration">
     *         https://discordapp.com/developers/docs/resources/guild#sync-guild-integration</a>
     */
    public static final Route<Void> GUILD_INTEGRATION_SYNC = Route.post(
            "/guilds/{guild.id}/integrations/{integration.id}/sync", Void.class);

    /**
     * Returns the guild embed object. Requires the 'MANAGE_GUILD' permission.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#get-guild-embed">
     *         https://discordapp.com/developers/docs/resources/guild#get-guild-embed</a>
     */
    public static final Route<GuildEmbedResponse> GUILD_EMBED_GET = Route.get("/guilds/{guild.id}/embed",
            GuildEmbedResponse.class);

    /**
     * Modify a guild embed object for the guild. All attributes may be passed in with JSON and modified. Requires the
     * 'MANAGE_GUILD' permission. Returns the updated guild embed object.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/guild#modify-guild-embed">
     *         https://discordapp.com/developers/docs/resources/guild#modify-guild-embed</a>
     */
    public static final Route<GuildEmbedResponse> GUILD_EMBED_MODIFY = Route.patch("/guilds/{guild.id}/embed",
            GuildEmbedResponse.class);

    /////////////////////////////////////////////
    ////////////// Invite Resource //////////////
    /////////////////////////////////////////////

    /**
     * Returns an invite object for the given code.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/invite#get-invite">
     *         https://discordapp.com/developers/docs/resources/invite#get-invite</a>
     */
    public static final Route<InviteResponse> INVITE_GET = Route.get("/invites/{invite.code}", InviteResponse.class);

    /**
     * Delete an invite. Requires the MANAGE_CHANNELS permission. Returns an invite object on success.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/invite#delete-invite">
     *         https://discordapp.com/developers/docs/resources/invite#delete-invite</a>
     */
    public static final Route<InviteResponse> INVITE_DELETE = Route.delete("/invites/{invite.code}",
            InviteResponse.class);

    /**
     * Accept an invite. This requires the guilds.join OAuth2 scope to be able to accept invites on behalf of normal
     * users (via an OAuth2 Bearer token). Bot users are disallowed. Returns an invite object on success.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/invite#accept-invite">
     *         https://discordapp.com/developers/docs/resources/invite#accept-invite</a>
     */
    public static final Route<InviteResponse> INVITE_ACCEPT = Route.post("/invites/{invite.code}",
            InviteResponse.class);

    ///////////////////////////////////////////
    ////////////// User Resource //////////////
    ///////////////////////////////////////////

    /**
     * Returns the user object of the requester's account. For OAuth2, this requires the identify scope, which will
     * return the object without an email, and optionally the email scope, which returns the object with an email.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/user#get-current-user">
     *         https://discordapp.com/developers/docs/resources/user#get-current-user</a>
     */
    public static final Route<UserResponse> CURRENT_USER_GET = Route.get("/users/@me", UserResponse.class);

    /**
     * Returns a user object for a given user ID.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/user#get-user">
     *         https://discordapp.com/developers/docs/resources/user#get-user</a>
     */
    public static final Route<UserResponse> USER_GET = Route.get("/users/{user.id}", UserResponse.class);

    /**
     * Modify the requester's user account settings. Returns a user object on success.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/user#modify-current-user">
     *         https://discordapp.com/developers/docs/resources/user#modify-current-user</a>
     */
    public static final Route<UserResponse> CURRENT_USER_MODIFY = Route.patch("/users/@me", UserResponse.class);

    /**
     * Returns a list of partial guild objects the current user is a member of. Requires the guilds OAuth2 scope.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/user#get-current-user-guilds">
     *         https://discordapp.com/developers/docs/resources/user#get-current-user-guilds</a>
     */
    public static final Route<UserGuildResponse[]> CURRENT_USER_GUILDS_GET = Route.get("/users/@me/guilds",
            UserGuildResponse[].class);

    /**
     * Leave a guild. Returns a 204 empty response on success.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/user#leave-guild">
     *         https://discordapp.com/developers/docs/resources/user#leave-guild</a>
     */
    public static final Route<Void> GUILD_LEAVE = Route.delete("/users/@me/guilds/{guild.id}", Void.class);

    /**
     * Returns a list of DM channel objects.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/user#get-user-dms">
     *         https://discordapp.com/developers/docs/resources/user#get-user-dms</a>
     */
    public static final Route<ChannelResponse[]> USER_DMS_GET = Route.get("/users/@me/channels",
            ChannelResponse[].class);

    /**
     * Create a new DM channel with a user. Returns a DM channel object.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/user#create-dm">
     *         https://discordapp.com/developers/docs/resources/user#create-dm</a>
     */
    public static final Route<ChannelResponse> USER_DM_CREATE = Route.post("/users/@me/channels",
            ChannelResponse.class);

    /**
     * Create a new group DM channel with multiple users. Returns a DM channel object.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/user#create-group-dm">
     *         https://discordapp.com/developers/docs/resources/user#create-group-dm</a>
     */
    public static final Route<ChannelResponse> GROUP_DM_CREATE = Route.post("/users/@me/channels",
            ChannelResponse.class);

    /**
     * Returns a list of connection objects. Requires the connections OAuth2 scope.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/user#get-user-connections">
     *         https://discordapp.com/developers/docs/resources/user#get-user-connections</a>
     */
    public static final Route<ConnectionResponse[]> USER_CONNECTIONS_GET = Route.get("/users/@me/connections",
            ConnectionResponse[].class);

    ////////////////////////////////////////////
    ////////////// Voice Resource //////////////
    ////////////////////////////////////////////

    /**
     * Returns an array of voice region objects that can be used when creating servers.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/voice#list-voice-regions">
     *         https://discordapp.com/developers/docs/resources/voice#list-voice-regions</a>
     */
    public static final Route<VoiceRegionResponse[]> VOICE_REGION_LIST = Route.get("/voice/regions",
            VoiceRegionResponse[].class);

    //////////////////////////////////////////////
    ////////////// Webhook Resource //////////////
    //////////////////////////////////////////////

    /**
     * Create a new webhook. Returns a webhook object on success.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/webhook#create-webhook">
     *         https://discordapp.com/developers/docs/resources/webhook#create-webhook</a>
     */
    public static final Route<WebhookResponse> CHANNEL_WEBHOOK_CREATE = Route.post("/channels/{channel.id}/webhooks",
            WebhookResponse.class);

    /**
     * Returns a list of channel webhook objects.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/webhook#get-channel-webhooks">
     *         https://discordapp.com/developers/docs/resources/webhook#get-channel-webhooks</a>
     */
    public static final Route<WebhookResponse[]> CHANNEL_WEBHOOKS_GET = Route.get("/channels/{channel.id}/webhooks",
            WebhookResponse[].class);

    /**
     * Returns a list of guild webhook objects.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/webhook#get-guild-webhooks">
     *         https://discordapp.com/developers/docs/resources/webhook#get-guild-webhooks</a>
     */
    public static final Route<WebhookResponse[]> GUILD_WEBHOOKS_GET = Route.get("/guilds/{guild.id}/webhooks",
            WebhookResponse[].class);

    /**
     * Returns the new webhook object for the given id.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/webhook#get-webhook">
     *         https://discordapp.com/developers/docs/resources/webhook#get-webhook</a>
     */
    public static final Route<WebhookResponse> WEBHOOK_GET = Route.get("/webhooks/{webhook.id}", WebhookResponse.class);

    /**
     * Same as {@link #WEBHOOK_GET}, except this call does not require authentication and returns no user in the
     * webhook object.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/webhook#get-webhook-with-token"
     *         >https://discordapp.com/developers/docs/resources/webhook#get-webhook-with-token</a>
     */
    public static final Route<WebhookResponse> WEBHOOK_TOKEN_GET = Route.get(
            "/webhooks/{webhook.id}/{webhook.token}", WebhookResponse.class);

    /**
     * Modify a webhook. Returns the updated webhook object on success. All parameters to this endpoint are optional.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/webhook#modify-webhook">
     *         https://discordapp.com/developers/docs/resources/webhook#modify-webhook</a>
     */
    public static final Route<WebhookResponse> WEBHOOK_MODIFY = Route.patch("/webhooks/{webhook.id}",
            WebhookResponse.class);

    /**
     * Same as {@link #WEBHOOK_MODIFY}, except this call does not require authentication and returns no user in the
     * webhook object.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/webhook#modify-webhook-with-token">
     *         https://discordapp.com/developers/docs/resources/webhook#modify-webhook-with-token</a>
     */
    public static final Route<WebhookResponse> WEBHOOK_TOKEN_MODIFY = Route.patch(
            "/webhooks/{webhook.id}/{webhook.token}", WebhookResponse.class); // TODO: return type wrong

    /**
     * Delete a webhook permanently. User must be owner. Returns a 204 NO CONTENT response on success.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/webhook#delete-webhook">
     *         https://discordapp.com/developers/docs/resources/webhook#delete-webhook</a>
     */
    public static final Route<Void> WEBHOOK_DELETE = Route.delete("/webhooks/{webhook.id}", Void.class);

    /**
     * Same as above, except this call does not require authentication.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/webhook#delete-webhook-with-token">
     *         https://discordapp.com/developers/docs/resources/webhook#delete-webhook-with-token</a>
     */
    public static final Route<Void> WEBHOOK_TOKEN_DELETE = Route.delete("/webhooks/{webhook.id}/{webhook.token}",
            Void.class);

    /**
     * This endpoint supports both JSON and form data bodies. It does require multipart/form-data requests instead of
     * the normal JSON request type when uploading files. Make sure you set your Content-Type to multipart/form-data if
     * you're doing that. Note that in that case, the embeds field cannot be used, but you can pass an url-encoded JSON
     * body as a form value for payload_json.
     *
     * @see <a href="https://discordapp.com/developers/docs/resources/webhook#execute-webhook">
     *         https://discordapp.com/developers/docs/resources/webhook#execute-webhook</a>
     */
    public static final Route<Void> WEBHOOK_EXECUTE = Route.post("/webhooks/{webhook.id}/{webhook.token}", Void.class);

    /**
     * @see <a href="https://discordapp.com/developers/docs/resources/webhook#execute-slackcompatible-webhook">
     *         https://discordapp.com/developers/docs/resources/webhook#execute-slackcompatible-webhook</a>
     */
    public static final Route<Void> WEBHOOK_EXECUTE_SLACK = Route.post(
            "/webhooks/{webhook.id}/{webhook.token}/slack", Void.class);

    /**
     * @see <a href="https://discordapp.com/developers/docs/resources/webhook#execute-githubcompatible-webhook">
     *         https://discordapp.com/developers/docs/resources/webhook#execute-githubcompatible-webhook</a>
     */
    public static final Route<Void> WEBHOOK_EXECUTE_GITHUB = Route.post("/webhooks/{webhook.id}/{webhook.token}/github",
            Void.class);

    /**
     * Returns the bot's OAuth2 application info.
     *
     * @see <a href=https://discordapp.com/developers/docs/topics/oauth2#get-current-application-information>
     *         https://discordapp.com/developers/docs/topics/oauth2#get-current-application-information</a>
     */
    public static final Route<ApplicationInfoResponse> APPLICATION_INFO_GET = Route.get("/oauth2/applications/@me",
            ApplicationInfoResponse.class);
}
