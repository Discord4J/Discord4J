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
package discord4j.gateway.json.dispatch;

public abstract class EventNames {

    public static final String READY = "READY";
    public static final String RESUMED = "RESUMED";
    public static final String CHANNEL_CREATE = "CHANNEL_CREATE";
    public static final String CHANNEL_UPDATE = "CHANNEL_UPDATE";
    public static final String CHANNEL_DELETE = "CHANNEL_DELETE";
    public static final String CHANNEL_PINS_UPDATE = "CHANNEL_PINS_UPDATE";
    public static final String GUILD_CREATE = "GUILD_CREATE";
    public static final String GUILD_UPDATE = "GUILD_UPDATE";
    public static final String GUILD_DELETE = "GUILD_DELETE";
    public static final String GUILD_BAN_ADD = "GUILD_BAN_ADD";
    public static final String GUILD_BAN_REMOVE = "GUILD_BAN_REMOVE";
    public static final String GUILD_EMOJIS_UPDATE = "GUILD_EMOJIS_UPDATE";
    public static final String GUILD_INTEGRATIONS_UPDATE = "GUILD_INTEGRATIONS_UPDATE";
    public static final String GUILD_MEMBER_ADD = "GUILD_MEMBER_ADD";
    public static final String GUILD_MEMBER_REMOVE = "GUILD_MEMBER_REMOVE";
    public static final String GUILD_MEMBER_UPDATE = "GUILD_MEMBER_UPDATE";
    public static final String GUILD_MEMBERS_CHUNK = "GUILD_MEMBERS_CHUNK";
    public static final String GUILD_ROLE_CREATE = "GUILD_ROLE_CREATE";
    public static final String GUILD_ROLE_UPDATE = "GUILD_ROLE_UPDATE";
    public static final String GUILD_ROLE_DELETE = "GUILD_ROLE_DELETE";
    public static final String GUILD_SCHEDULED_EVENT_CREATE = "GUILD_SCHEDULED_EVENT_CREATE";
    public static final String GUILD_SCHEDULED_EVENT_UPDATE = "GUILD_SCHEDULED_EVENT_UPDATE";
    public static final String GUILD_SCHEDULED_EVENT_DELETE = "GUILD_SCHEDULED_EVENT_DELETE";
    public static final String GUILD_SCHEDULED_EVENT_USER_ADD = "GUILD_SCHEDULED_EVENT_USER_ADD";
    public static final String GUILD_SCHEDULED_EVENT_USER_REMOVE = "GUILD_SCHEDULED_EVENT_USER_REMOVE";
    public static final String MESSAGE_CREATE = "MESSAGE_CREATE";
    public static final String MESSAGE_UPDATE = "MESSAGE_UPDATE";
    public static final String MESSAGE_DELETE = "MESSAGE_DELETE";
    public static final String MESSAGE_DELETE_BULK = "MESSAGE_DELETE_BULK";
    public static final String MESSAGE_REACTION_ADD = "MESSAGE_REACTION_ADD";
    public static final String MESSAGE_REACTION_REMOVE = "MESSAGE_REACTION_REMOVE";
    public static final String MESSAGE_REACTION_REMOVE_ALL = "MESSAGE_REACTION_REMOVE_ALL";
    public static final String MESSAGE_REACTION_REMOVE_EMOJI = "MESSAGE_REACTION_REMOVE_EMOJI";
    public static final String PRESENCE_UPDATE = "PRESENCE_UPDATE";
    public static final String TYPING_START = "TYPING_START";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String VOICE_STATE_UPDATE = "VOICE_STATE_UPDATE";
    public static final String VOICE_SERVER_UPDATE = "VOICE_SERVER_UPDATE";
    public static final String WEBHOOKS_UPDATE = "WEBHOOKS_UPDATE";
    public static final String INVITE_CREATE = "INVITE_CREATE";
    public static final String INVITE_DELETE = "INVITE_DELETE";
    public static final String APPLICATION_COMMAND_CREATE = "APPLICATION_COMMAND_CREATE";
    public static final String APPLICATION_COMMAND_UPDATE = "APPLICATION_COMMAND_UPDATE";
    public static final String APPLICATION_COMMAND_DELETE = "APPLICATION_COMMAND_DELETE";
    public static final String APPLICATION_COMMAND_PERMISSIONS_UPDATE = "APPLICATION_COMMAND_PERMISSIONS_UPDATE";
    public static final String INTERACTION_CREATE = "INTERACTION_CREATE";
    public static final String THREAD_CREATE = "THREAD_CREATE";
    public static final String THREAD_UPDATE = "THREAD_UPDATE";
    public static final String THREAD_DELETE = "THREAD_DELETE";
    public static final String THREAD_LIST_SYNC = "THREAD_LIST_SYNC";
    public static final String THREAD_MEMBER_UPDATE = "THREAD_MEMBER_UPDATE";
    public static final String THREAD_MEMBERS_UPDATE = "THREAD_MEMBERS_UPDATE";
    public static final String STAGE_INSTANCE_CREATE = "STAGE_INSTANCE_CREATE";
    public static final String STAGE_INSTANCE_UPDATE = "STAGE_INSTANCE_UPDATE";
    public static final String STAGE_INSTANCE_DELETE = "STAGE_INSTANCE_DELETE";
    public static final String GUILD_AUDIT_LOG_ENTRY_CREATE = "GUILD_AUDIT_LOG_ENTRY_CREATE";
    public static final String AUTO_MODERATION_RULE_CREATE = "AUTO_MODERATION_RULE_CREATE";
    public static final String AUTO_MODERATION_RULE_UPDATE = "AUTO_MODERATION_RULE_UPDATE";
    public static final String AUTO_MODERATION_RULE_DELETE = "AUTO_MODERATION_RULE_DELETE";
    public static final String AUTO_MODERATION_ACTION_EXECUTION = "AUTO_MODERATION_ACTION_EXECUTION";
    public static final String MESSAGE_POLL_VOTE_ADD = "MESSAGE_POLL_VOTE_ADD";
    public static final String MESSAGE_POLL_VOTE_REMOVE = "MESSAGE_POLL_VOTE_REMOVE";

    // Ignored
    public static final String PRESENCES_REPLACE = "PRESENCES_REPLACE";
    public static final String GIFT_CODE_UPDATE = "GIFT_CODE_UPDATE";
    public static final String INTEGRATION_CREATE = "INTEGRATION_CREATE";
    public static final String INTEGRATION_UPDATE = "INTEGRATION_UPDATE";
    public static final String INTEGRATION_DELETE = "INTEGRATION_DELETE";
    public static final String GUILD_JOIN_REQUEST_DELETE = "GUILD_JOIN_REQUEST_DELETE";
    public static final String GUILD_JOIN_REQUEST_UPDATE = "GUILD_JOIN_REQUEST_UPDATE";

}
