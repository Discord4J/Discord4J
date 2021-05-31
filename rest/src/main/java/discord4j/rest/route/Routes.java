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

/**
 * A collection of {@link discord4j.rest.route.Route} object definitions.
 *
 * @since 3.0
 */
public abstract class Routes {

    /**
     * The base URL for all API requests.
     *
     * @see <a href="https://discord.com/developers/docs/reference#base-url">
     * https://discord.com/developers/docs/reference#base-url</a>
     */
    public static final String BASE_URL = "https://discord.com/api/v8";

    //////////////////////////////////////////////
    ////////////// Gateway Resource //////////////
    //////////////////////////////////////////////

    /**
     * Returns an object with a single valid WSS URL, which the client can use as a basis for Connecting. Clients
     * should cache this value and only call this endpoint to retrieve a new URL if they are unable to properly
     * establish a connection using the cached version of the URL.
     *
     * @see <a href="https://discord.com/developers/docs/topics/gateway#get-gateway">
     * https://discord.com/developers/docs/topics/gateway#get-gateway</a>
     */
    public static final Route GATEWAY_GET = Route.get("/gateway");

    /**
     * Returns an object with the same information as Get Gateway, plus a shards key, containing the recommended number
     * of shards to connect with (as an integer). Bots that want to dynamically/automatically spawn shard processes
     * should use this endpoint to determine the number of processes to run. This route should be called once when
     * starting up numerous shards, with the response being cached and passed to all sub-shards/processes. Unlike the
     * Get Gateway, this route should not be cached for extended periods of time as the value is not guaranteed to be
     * the same per-call, and changes as the bot joins/leaves guilds.
     *
     * @see <a href="https://discord.com/developers/docs/topics/gateway#get-gateway-bot">
     * https://discord.com/developers/docs/topics/gateway#get-gateway-bot</a>
     */
    public static final Route GATEWAY_BOT_GET = Route.get("/gateway/bot");

    //////////////////////////////////////////////
    ////////////// Audit Log Resource ////////////
    //////////////////////////////////////////////

    /**
     * Returns an audit log object for the guild. Requires the 'VIEW_AUDIT_LOG' permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/audit-log#get-guild-audit-log">
     * https://discord.com/developers/docs/resources/audit-log#get-guild-audit-log</a>
     */
    public static final Route AUDIT_LOG_GET = Route.get("/guilds/{guild.id}/audit-logs");

    //////////////////////////////////////////////
    ////////////// Channel Resource //////////////
    //////////////////////////////////////////////

    /**
     * Get a channel by ID. Returns a guild channel or dm channel object.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#get-channel">
     * https://discord.com/developers/docs/resources/channel#get-channel</a>
     */
    public static final Route CHANNEL_GET = Route.get("/channels/{channel.id}");

    /**
     * Update a channels settings. Requires the 'MANAGE_CHANNELS' permission for the guild. Returns a guild channel on
     * success, and a 400 BAD REQUEST on invalid parameters. Fires a Channel Update Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#modify-channel">
     * https://discord.com/developers/docs/resources/channel#modify-channel</a>
     */
    public static final Route CHANNEL_MODIFY = Route.put("/channels/{channel.id}");

    /**
     * Update a channels settings. Requires the 'MANAGE_CHANNELS' permission for the guild. Returns a guild channel on
     * success, and a 400 BAD REQUEST on invalid parameters. Fires a Channel Update Gateway event. All the JSON Params
     * are optional.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#modify-channel">
     * https://discord.com/developers/docs/resources/channel#modify-channel</a>
     */
    public static final Route CHANNEL_MODIFY_PARTIAL = Route.patch("/channels/{channel.id}");

    /**
     * Delete a guild channel, or close a private message. Requires the 'MANAGE_CHANNELS' permission for the guild.
     * Returns a guild channel or dm channel object on success. Fires a Channel Delete Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#deleteclose-channel">
     * https://discord.com/developers/docs/resources/channel#deleteclose-channel</a>
     */
    public static final Route CHANNEL_DELETE = Route.delete("/channels/{channel.id}");

    /**
     * Returns the messages for a channel. If operating on a guild channel, this endpoint requires the 'READ_MESSAGES'
     * permission to be present on the current user. Returns an array of message objects on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#get-channel-messages">
     * https://discord.com/developers/docs/resources/channel#get-channel-messages</a>
     */
    public static final Route MESSAGES_GET = Route.get("/channels/{channel.id}/messages");

    /**
     * Returns a specific message in the channel. If operating on a guild channel, this endpoints requires the
     * 'READ_MESSAGE_HISTORY' permission to be present on the current user. Returns a message object on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#get-channel-message">
     * https://discord.com/developers/docs/resources/channel#get-channel-message</a>
     */
    public static final Route MESSAGE_GET = Route.get("/channels/{channel.id}/messages/{message.id}");

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
     * @see <a href="https://discord.com/developers/docs/resources/channel#create-message">
     * https://discord.com/developers/docs/resources/channel#create-message</a>
     */
    public static final Route MESSAGE_CREATE = Route.post("/channels/{channel.id}/messages");

    /**
     * Create a reaction for the message. This endpoint requires the 'READ_MESSAGE_HISTORY' permission to be present on
     * the current user. Additionally, if nobody else has reacted to the message using this emoji, this endpoint
     * requires the 'ADD_REACTIONS' permission to be present on the current user. Returns a 204 empty response on
     * success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#create-reaction">
     * https://discord.com/developers/docs/resources/channel#create-reaction</a>
     */
    public static final Route REACTION_CREATE = Route.put("/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/@me");

    /**
     * Delete a reaction the current user has made for the message. Returns a 204 empty response on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#delete-own-reaction">
     * https://discord.com/developers/docs/resources/channel#delete-own-reaction</a>
     */
    public static final Route REACTION_DELETE_OWN = Route.delete("/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/@me");

    /**
     * Deletes another user's reaction. This endpoint requires the 'MANAGE_MESSAGES' permission to be present on the
     * current user. Returns a 204 empty response on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#delete-user-reaction">
     * https://discord.com/developers/docs/resources/channel#delete-user-reaction</a>
     */
    public static final Route REACTION_DELETE_USER = Route.delete("/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/{user.id}");

    /**
     * Deletes all the reactions for a given emoji on a message. This endpoint requires the 'MANAGE_MESSAGES' permission
     * to be present on the current user.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#delete-all-reactions-for-emoji">
     * https://discord.com/developers/docs/resources/channel#delete-all-reactions-for-emoji</a>
     */
    public static final Route REACTION_DELETE = Route.delete("/channels/{channel.id}/messages/{message.id}/reactions/{emoji}");

    /**
     * Get a list of users that reacted with this emoji. Returns an array of user objects on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#get-reactions">
     * https://discord.com/developers/docs/resources/channel#get-reactions</a>
     */
    public static final Route REACTIONS_GET = Route.get("/channels/{channel.id}/messages/{message.id}/reactions/{emoji}");

    /**
     * Deletes all reactions on a message. This endpoint requires the 'MANAGE_MESSAGES' permission to be present on the
     * current user.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#delete-all-reactions">
     * https://discord.com/developers/docs/resources/channel#delete-all-reactions</a>
     */
    public static final Route REACTIONS_DELETE_ALL = Route.delete("/channels/{channel.id}/messages/{message.id}/reactions");

    /**
     * Edit a previously sent message. You can only edit messages that have been sent by the current user. Returns a
     * message object. Fires a Message Update Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#edit-message">
     * https://discord.com/developers/docs/resources/channel#edit-message</a>
     */
    public static final Route MESSAGE_EDIT = Route.patch("/channels/{channel.id}/messages/{message.id}");

    /**
     * Delete a message. If operating on a guild channel and trying to delete a message that was not sent by the
     * current user, this endpoint requires the 'MANAGE_MESSAGES' permission. Returns a 204 empty response on success.
     * Fires a Message Delete Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#delete-message">
     * https://discord.com/developers/docs/resources/channel#delete-message</a>
     */
    public static final Route MESSAGE_DELETE = Route.delete("/channels/{channel.id}/messages/{message.id}");

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
     * @see <a href="https://discord.com/developers/docs/resources/channel#bulk-delete-messages">
     * https://discord.com/developers/docs/resources/channel#bulk-delete-messages</a>
     */
    public static final Route MESSAGE_DELETE_BULK = Route.post("/channels/{channel.id}/messages/bulk-delete");

    /**
     * Enable/disable suppression of embeds on a Message. This endpoint requires the 'MANAGE_MESSAGES' permission to
     * be present for the current user.
     * <p>
     * Returns a 204 empty response on success. Fires a Message Update Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#suppress-message-embeds">
     * https://discord.com/developers/docs/resources/channel#suppress-message-embeds</a>
     */
    @Experimental
    public static final Route MESSAGE_SUPPRESS_EMBEDS = Route.post("/channels/{channel.id}/messages/{message.id}/suppress-embeds");

    /**
     * Crosspost a Message into all guilds what follow the news channel indicated. This endpoint requires the
     * 'DISCOVERY' feature to be present for the guild and requires the 'SEND_MESSAGES' permission, if the current user
     * sent the message, or additionally the 'MANAGE_MESSAGES' permission, for all other messages, to be present for
     * the current user.
     * <p>
     * Returns a 204 empty response on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#crosspost-message">
     * https://discord.com/developers/docs/resources/channel#crosspost-message</a>
     */
    public static final Route CROSSPOST_MESSAGE = Route.post("/channels/{channel.id}/messages/{message.id}/crosspost");

    /**
     * Edit the channel permission overwrites for a user or role in a channel. Only usable for guild channels. Requires
     * the 'MANAGE_ROLES' permission. Returns a 204 empty response on success. For more information about permissions,
     * see permissions.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#edit-channel-permissions">
     * https://discord.com/developers/docs/resources/channel#edit-channel-permissions</a>
     */
    public static final Route CHANNEL_PERMISSIONS_EDIT = Route.put("/channels/{channel.id}/permissions/{overwrite.id}");

    /**
     * Returns a list of invite objects (with invite metadata) for the channel. Only usable for guild channels.
     * Requires the 'MANAGE_CHANNELS' permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#get-channel-invites">
     * https://discord.com/developers/docs/resources/channel#get-channel-invites</a>
     */
    public static final Route CHANNEL_INVITES_GET = Route.get("/channels/{channel.id}/invites");

    /**
     * Create a new invite object for the channel. Only usable for guild channels. Requires the CREATE_INSTANT_INVITE
     * permission. All JSON parameters for this route are optional, however the request body is not. If you are not
     * sending any fields, you still have to send an empty JSON object ({}). Returns an invite object.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#create-channel-invite">
     * https://discord.com/developers/docs/resources/channel#create-channel-invite</a>
     */
    public static final Route CHANNEL_INVITE_CREATE = Route.post("/channels/{channel.id}/invites");

    /**
     * Delete a channel permission overwrite for a user or role in a channel. Only usable for guild channels. Requires
     * the 'MANAGE_ROLES' permission. Returns a 204 empty response on success. For more information about permissions,
     * see permissions.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#delete-channel-permission">
     * https://discord.com/developers/docs/resources/channel#delete-channel-permission</a>
     */
    public static final Route CHANNEL_PERMISSION_DELETE = Route.delete("/channels/{channel.id}/permissions/{overwrite.id}");

    /**
     * Follow a News Channel to send messages to a target channel. Requires the `MANAGE_WEBHOOKS` permission in the
     * target channel. Returns a followed channel object.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#follow-news-channel">
     * https://discord.com/developers/docs/resources/channel#follow-news-channel</a>
     */
    public static final Route FOLLOW_NEWS_CHANNEL = Route.post("/channels/{channel.id}/followers");

    /**
     * Post a typing indicator for the specified channel. Generally bots should not implement this route. However, if a
     * bot is responding to a command and expects the computation to take a few seconds, this endpoint may be called to
     * let the user know that the bot is processing their message. Returns a 204 empty response on success. Fires a
     * Typing Start Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#trigger-typing-indicator">
     * https://discord.com/developers/docs/resources/channel#trigger-typing-indicator</a>
     */
    public static final Route TYPING_INDICATOR_TRIGGER = Route.post("/channels/{channel.id}/typing");

    /**
     * Returns all pinned messages in the channel as an array of message objects.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#get-pinned-messages">
     * https://discord.com/developers/docs/resources/channel#get-pinned-messages</a>
     */
    public static final Route MESSAGES_PINNED_GET = Route.get("/channels/{channel.id}/pins");

    /**
     * Pin a message in a channel. Requires the 'MANAGE_MESSAGES' permission. Returns a 204 empty response on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#add-pinned-channel-message">
     * https://discord.com/developers/docs/resources/channel#add-pinned-channel-message</a>
     */
    public static final Route MESSAGES_PINNED_ADD = Route.put("/channels/{channel.id}/pins/{message.id}");

    /**
     * Delete a pinned message in a channel. Requires the 'MANAGE_MESSAGES' permission. Returns a 204 empty response on
     * success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#delete-pinned-channel-message">
     * https://discord.com/developers/docs/resources/channel#delete-pinned-channel-message</a>
     */
    public static final Route MESSAGES_PINNED_DELETE = Route.delete("/channels/{channel.id}/pins/{message.id}");

    /**
     * Adds a recipient to a Group DM using their access token.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#group-dm-add-recipient">
     * https://discord.com/developers/docs/resources/channel#group-dm-add-recipient</a>
     */
    public static final Route GROUP_DM_RECIPIENT_ADD = Route.put("/channels/{channel.id}/recipients/{user.id}");

    /**
     * Removes a recipient from a Group DM.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#group-dm-remove-recipient">
     * https://discord.com/developers/docs/resources/channel#group-dm-remove-recipient</a>
     */
    public static final Route GROUP_DM_RECIPIENT_DELETE = Route.delete("/channels/{channel.id}/recipients/{user.id}");

    ////////////////////////////////////////////
    ////////////// Emoji Resource //////////////
    ////////////////////////////////////////////

    /**
     * Returns a list of emoji objects for the given guild.
     *
     * @see <a href="https://discord.com/developers/docs/resources/emoji#list-guild-emojis">
     * https://discord.com/developers/docs/resources/emoji#list-guild-emojis</a>
     */
    public static final Route GUILD_EMOJIS_GET = Route.get("/guilds/{guild.id}/emojis");

    /**
     * Returns an emoji object for the given guild and emoji IDs.
     *
     * @see <a href="https://discord.com/developers/docs/resources/emoji#get-guild-emoji">
     * https://discord.com/developers/docs/resources/emoji#get-guild-emoji</a>
     */
    public static final Route GUILD_EMOJI_GET = Route.get("/guilds/{guild.id}/emojis/{emoji.id}");

    /**
     * Create a new emoji for the guild. Returns the new emoji object on success. Fires a Guild Emojis Update Gateway
     * event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/emoji#create-guild-emoji">
     * https://discord.com/developers/docs/resources/emoji#create-guild-emoji</a>
     */
    public static final Route GUILD_EMOJI_CREATE = Route.post("/guilds/{guild.id}/emojis");

    /**
     * Modify the given emoji. Returns the updated emoji object on success. Fires a Guild Emojis Update Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/emoji#modify-guild-emoji">
     * https://discord.com/developers/docs/resources/emoji#modify-guild-emoji</a>
     */
    public static final Route GUILD_EMOJI_MODIFY = Route.patch("/guilds/{guild.id}/emojis/{emoji.id}");

    /**
     * Delete the given emoji. Returns 204 No Content on success. Fires a Guild Emojis Update Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/emoji#delete-guild-emoji">
     * https://discord.com/developers/docs/resources/emoji#delete-guild-emoji</a>
     */
    public static final Route GUILD_EMOJI_DELETE = Route.delete("/guilds/{guild.id}/emojis/{emoji.id}");

    ////////////////////////////////////////////
    ////////////// Guild Resource //////////////
    ////////////////////////////////////////////

    /**
     * Create a new guild. Returns a guild object on success. Fires a Guild Create Gateway event.
     * <p>
     * By default this endpoint is limited to 10 active guilds. These limits are raised for whitelisted GameBridge
     * applications.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#create-guild">
     * https://discord.com/developers/docs/resources/guild#create-guild</a>
     */
    public static final Route GUILD_CREATE = Route.post("/guilds");

    /**
     * Returns the guild object for the given id.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#get-guild">
     * https://discord.com/developers/docs/resources/guild#get-guild</a>
     */
    public static final Route GUILD_GET = Route.get("/guilds/{guild.id}");

    /**
     * Modify a guild's settings. Returns the updated guild object on success. Fires a Guild Update Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#modify-guild">
     * https://discord.com/developers/docs/resources/guild#modify-guild</a>
     */
    public static final Route GUILD_MODIFY = Route.patch("/guilds/{guild.id}");

    /**
     * Delete a guild permanently. User must be owner. Returns 204 No Content on success. Fires a Guild Delete Gateway
     * event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#delete-guild">
     * https://discord.com/developers/docs/resources/guild#delete-guild</a>
     */
    public static final Route GUILD_DELETE = Route.delete("/guilds/{guild.id}");

    /**
     * Returns a list of guild channel objects.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#get-guild-channels">
     * https://discord.com/developers/docs/resources/guild#get-guild-channels</a>
     */
    public static final Route GUILD_CHANNELS_GET = Route.get("/guilds/{guild.id}/channels");

    /**
     * Create a new channel object for the guild. Requires the 'MANAGE_CHANNELS' permission. Returns the new channel
     * object on success. Fires a Channel Create Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#create-guild-channel">
     * https://discord.com/developers/docs/resources/guild#create-guild-channel</a>
     */
    public static final Route GUILD_CHANNEL_CREATE = Route.post("/guilds/{guild.id}/channels");

    /**
     * Modify the positions of a set of role objects for the guild. Requires the 'MANAGE_ROLES' permission. Returns a
     * list of all of the guild's role objects on success. Fires multiple Guild Role Update Gateway events.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#modify-guild-channel-positions">
     * https://discord.com/developers/docs/resources/guild#modify-guild-channel-positions</a>
     */
    public static final Route GUILD_CHANNEL_POSITIONS_MODIFY = Route.patch("/guilds/{guild.id}/channels");

    /**
     * Returns a guild member object for the specified user.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#get-guild-member">
     * https://discord.com/developers/docs/resources/guild#get-guild-member</a>
     */
    public static final Route GUILD_MEMBER_GET = Route.get("/guilds/{guild.id}/members/{user.id}");

    /**
     * Returns a list of guild member objects that are members of the guild.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#list-guild-members">
     * https://discord.com/developers/docs/resources/guild#list-guild-members</a>
     */
    public static final Route GUILD_MEMBERS_LIST = Route.get("/guilds/{guild.id}/members");

    /**
     * Returns a list of guild member objects whose username or nickname starts with a provided string.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#search-guild-members">
     * https://discord.com/developers/docs/resources/guild#search-guild-members</a>
     */
    public static final Route SEARCH_GUILD_MEMBERS_GET = Route.get("/guilds/{guild.id}/members/search");

    /**
     * Adds a user to the guild, provided you have a valid oauth2 access token for the user with the guilds.join scope.
     * Returns a 201 Created with the guild member as the body. Fires a Guild Member Add Gateway event. Requires the
     * bot to have the CREATE_INSTANT_INVITE permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#add-guild-member">
     * https://discord.com/developers/docs/resources/guild#add-guild-member</a>
     */
    public static final Route GUILD_MEMBER_ADD = Route.put("/guilds/{guild.id}/members/{user.id}");

    /**
     * Modify attributes of a guild member. Returns a 200 OK with the guild member on success. Fires a Guild Member
     * Update Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#modify-guild-member">
     * https://discord.com/developers/docs/resources/guild#modify-guild-member</a>
     */
    public static final Route GUILD_MEMBER_MODIFY = Route.patch("/guilds/{guild.id}/members/{user.id}");

    /**
     * Modifies the nickname of the current user in a guild. Returns a 200 with the nickname on success. Fires a Guild
     * Member Update Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#modify-current-user-nick">
     * https://discord.com/developers/docs/resources/guild#modify-current-user-nick</a>
     */
    public static final Route NICKNAME_MODIFY_OWN = Route.patch("/guilds/{guild.id}/members/@me/nick");

    /**
     * Adds a role to a guild member. Requires the 'MANAGE_ROLES' permission. Returns a 204 empty response on success.
     * Fires a Guild Member Update Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#add-guild-member-role">
     * https://discord.com/developers/docs/resources/guild#add-guild-member-role</a>
     */
    public static final Route GUILD_MEMBER_ROLE_ADD = Route.put("/guilds/{guild.id}/members/{user.id}/roles/{role.id}");

    /**
     * Removes a role from a guild member. Requires the 'MANAGE_ROLES' permission. Returns a 204 empty response on
     * success. Fires a Guild Member Update Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#remove-guild-member-role">
     * https://discord.com/developers/docs/resources/guild#remove-guild-member-role</a>
     */
    public static final Route GUILD_MEMBER_ROLE_REMOVE = Route.delete("/guilds/{guild.id}/members/{user.id}/roles/{role.id}");

    /**
     * Remove a member from a guild. Requires 'KICK_MEMBERS' permission. Returns a 204 empty response on success. Fires
     * a Guild Member Remove Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#remove-guild-member">
     * https://discord.com/developers/docs/resources/guild#remove-guild-member</a>
     */
    public static final Route GUILD_MEMBER_REMOVE = Route.delete("/guilds/{guild.id}/members/{user.id}");

    /**
     * Returns a list of ban objects for the users banned from this guild. Requires the 'BAN_MEMBERS' permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#get-guild-bans">
     * https://discord.com/developers/docs/resources/guild#get-guild-bans</a>
     */
    public static final Route GUILD_BANS_GET = Route.get("/guilds/{guild.id}/bans");

    /**
     * Returns a ban object for the given user or a 404 not found if the ban cannot be found. Requires the 'BAN_MEMBERS'
     * permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#get-guild-ban">
     * https://discord.com/developers/docs/resources/guild#get-guild-ban</a>
     */
    public static final Route GUILD_BAN_GET = Route.get("/guilds/{guild.id}/bans/{user.id}");

    /**
     * Create a guild ban, and optionally delete previous messages sent by the banned user. Requires the 'BAN_MEMBERS'
     * permission. Returns a 204 empty response on success. Fires a Guild Ban Add Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#create-guild-ban">
     * https://discord.com/developers/docs/resources/guild#create-guild-ban</a>
     */
    public static final Route GUILD_BAN_CREATE = Route.put("/guilds/{guild.id}/bans/{user.id}");

    /**
     * Remove the ban for a user. Requires the 'BAN_MEMBERS' permissions. Returns a 204 empty response on success.
     * Fires a Guild Ban Remove Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#remove-guild-ban">
     * https://discord.com/developers/docs/resources/guild#remove-guild-ban</a>
     */
    public static final Route GUILD_BAN_REMOVE = Route.delete("/guilds/{guild.id}/bans/{user.id}");

    /**
     * Returns a list of role objects for the guild. Requires the 'MANAGE_ROLES' permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#get-guild-roles">
     * https://discord.com/developers/docs/resources/guild#get-guild-roles</a>
     */
    public static final Route GUILD_ROLES_GET = Route.get("/guilds/{guild.id}/roles");

    /**
     * Create a new role for the guild. Requires the 'MANAGE_ROLES' permission. Returns the new role object on success.
     * Fires a Guild Role Create Gateway event. All JSON params are optional.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#create-guild-role">
     * https://discord.com/developers/docs/resources/guild#create-guild-role</a>
     */
    public static final Route GUILD_ROLE_CREATE = Route.post("/guilds/{guild.id}/roles");

    /**
     * Modify the positions of a set of role objects for the guild. Requires the 'MANAGE_ROLES' permission. Returns a
     * list of all of the guild's role objects on success. Fires multiple Guild Role Update Gateway events.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#modify-guild-role-positions">
     * https://discord.com/developers/docs/resources/guild#modify-guild-role-positions</a>
     */
    public static final Route GUILD_ROLE_POSITIONS_MODIFY = Route.patch("/guilds/{guild.id}/roles");

    /**
     * Modify a guild role. Requires the 'MANAGE_ROLES' permission. Returns the updated role on success. Fires a Guild
     * Role Update Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#modify-guild-role">
     * https://discord.com/developers/docs/resources/guild#modify-guild-role</a>
     */
    public static final Route GUILD_ROLE_MODIFY = Route.patch("/guilds/{guild.id}/roles/{role.id}");

    /**
     * Delete a guild role. Requires the 'MANAGE_ROLES' permission. Returns a 204 empty response on success. Fires a
     * Guild Role Delete Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#delete-guild-role">
     * https://discord.com/developers/docs/resources/guild#delete-guild-role</a>
     */
    public static final Route GUILD_ROLE_DELETE = Route.delete("/guilds/{guild.id}/roles/{role.id}");

    /**
     * Returns an object with one 'pruned' key indicating the number of members that would be removed in a prune
     * operation. Requires the 'KICK_MEMBERS' permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#get-guild-prune-count">
     * https://discord.com/developers/docs/resources/guild#get-guild-prune-count</a>
     */
    public static final Route GUILD_PRUNE_COUNT_GET = Route.get("/guilds/{guild.id}/prune");

    /**
     * Begin a prune operation. Requires the 'KICK_MEMBERS' permission. Returns an object with one 'pruned' key
     * indicating the number of members that were removed in the prune operation. Fires multiple Guild Member Remove
     * Gateway events.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#begin-guild-prune">
     * https://discord.com/developers/docs/resources/guild#begin-guild-prune</a>
     */
    public static final Route GUILD_PRUNE_BEGIN = Route.post("/guilds/{guild.id}/prune");

    /**
     * Returns a list of voice region objects for the guild. Unlike the similar /voice route, this returns VIP servers
     * when the guild is VIP-enabled.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#get-guild-voice-regions">
     * https://discord.com/developers/docs/resources/guild#get-guild-voice-regions</a>
     */
    public static final Route GUILD_VOICE_REGIONS_GET = Route.get("/guilds/{guild.id}/regions");

    /**
     * Returns a list of invite objects (with invite metadata) for the guild. Requires the 'MANAGE_GUILD' permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#get-guild-invites">
     * https://discord.com/developers/docs/resources/guild#get-guild-invites</a>
     */
    public static final Route GUILD_INVITES_GET = Route.get("/guilds/{guild.id}/invites");

    /**
     * Returns a list of integration objects for the guild. Requires the 'MANAGE_GUILD' permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#get-guild-integrations">
     * https://discord.com/developers/docs/resources/guild#get-guild-integrations</a>
     */
    public static final Route GUILD_INTEGRATIONS_GET = Route.get("/guilds/{guild.id}/integrations");

    /**
     * Attach an integration object from the current user to the guild. Requires the 'MANAGE_GUILD' permission. Returns
     * a 204 empty response on success. Fires a Guild Integrations Update Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#create-guild-integration">
     * https://discord.com/developers/docs/resources/guild#create-guild-integration</a>
     */
    public static final Route GUILD_INTEGRATION_CREATE = Route.post("/guilds/{guild.id}/integrations");

    /**
     * Modify the behavior and settings of a integration object for the guild. Requires the 'MANAGE_GUILD' permission.
     * Returns a 204 empty response on success. Fires a Guild Integrations Update Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#modify-guild-integration">
     * https://discord.com/developers/docs/resources/guild#modify-guild-integration</a>
     */
    public static final Route GUILD_INTEGRATION_MODIFY = Route.patch("/guilds/{guild.id}/integrations/{integration.id}");

    /**
     * Delete the attached integration object for the guild. Requires the 'MANAGE_GUILD' permission. Returns a 204
     * empty response on success. Fires a Guild Integrations Update Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#delete-guild-integration">
     * https://discord.com/developers/docs/resources/guild#delete-guild-integration</a>
     */
    public static final Route GUILD_INTEGRATION_DELETE = Route.delete("/guilds/{guild.id}/integrations/{integration.id}");

    /**
     * Sync an integration. Requires the 'MANAGE_GUILD' permission. Returns a 204 empty response on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#sync-guild-integration">
     * https://discord.com/developers/docs/resources/guild#sync-guild-integration</a>
     */
    public static final Route GUILD_INTEGRATION_SYNC = Route.post("/guilds/{guild.id}/integrations/{integration.id}/sync");

    /**
     * Returns the guild widget object. Requires the 'MANAGE_GUILD' permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#get-guild-widget">
     * https://discord.com/developers/docs/resources/guild#get-guild-widget</a>
     */
    public static final Route GUILD_WIDGET_GET = Route.get("/guilds/{guild.id}/widget");

    /**
     * Modify a guild widget object for the guild. All attributes may be passed in with JSON and modified. Requires the
     * 'MANAGE_GUILD' permission. Returns the updated guild widget object.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#modify-guild-widget">
     * https://discord.com/developers/docs/resources/guild#modify-guild-widget</a>
     */
    public static final Route GUILD_WIDGET_MODIFY = Route.patch("/guilds/{guild.id}/widget");

    /**
     * Returns the guild preview object. If the user is not in the guild, then the guild must be Discoverable.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#get-guild-preview">
     * https://discord.com/developers/docs/resources/guild#get-guild-preview</a>
     */
    public static final Route GUILD_PREVIEW_GET = Route.get("/guilds/{guild.id}/preview");

    /**
     * Updates the current user's voice state.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#update-self-voice-state">
     * https://discord.com/developers/docs/resources/guild#update-self-voice-state</a>
     */
    public static final Route SELF_VOICE_STATE_MODIFY = Route.patch("/guilds/{guild.id}/voice-states/@me");

    /**
     * Updates another user's voice state.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#update-others-voice-state">
     * https://discord.com/developers/docs/resources/guild#update-others-voice-state</a>
     */
    public static final Route OTHERS_VOICE_STATE_MODIFY = Route.patch("/guilds/{guild.id}/voice-states/{user.id}");

    /////////////////////////////////////////////
    ////////////// Invite Resource //////////////
    /////////////////////////////////////////////

    /**
     * Returns an invite object for the given code.
     *
     * @see <a href="https://discord.com/developers/docs/resources/invite#get-invite">
     * https://discord.com/developers/docs/resources/invite#get-invite</a>
     */
    public static final Route INVITE_GET = Route.get("/invites/{invite.code}");

    /**
     * Delete an invite. Requires the MANAGE_CHANNELS permission. Returns an invite object on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/invite#delete-invite">
     * https://discord.com/developers/docs/resources/invite#delete-invite</a>
     */
    public static final Route INVITE_DELETE = Route.delete("/invites/{invite.code}");

    /**
     * Accept an invite. This requires the guilds.join OAuth2 scope to be able to accept invites on behalf of normal
     * users (via an OAuth2 Bearer token). Bot users are disallowed. Returns an invite object on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/invite#accept-invite">
     * https://discord.com/developers/docs/resources/invite#accept-invite</a>
     */
    public static final Route INVITE_ACCEPT = Route.post("/invites/{invite.code}");

    /////////////////////////////////////////////
    ////////////// Template Resource ////////////
    /////////////////////////////////////////////

    /**
     * Get a template. Returns a template object for the given code on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/template#get-template">
     *         https://discord.com/developers/docs/resources/template#get-template</a>
     */
    public static final Route GUILD_TEMPLATE_GET = Route.get("/guilds/templates/{template.code}");

    /**
     * Create a new guild from template. Returns a guild object on success. Fires a Guild Create Gateway event.
     *
     * By default this endpoint can be used only by bots in less than 10 guilds.
     *
     * @see <a href="https://discord.com/developers/docs/resources/template#create-guild-from-template">
     *         https://discord.com/developers/docs/resources/template#create-guild-from-template</a>
     */
    public static final Route TEMPLATE_GUILD_CREATE = Route.post("/guilds/templates/{template.code}");

    /**
     * Returns an array of template objects. Requires the MANAGE_GUILD permission. Returns an array of template objects.
     *
     * @see <a href="https://discord.com/developers/docs/resources/template#get-guild-templates">
     *         https://discord.com/developers/docs/resources/template#get-guild-templates</a>
     */
    public static final Route GUILD_TEMPLATE_LIST_GET = Route.get("/guilds/{guild.id}/templates");

    /**
     * Creates a template for the guild. Requires the MANAGE_GUILD permission. Returns the created template object on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/template#create-guild-template">
     *         https://discord.com/developers/docs/resources/template#create-guild-template</a>
     */
    public static final Route GUILD_TEMPLATE_CREATE = Route.post("/guilds/{guild.id}/templates");

    /**
     * Syncs the template to the guild's current state. Requires the MANAGE_GUILD permission. Returns the template object on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/template#sync-guild-template">
     *         https://discord.com/developers/docs/resources/template#sync-guild-template</a>
     */
    public static final Route GUILD_TEMPLATE_SYNC = Route.put("/guilds/{guild.id}/templates/{template.code}");

    /**
     * Modifies the template's metadata. Requires the MANAGE_GUILD permission. Returns the template object on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/template#modify-guild-template">
     *         https://discord.com/developers/docs/resources/template#modify-guild-template</a>
     */
    public static final Route GUILD_TEMPLATE_MODIFY = Route.patch("/guilds/{guild.id}/templates/{template.code}");

    /**
     * Deletes the template. Requires the MANAGE_GUILD permission. Returns the deleted template object on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/template#delete-guild-template">
     *         https://discord.com/developers/docs/resources/template#delete-guild-template</a>
     */
    public static final Route GUILD_TEMPLATE_DELETE = Route.delete("/guilds/{guild.id}/templates/{template.code}");

    ///////////////////////////////////////////
    ////////////// User Resource //////////////
    ///////////////////////////////////////////

    /**
     * Returns the user object of the requester's account. For OAuth2, this requires the identify scope, which will
     * return the object without an email, and optionally the email scope, which returns the object with an email.
     *
     * @see <a href="https://discord.com/developers/docs/resources/user#get-current-user">
     * https://discord.com/developers/docs/resources/user#get-current-user</a>
     */
    public static final Route CURRENT_USER_GET = Route.get("/users/@me");

    /**
     * Returns a user object for a given user ID.
     *
     * @see <a href="https://discord.com/developers/docs/resources/user#get-user">
     * https://discord.com/developers/docs/resources/user#get-user</a>
     */
    public static final Route USER_GET = Route.get("/users/{user.id}");

    /**
     * Modify the requester's user account settings. Returns a user object on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/user#modify-current-user">
     * https://discord.com/developers/docs/resources/user#modify-current-user</a>
     */
    public static final Route CURRENT_USER_MODIFY = Route.patch("/users/@me");

    /**
     * Returns a list of partial guild objects the current user is a member of. Requires the guilds OAuth2 scope.
     *
     * @see <a href="https://discord.com/developers/docs/resources/user#get-current-user-guilds">
     * https://discord.com/developers/docs/resources/user#get-current-user-guilds</a>
     */
    public static final Route CURRENT_USER_GUILDS_GET = Route.get("/users/@me/guilds");

    /**
     * Leave a guild. Returns a 204 empty response on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/user#leave-guild">
     * https://discord.com/developers/docs/resources/user#leave-guild</a>
     */
    public static final Route GUILD_LEAVE = Route.delete("/users/@me/guilds/{guild.id}");

    /**
     * Create a new DM channel with a user. Returns a DM channel object.
     *
     * @see <a href="https://discord.com/developers/docs/resources/user#create-dm">
     * https://discord.com/developers/docs/resources/user#create-dm</a>
     */
    public static final Route USER_DM_CREATE = Route.post("/users/@me/channels");

    /**
     * Create a new group DM channel with multiple users. Returns a DM channel object.
     *
     * @see <a href="https://discord.com/developers/docs/resources/user#create-group-dm">
     * https://discord.com/developers/docs/resources/user#create-group-dm</a>
     */
    public static final Route GROUP_DM_CREATE = Route.post("/users/@me/channels");

    /**
     * Returns a list of connection objects. Requires the connections OAuth2 scope.
     *
     * @see <a href="https://discord.com/developers/docs/resources/user#get-user-connections">
     * https://discord.com/developers/docs/resources/user#get-user-connections</a>
     */
    public static final Route USER_CONNECTIONS_GET = Route.get("/users/@me/connections");

    ////////////////////////////////////////////
    ////////////// Voice Resource //////////////
    ////////////////////////////////////////////

    /**
     * Returns an array of voice region objects that can be used when creating servers.
     *
     * @see <a href="https://discord.com/developers/docs/resources/voice#list-voice-regions">
     * https://discord.com/developers/docs/resources/voice#list-voice-regions</a>
     */
    public static final Route VOICE_REGION_LIST = Route.get("/voice/regions");

    //////////////////////////////////////////////
    ////////////// Webhook Resource //////////////
    //////////////////////////////////////////////

    /**
     * Create a new webhook. Returns a webhook object on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/webhook#create-webhook">
     * https://discord.com/developers/docs/resources/webhook#create-webhook</a>
     */
    public static final Route CHANNEL_WEBHOOK_CREATE = Route.post("/channels/{channel.id}/webhooks");

    /**
     * Returns a list of channel webhook objects.
     *
     * @see <a href="https://discord.com/developers/docs/resources/webhook#get-channel-webhooks">
     * https://discord.com/developers/docs/resources/webhook#get-channel-webhooks</a>
     */
    public static final Route CHANNEL_WEBHOOKS_GET = Route.get("/channels/{channel.id}/webhooks");

    /**
     * Returns a list of guild webhook objects.
     *
     * @see <a href="https://discord.com/developers/docs/resources/webhook#get-guild-webhooks">
     * https://discord.com/developers/docs/resources/webhook#get-guild-webhooks</a>
     */
    public static final Route GUILD_WEBHOOKS_GET = Route.get("/guilds/{guild.id}/webhooks");

    /**
     * Returns the new webhook object for the given id.
     *
     * @see <a href="https://discord.com/developers/docs/resources/webhook#get-webhook">
     * https://discord.com/developers/docs/resources/webhook#get-webhook</a>
     */
    public static final Route WEBHOOK_GET = Route.get("/webhooks/{webhook.id}");

    /**
     * Same as {@link #WEBHOOK_GET}, except this call does not require authentication and returns no user in the
     * webhook object.
     *
     * @see <a href="https://discord.com/developers/docs/resources/webhook#get-webhook-with-token"
     * >https://discord.com/developers/docs/resources/webhook#get-webhook-with-token</a>
     */
    public static final Route WEBHOOK_TOKEN_GET = Route.get("/webhooks/{webhook.id}/{webhook.token}");

    /**
     * Modify a webhook. Returns the updated webhook object on success. All parameters to this endpoint are optional.
     *
     * @see <a href="https://discord.com/developers/docs/resources/webhook#modify-webhook">
     * https://discord.com/developers/docs/resources/webhook#modify-webhook</a>
     */
    public static final Route WEBHOOK_MODIFY = Route.patch("/webhooks/{webhook.id}");

    /**
     * Same as {@link #WEBHOOK_MODIFY}, except this call does not require authentication and returns no user in the
     * webhook object.
     *
     * @see <a href="https://discord.com/developers/docs/resources/webhook#modify-webhook-with-token">
     * https://discord.com/developers/docs/resources/webhook#modify-webhook-with-token</a>
     */
    public static final Route WEBHOOK_TOKEN_MODIFY = Route.patch("/webhooks/{webhook.id}/{webhook.token}");

    /**
     * Delete a webhook permanently. User must be owner. Returns a 204 NO CONTENT response on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/webhook#delete-webhook">
     * https://discord.com/developers/docs/resources/webhook#delete-webhook</a>
     */
    public static final Route WEBHOOK_DELETE = Route.delete("/webhooks/{webhook.id}");

    /**
     * Same as above, except this call does not require authentication.
     *
     * @see <a href="https://discord.com/developers/docs/resources/webhook#delete-webhook-with-token">
     * https://discord.com/developers/docs/resources/webhook#delete-webhook-with-token</a>
     */
    public static final Route WEBHOOK_TOKEN_DELETE = Route.delete("/webhooks/{webhook.id}/{webhook.token}");

    /**
     * This endpoint supports both JSON and form data bodies. It does require multipart/form-data requests instead of
     * the normal JSON request type when uploading files. Make sure you set your Content-Type to multipart/form-data if
     * you're doing that. Note that in that case, the embeds field cannot be used, but you can pass an url-encoded JSON
     * body as a form value for payload_json.
     *
     * @see <a href="https://discord.com/developers/docs/resources/webhook#execute-webhook">
     * https://discord.com/developers/docs/resources/webhook#execute-webhook</a>
     */
    public static final Route WEBHOOK_EXECUTE = Route.post("/webhooks/{webhook.id}/{webhook.token}");

    /**
     * @see <a href="https://discord.com/developers/docs/resources/webhook#execute-slackcompatible-webhook">
     * https://discord.com/developers/docs/resources/webhook#execute-slackcompatible-webhook</a>
     */
    public static final Route WEBHOOK_EXECUTE_SLACK = Route.post("/webhooks/{webhook.id}/{webhook.token}/slack");

    /**
     * @see <a href="https://discord.com/developers/docs/resources/webhook#execute-githubcompatible-webhook">
     * https://discord.com/developers/docs/resources/webhook#execute-githubcompatible-webhook</a>
     */
    public static final Route WEBHOOK_EXECUTE_GITHUB = Route.post("/webhooks/{webhook.id}/{webhook.token}/github");

    /**
     * @see <a href="https://discord.com/developers/docs/resources/webhook#edit-webhook-message">
     * https://discord.com/developers/docs/resources/webhook#edit-webhook-message</a>
     */
    public static final Route WEBHOOK_MESSAGE_EDIT = Route.patch("/webhooks/{webhook.id}/{webhook.token}/messages/{message.id}");

    /**
     * @see <a href="https://discord.com/developers/docs/resources/webhook#delete-webhook-message">
     * https://discord.com/developers/docs/resources/webhook#delete-webhook-message</a>
     */
    public static final Route WEBHOOK_MESSAGE_DELETE = Route.delete("/webhooks/{webhook.id}/{webhook.token}/messages/{message.id}");

    ///////////////////////////////////////////
    ////////// Application Resource ///////////
    ///////////////////////////////////////////

    /**
     * Returns the bot's OAuth2 application info.
     *
     * @see <a href=https://discord.com/developers/docs/topics/oauth2#get-current-application-information>
     * https://discord.com/developers/docs/topics/oauth2#get-current-application-information</a>
     */
    public static final Route APPLICATION_INFO_GET = Route.get("/oauth2/applications/@me");

    public static final Route GLOBAL_APPLICATION_COMMANDS_GET = Route.get("/applications/{application.id}/commands");

    public static final Route GLOBAL_APPLICATION_COMMANDS_CREATE = Route.post("/applications/{application.id}/commands");

    public static final Route GLOBAL_APPLICATION_COMMANDS_BULK_OVERWRITE = Route.put("/applications/{application.id}/commands");

    public static final Route GLOBAL_APPLICATION_COMMAND_GET = Route.get("/applications/{application.id}/commands/{command.id}");

    public static final Route GLOBAL_APPLICATION_COMMAND_MODIFY = Route.patch("/applications/{application.id}/commands/{command.id}");

    public static final Route GLOBAL_APPLICATION_COMMAND_DELETE = Route.delete("/applications/{application.id}/commands/{command.id}");

    public static final Route GUILD_APPLICATION_COMMANDS_GET = Route.get("/applications/{application.id}/guilds/{guild.id}/commands");

    public static final Route GUILD_APPLICATION_COMMANDS_CREATE = Route.post("/applications/{application.id}/guilds/{guild.id}/commands");

    public static final Route GUILD_APPLICATION_COMMANDS_BULK_OVERWRITE = Route.put("/applications/{application.id}/guilds/{guild.id}/commands");

    public static final Route GUILD_APPLICATION_COMMAND_GET = Route.get("/applications/{application.id}/guilds/{guild.id}/commands/{command.id}");

    public static final Route GUILD_APPLICATION_COMMAND_MODIFY = Route.patch("/applications/{application.id}/guilds/{guild.id}/commands/{command.id}");

    public static final Route GUILD_APPLICATION_COMMAND_DELETE = Route.delete("/applications/{application.id}/guilds/{guild.id}/commands/{command.id}");

    public static final Route GUILD_APPLICATION_COMMAND_PERMISSIONS_GET = Route.get("/applications/{application.id}/guilds/{guild.id}/commands/permissions");

    public static final Route APPLICATION_COMMAND_PERMISSIONS_GET = Route.get("/applications/{application.id}/guilds/{guild.id}/commands/{command.id}/permissions");

    public static final Route APPLICATION_COMMAND_PERMISSIONS_MODIFY = Route.put("/applications/{application.id}/guilds/{guild.id}/commands/{command.id}/permissions");

    public static final Route APPLICATION_COMMAND_PERMISSIONS_BULK_MODIFY = Route.put("/applications/{application.id}/guilds/{guild.id}/commands/permissions");

    ///////////////////////////////////////////
    ////////// Interaction Resource ///////////
    ///////////////////////////////////////////

    public static final Route INTERACTION_RESPONSE_CREATE = Route.post("/interactions/{interaction.id}/{interaction.token}/callback");
}
