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
    public static final String BASE_URL = "https://discord.com/api/v10";

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
    ////////////// AutoMod Resource //////////////
    //////////////////////////////////////////////

    /**
     * Get a list of all rules currently configured for guild. Returns a list of auto moderation rule objects for the given guild. Requires the 'MANAGE_GUILD' permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/auto-moderation#list-auto-moderation-rules-for-guild">
     * https://discord.com/developers/docs/resources/auto-moderation#list-auto-moderation-rules-for-guild</a>
     */
    public static final Route AUTO_MOD_RULES_GET = Route.get("/guilds/{guild.id}/auto-moderation/rules");

    /**
     * Get a single rule. Returns an auto moderation rule object. Requires the 'MANAGE_GUILD' permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/auto-moderation#get-auto-moderation-rule">
     * https://discord.com/developers/docs/resources/auto-moderation#get-auto-moderation-rule</a>
     */
    public static final Route AUTO_MOD_RULE_GET = Route.get("/guilds/{guild.id}/auto-moderation/rules/{auto_moderation_rule.id}");

    /**
     * Create a new rule. Returns an auto moderation rule on success. Requires the 'MANAGE_GUILD' permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/auto-moderation#create-auto-moderation-rule">
     * https://discord.com/developers/docs/resources/auto-moderation#create-auto-moderation-rule</a>
     */
    public static final Route AUTO_MOD_RULE_CREATE = Route.post("/guilds/{guild.id}/auto-moderation/rules");

    /**
     * Modify an existing rule. Returns an auto moderation rule on success. Requires the 'MANAGE_GUILD' permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/auto-moderation#modify-auto-moderation-rule">
     * https://discord.com/developers/docs/resources/auto-moderation#modify-auto-moderation-rule</a>
     */
    public static final Route AUTO_MOD_RULE_MODIFY = Route.patch("/guilds/{guild.id}/auto-moderation/rules/{auto_moderation_rule.id}");

    /**
     * Delete a rule. Returns a 204 on success. Requires the 'MANAGE_GUILD' permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/auto-moderation#delete-auto-moderation-rule">
     * https://discord.com/developers/docs/resources/auto-moderation#delete-auto-moderation-rule</a>
     */
    public static final Route AUTO_MOD_RULE_DELETE = Route.patch("/guilds/{guild.id}/auto-moderation/rules/{auto_moderation_rule.id}");

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

    public static final Route START_THREAD_WITH_MESSAGE = Route.post("/channels/{channel.id}/messages/{message.id}/threads");

    public static final Route START_THREAD_WITHOUT_MESSAGE = Route.post("/channels/{channel.id}/threads");

    public static final Route START_THREAD_IN_FORUM_CHANNEL_MESSAGE = Route.post("/channels/{channel.id}/threads");

    public static final Route JOIN_THREAD = Route.put("/channels/{channel.id}/thread-members/@me");

    public static final Route ADD_THREAD_MEMBER = Route.put("/channels/{channel.id}/thread-members/{user.id}");

    public static final Route LEAVE_THREAD = Route.delete("/channels/{channel.id}/thread-members/@me");

    public static final Route REMOVE_THREAD_MEMBER = Route.delete("/channels/{channel.id}/thread-members/{user.id}");

    public static final Route GET_THREAD_MEMBER = Route.get("/channels/{channel.id}/thread-members/{user.id}");

    public static final Route LIST_THREAD_MEMBERS = Route.get("/channels/{channel.id}/thread-members");

    public static final Route LIST_PUBLIC_ARCHIVED_THREADS = Route.get("/channels/{channel.id}/threads/archived/public");

    public static final Route LIST_PRIVATE_ARCHIVED_THREADS = Route.get("/channels/{channel.id}/threads/archived/private");

    public static final Route LIST_JOINED_PRIVATE_ARCHIVED_THREADS = Route.get("/channels/{channel.id}/users/@me/threads/archived/private");

    ///////////////////////////////////////////
    ////////////////// Polls //////////////////
    ///////////////////////////////////////////

    /**
     * Returns a list of users that voted for the given `answer_id` in the poll for the given message represented by
     * its `message.id` and `channel.id`.
     * @see <a href="https://discord.com/developers/docs/resources/poll#get-answer-voters">https://discord.com/developers/docs/resources/poll#get-answer-voters</a>
     */
    public static final Route POLL_ANSWER_VOTERS_GET = Route.get("/channels/{channel.id}/polls/{message.id}/answers/{answer_id}");

    /**
     * Request to end a poll early. This will end the poll and return the final results.
     *
     * @see <a href="https://discord.com/developers/docs/resources/poll#end-poll">https://discord.com/developers/docs/resources/poll#end-poll</a>
     */
    public static final Route END_POLL = Route.post("/channels/{channel.id}/polls/{message.id}/expire");

    ////////////////////////////////////////////
    ////////////// Sticker Resource //////////////
    ////////////////////////////////////////////

    /**
     * Returns a sticker object for the given sticker ID.
     *
     * @see <a href="https://discord.com/developers/docs/resources/sticker#get-sticker">
     * https://discord.com/developers/docs/resources/sticker#get-sticker</a>
     */
    public static final Route STICKER_GET = Route.get("/stickers/{sticker.id}");

    /**
     * Returns the list of available sticker packs.
     *
     * @see <a href="https://discord.com/developers/docs/resources/sticker#list-sticker-packs">
     * https://discord.com/developers/docs/resources/sticker#list-sticker-packs</a>
     */
    public static final Route STICKER_PACKS_GET = Route.get("/sticker-packs");

    /**
     * Returns an array of sticker objects for the given guild. Includes user fields if the bot has the MANAGE_EMOJIS_AND_STICKERS permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/sticker#list-guild-stickers">
     * https://discord.com/developers/docs/resources/sticker#list-guild-stickers</a>
     */
    public static final Route GUILD_STICKERS_GET = Route.get("/guilds/{guild.id}/stickers");

    /**
     * Returns a sticker object for the given guild and sticker IDs. Includes the user field if the bot has the MANAGE_EMOJIS_AND_STICKERS permission.
     *
     * @see <a href="https://discord.com/developers/docs/resources/sticker#get-guild-sticker">
     * https://discord.com/developers/docs/resources/sticker#get-guild-sticker</a>
     */
    public static final Route GUILD_STICKER_GET = Route.get("/guilds/{guild.id}/stickers/{sticker.id}");

    /**
     * Create a new sticker for the guild. Send a multipart/form-data body. Requires the MANAGE_EMOJIS_AND_STICKERS permission. Returns the new sticker object on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/sticker#create-guild-sticker">
     * https://discord.com/developers/docs/resources/sticker#create-guild-sticker</a>
     */
    public static final Route GUILD_STICKER_CREATE = Route.post("/guilds/{guild.id}/stickers");

    /**
     * Modify the given sticker. Requires the MANAGE_EMOJIS_AND_STICKERS permission. Returns the updated sticker object on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/sticker#modify-guild-sticker">
     * https://discord.com/developers/docs/resources/sticker#modify-guild-sticker</a>
     */
    public static final Route GUILD_STICKER_MODIFY = Route.patch("/guilds/{guild.id}/stickers/{sticker.id}");

    /**
     * Delete the given sticker. Requires the MANAGE_EMOJIS_AND_STICKERS permission. Returns 204 No Content on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/sticker#delete-guild-sticker">
     * https://discord.com/developers/docs/resources/sticker#delete-guild-sticker</a>
     */
    public static final Route GUILD_STICKER_DELETE = Route.delete("/guilds/{guild.id}/stickers/{sticker.id}");

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
     * Modifies the current member in a guild. Returns a 200 with the updated member on success. Fires a Guild
     * Member Update Gateway event.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#modify-current-member">Discord</a>
     */
    public static final Route CURRENT_MEMBER_MODIFY = Route.patch("/guilds/{guild.id}/members/@me");

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
     * Ban up to 200 users from a guild, and optionally delete previous messages sent by the banned users.
     * Requires both the `BAN_MEMBERS` and `MANAGE_GUILD` permissions.
     * Returns a 200 response on success, including the fields banned_users with the IDs of the banned users and failed_users with IDs that could not be banned or were already banned.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#bulk-guild-ban">
     * https://discord.com/developers/docs/resources/guild#bulk-guild-ban</a>
     */
    public static final Route GUILD_BAN_BULK = Route.post("/guilds/{guild.id}/bulk-ban");

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

    public static final Route LIST_ACTIVE_GUILD_THREADS = Route.get("/guilds/{guild.id}/threads/active");

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
     * Returns a guild member object for the current user. Requires the guilds.members.read OAuth2 scope.
     *
     * @see <a href="https://discord.com/developers/docs/resources/user#get-current-user-guild-member">Discord</a>
     */
    public static final Route CURRENT_USER_GUILD_MEMBER_GET = Route.get("/users/@me/guilds/{guild.id}/member");

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

    /**
     * Returns the application role connection for the user. Requires an OAuth2 access token with role_connections.write scope for the application specified in the path.
     *
     * @see <a href="https://discord.com/developers/docs/resources/user#get-current-user-application-role-connection">
     * https://discord.com/developers/docs/resources/user#get-current-user-application-role-connection</a>
     */
    public static final Route USER_APPLICATIONS_ROLE_CONNECTION_GET = Route.get("/users/@me/applications/{application.id}/role-connection");

    /**
     * Updates and returns the application role connection for the user. Requires an OAuth2 access token with role_connections.write scope for the application specified in the path.
     *
     * @see <a href="https://discord.com/developers/docs/resources/user#update-current-user-application-role-connection">
     * https://discord.com/developers/docs/resources/user#update-current-user-application-role-connection</a>
     */
    public static final Route USER_APPLICATIONS_ROLE_CONNECTION_MODIFY = Route.put("/users/@me/applications/{application.id}/role-connection");

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
     * @see <a href="https://discord.com/developers/docs/resources/webhook#get-webhook-message">
     * https://discord.com/developers/docs/resources/webhook#get-webhook-message</a>
     */
    public static final Route WEBHOOK_MESSAGE_GET = Route.get("/webhooks/{webhook.id}/{webhook.token}/messages/{message.id}");

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

    public static final Route APPLICATION_INFO_MODIFY = Route.patch("/applications/@me");

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

    ///////////////////////////////////////////////////
    // Application Emoji Resource //
    ///////////////////////////////////////////////////

    /**
     * Returns an object containing a list of emoji objects for the given application under the items key.
     * <br>
     * Includes a user object for the team member that uploaded the emoji from the app's settings, or for the bot user if uploaded using the API.
     *
     * @see <a href="https://discord.com/developers/docs/resources/emoji#list-application-emojis">https://discord.com/developers/docs/resources/emoji#list-application-emojis</a>
     */
    public static final Route APPLICATION_EMOJIS_GET = Route.get("/applications/{application.id}/emojis");

    /**
     * Returns an emoji object for the given application and emoji IDs. Includes the user field.
     *
     * @see <a href="https://discord.com/developers/docs/resources/emoji#get-application-emoji">https://discord.com/developers/docs/resources/emoji#get-application-emoji</a>
     */
    public static final Route APPLICATION_EMOJI_GET = Route.get("/applications/{application.id}/emojis/{emoji.id}");

    /**
     * Create a new emoji for the application. Returns the new emoji object on success.
     * <br>
     * Emojis and animated emojis have a maximum file size of 256 KiB.
     * Attempting to upload an emoji larger than this limit will fail and return 400 Bad Request and an error message, but not a JSON status code.
     *
     * @see <a href="https://discord.com/developers/docs/resources/emoji#create-application-emoji">https://discord.com/developers/docs/resources/emoji#create-application-emoji</a>
     */
    public static final Route APPLICATION_EMOJI_CREATE = Route.post("/applications/{application.id}/emojis");

    /**
     * Modify the given emoji. Returns the updated emoji object on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/emoji#modify-application-emoji">https://discord.com/developers/docs/resources/emoji#modify-application-emoji</a>
     */
    public static final Route APPLICATION_EMOJI_MODIFY = Route.patch("/applications/{application.id}/emojis/{emoji.id}");

    /**
     * Delete the given emoji. Returns 204 No Content on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/emoji#delete-application-emoji">https://discord.com/developers/docs/resources/emoji#delete-application-emoji</a>
     */
    public static final Route APPLICATION_EMOJI_DELETE = Route.delete("/applications/{application.id}/emojis/{emoji.id}");

    ///////////////////////////////////////////////////
    // Application Role Connection Metadata Resource //
    ///////////////////////////////////////////////////

    /**
     * Returns a list of application role connection metadata objects for the given application.
     *
     * @see <a href="https://discord.com/developers/docs/resources/application-role-connection-metadata#get-application-role-connection-metadata-records">https://discord.com/developers/docs/resources/application-role-connection-metadata#get-application-role-connection-metadata-records</a>
     */
    public static final Route APPLICATION_ROLE_CONNECTION_METADATA_GET = Route.get("/applications/{application.id}/role-connections/metadata");

    /**
     * Updates and returns a list of application role connection metadata objects for the given application.
     * An application can have a maximum of 5 metadata records.
     *
     * @see <a href="https://discord.com/developers/docs/resources/application-role-connection-metadata#modify-application-role-connection-metadata">https://discord.com/developers/docs/resources/application-role-connection-metadata#modify-application-role-connection-metadata</a>
     */
    public static final Route APPLICATION_ROLE_CONNECTION_METADATA_MODIFY = Route.put("/applications/{application.id}/role-connections/metadata");

    ///////////////////////////////////////////
    ////////// Interaction Resource ///////////
    ///////////////////////////////////////////

    public static final Route INTERACTION_RESPONSE_CREATE = Route.post("/interactions/{interaction.id}/{interaction.token}/callback");

    ///////////////////////////////////////////
    //////// Stage Instance Resource /////////
    ///////////////////////////////////////////

    public static final Route CREATE_STAGE_INSTANCE = Route.post("/stage-instances");

    public static final Route GET_STAGE_INSTANCE = Route.get("/stage-instances/{channel.id}");

    public static final Route MODIFY_STAGE_INSTANCE = Route.patch("/stage-instances/{channel.id}");

    public static final Route DELETE_STAGE_INSTANCE = Route.delete("/stage-instances/{channel.id}");

    /////////////////////////////////////////////////////
    ////////// Guild Scheduled Event Resource ///////////
    /////////////////////////////////////////////////////

    /**
     * Returns a list of all scheduled events for a guild.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild-scheduled-event#list-scheduled-events-for-guild">
     * https://discord.com/developers/docs/resources/guild-scheduled-event#list-scheduled-events-for-guild</a>
     */
    public static final Route GUILD_SCHEDULED_EVENTS_GET = Route.get("/guilds/{guild.id}/scheduled-events");

    /**
     * Creates a guild scheduled event for the given guild. Returns a scheduled event object on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild-scheduled-event#create-guild-scheduled-event">
     * ttps://discord.com/developers/docs/resources/guild-scheduled-event#create-guild-scheduled-event</a>
     */
    public static final Route GUILD_SCHEDULED_EVENT_CREATE = Route.post("/guilds/{guild.id}/scheduled-events");

    /**
     * Returns a scheduled event for the given guild.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild-scheduled-event#get-guild-scheduled-event">
     * https://discord.com/developers/docs/resources/guild-scheduled-event#get-guild-scheduled-event</a>
     */
    public static final Route GUILD_SCHEDULED_EVENT_GET = Route.get("/guilds/{guild.id}/scheduled-events/{event.id}");

    /**
     * Modifies a scheduled event for the given guild. Returns the modified scheduled event object on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild-scheduled-event#modify-guild-scheduled-event">
     * https://discord.com/developers/docs/resources/guild-scheduled-event#modify-guild-scheduled-event</a>
     */
    public static final Route GUILD_SCHEDULED_EVENT_MODIFY = Route.patch("/guilds/{guild.id}/scheduled-events/{event.id}");

    /**
     * Deletes a scheduled event for the given guild. Returns a 204 empty response on success.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild-scheduled-event#delete-guild-scheduled-event">
     * https://discord.com/developers/docs/resources/guild-scheduled-event#delete-guild-scheduled-event</a>
     */
    public static final Route GUILD_SCHEDULED_EVENT_DELETE = Route.delete("/guilds/{guild.id}/scheduled-events/{event.id}");

    /**
     * Returns a list of users RSVP'd to the scheduled event for the given guild. Returns a list of user objects on
     * success with an optional `guild_member` property for each user if `with_member` query param is passed.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild-scheduled-event#get-guild-scheduled-event-users">
     * https://discord.com/developers/docs/resources/guild-scheduled-event#get-guild-scheduled-event-users</a>
     */
    public static final Route GUILD_SCHEDULED_EVENT_USERS_GET = Route.get("/guilds/{guild.id}/scheduled-events/{event.id}/users");

    ///////////////////////////////////////////
    ////// Onboarding and welcome screen //////
    ///////////////////////////////////////////

    /**
     * Returns the Onboarding object for the guild.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#get-guild-onboarding">https://discord.com/developers/docs/resources/guild#get-guild-onboarding</a>
     */
    public static final Route GUILD_ONBOARDING_GET = Route.get("/guilds/{guild.id}/onboarding");

    /**
     * Modifies the onboarding configuration of the guild. Returns a 200 with the Onboarding object for the guild. Requires the MANAGE_GUILD and MANAGE_ROLES permissions.
     * This endpoint supports the X-Audit-Log-Reason header.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild#modify-guild-onboarding">https://discord.com/developers/docs/resources/guild#modify-guild-onboarding</a>
     */
    public static final Route GUILD_ONBOARDING_MODIFY = Route.put("/guilds/{guild.id}/onboarding");

    ///////////////////////////////////////////
    ///////////// OAuth2 Resource /////////////
    ///////////////////////////////////////////

    public static final Route TOKEN = Route.post("/oauth2/token");

    public static final Route TOKEN_REVOKE = Route.post("/oauth2/token/revoke");

    public static final Route AUTHORIZATION_INFO_GET = Route.get("/oauth2/@me");


    ////////////////////////////////////////
    ///////////// Monetization /////////////
    ////////////////////////////////////////

    /**
     * Returns a list of SKUs for a given application.
     *
     * @see <a href="https://discord.com/developers/docs/monetization/skus#list-skus">Docs</a>
     */
    public static final Route LIST_SKUS = Route.get("/applications/{application.id}/skus");

    /**
     * Returns a list of entitlements for a given application.
     *
     * @see <a href="https://discord.com/developers/docs/monetization/entitlements#list-entitlements">Docs</a>
     */
    public static final Route LIST_ENTITLEMENTS = Route.get("/applications/{application.id}/entitlements");

    /**
     * Creates a test entitlement for a given application.
     *
     * @see <a href="https://discord.com/developers/docs/monetization/entitlements#create-test-entitlement">Docs</a>
     */
    public static final Route CREATE_TEST_ENTITLEMENT = Route.post("/applications/{application.id}/entitlements");

    /**
     * Deletes a test entitlement for a given application.
     *
     * @see <a href="https://discord.com/developers/docs/monetization/entitlements#delete-test-entitlement">Docs</a>
     */
    public static final Route DELETE_TEST_ENTITLEMENT = Route.delete("/applications/{application.id}/entitlements/{entitlement.id}");

    /**
     * For One-Time Purchase consumable SKUs, marks a given entitlement for the user as consumed. The entitlement will have consumed=true when using {@link Routes#LIST_ENTITLEMENTS}.
     *
     * @see <a href="https://discord.com/developers/docs/monetization/entitlements#consume-an-entitlement">Docs</a>
     */
    public static final Route CONSUME_ENTITLEMENT = Route.post("/applications/{application.id}/entitlements/{entitlement.id}/consume");

}
