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

package discord4j.gateway.intent;

/**
 * A group of Discord Gateway events.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#gateway-intents">Gateway Intents</a>
 */
public enum Intent {

    /**
     * Events which will be received by subscribing to GUILDS
     * <ul>
     *     <li>GUILD_CREATE</li>
     *     <li>GUILD_UPDATE</li>
     *     <li>GUILD_DELETE</li>
     *     <li>GUILD_ROLE_CREATE</li>
     *     <li>GUILD_ROLE_UPDATE</li>
     *     <li>GUILD_ROLE_DELETE</li>
     *     <li>CHANNEL_CREATE</li>
     *     <li>CHANNEL_UPDATE</li>
     *     <li>CHANNEL_DELETE</li>
     *     <li>CHANNEL_PINS_UPDATE</li>
     *     <li>THREAD_CREATE</li>
     *     <li>THREAD_UPDATE</li>
     *     <li>THREAD_DELETE</li>
     *     <li>THREAD_LIST_SYNC</li>
     *     <li>THREAD_MEMBER_UPDATE</li>
     *     <li>THREAD_MEMBERS_UPDATE</li>
     *     <li>STAGE_INSTANCE_CREATE</li>
     *     <li>STAGE_INSTANCE_UPDATE</li>
     *     <li>STAGE_INSTANCE_DELETE</li>
     * </ul>
     */
    GUILDS(0),

    /**
     * Events which will be received by subscribing to GUILD_MEMBERS
     * <ul>
     *     <li>GUILD_MEMBER_ADD</li>
     *     <li>GUILD_MEMBER_UPDATE</li>
     *     <li>GUILD_MEMBER_REMOVE</li>
     *     <li>THREAD_MEMBERS_UPDATE</li>
     * </ul>
     */
    GUILD_MEMBERS(1),

    /**
     * Events which will be received by subscribing to GUILD_MODERATION
     * <ul>
     *     <li>GUILD_AUDIT_LOG_ENTRY_CREATE</li>
     *     <li>GUILD_BAN_ADD</li>
     *     <li>GUILD_BAN_REMOVE</li>
     * </ul>
     */
    GUILD_MODERATION(2),

    /**
     * Events which will be received by subscribing to GUILD_BANS
     * <ul>
     *     <li>GUILD_BAN_ADD</li>
     *     <li>GUILD_BAN_REMOVE</li>
     * </ul>
     * @deprecated deprecated in favor of {@link #GUILD_MODERATION}
     */
    @Deprecated
    GUILD_BANS(2),

    /**
     * Events which will be received by subscribing to GUILD_EMOJIS_AND_STICKERS
     * <ul>
     *     <li>GUILD_EMOJIS_UPDATE</li>
     *     <li>GUILD_STICKERS_UPDATE</li>
     * </ul>
     */
    GUILD_EMOJIS_AND_STICKERS(3),

    /**
     * Events which will be received by subscribing to GUILD_EMOJIS
     * <ul>
     *     <li>GUILD_EMOJIS_UPDATE</li>
     *     <li>GUILD_STICKERS_UPDATE</li>
     * </ul>
     */
    @Deprecated
    GUILD_EMOJIS(3),

    /**
     * Events which will be received by subscribing to GUILD_INTEGRATIONS
     * <ul>
     *     <li>GUILD_INTEGRATIONS_UPDATE</li>
     *     <li>INTEGRATION_CREATE</li>
     *     <li>INTEGRATION_UPDATE</li>
     *     <li>INTEGRATION_DELETE</li>
     * </ul>
     */
    GUILD_INTEGRATIONS(4),

    /**
     * Events which will be received by subscribing to GUILD_WEBHOOKS
     * <ul>
     *     <li>WEBHOOKS_UPDATE</li>
     * </ul>
     */
    GUILD_WEBHOOKS(5),

    /**
     * Events which will be received by subscribing to GUILD_INVITES
     * <ul>
     *     <li>INVITE_CREATE</li>
     *     <li>INVITE_DELETE</li>
     * </ul>
     */
    GUILD_INVITES(6),

    /**
     * Events which will be received by subscribing to GUILD_VOICE_STATES
     * <ul>
     *     <li>VOICE_STATE_UPDATE</li>
     * </ul>
     */
    GUILD_VOICE_STATES(7),

    /**
     * Events which will be received by subscribing to GUILD_PRESENCES
     * <ul>
     *     <li>PRESENCE_UPDATE</li>
     * </ul>
     */
    GUILD_PRESENCES(8),

    /**
     * Events which will be received by subscribing to GUILD_MESSAGES
     * <ul>
     *     <li>MESSAGE_CREATE</li>
     *     <li>MESSAGE_UPDATE</li>
     *     <li>MESSAGE_DELETE</li>
     *     <li>MESSAGE_DELETE_BULK</li>
     * </ul>
     */
    GUILD_MESSAGES(9),

    /**
     * Events which will be received by subscribing to GUILD_MESSAGE_REACTIONS
     * <ul>
     *     <li>MESSAGE_REACTION_ADD</li>
     *     <li>MESSAGE_REACTION_REMOVE</li>
     *     <li>MESSAGE_REACTION_REMOVE_ALL</li>
     *     <li>MESSAGE_REACTION_REMOVE_EMOJI</li>
     * </ul>
     */
    GUILD_MESSAGE_REACTIONS(10),

    /**
     * Events which will be received by subscribing to GUILD_MESSAGE_TYPING
     * <ul>
     *     <li>TYPING_START</li>
     * </ul>
     */
    GUILD_MESSAGE_TYPING(11),

    /**
     * Events which will be received by subscribing to DIRECT_MESSAGES
     * <ul>
     *     <li>MESSAGE_CREATE</li>
     *     <li>MESSAGE_UPDATE</li>
     *     <li>MESSAGE_DELETE</li>
     *     <li>CHANNEL_PINS_UPDATE</li>
     * </ul>
     */
    DIRECT_MESSAGES(12),

    /**
     * Events which will be received by subscribing to DIRECT_MESSAGE_REACTIONS
     * <ul>
     *     <li>MESSAGE_REACTION_ADD</li>
     *     <li>MESSAGE_REACTION_REMOVE</li>
     *     <li>MESSAGE_REACTION_REMOVE_ALL</li>
     *     <li>MESSAGE_REACTION_REMOVE_EMOJI</li>
     * </ul>
     */
    DIRECT_MESSAGE_REACTIONS(13),

    /**
     * Events which will be received by subscribing to DIRECT_MESSAGE_TYPING
     * <ul>
     *     <li>TYPING_START</li>
     * </ul>
     */
    DIRECT_MESSAGE_TYPING(14),

    /**
     * MESSAGE_CONTENT does not represent individual events, but rather affects what data is present for
     * events that could contain message content fields.
     *
     * @see <a href="https://discord.com/developers/docs/topics/gateway#message-content-intent">Message Content Intent</a>
     */
    MESSAGE_CONTENT(15),

    /**
     * Events which will be received by subscribing to GUILD_SCHEDULED_EVENTS
     * <ul>
     *     <li>GUILD_SCHEDULED_EVENT_CREATE</li>
     *     <li>GUILD_SCHEDULED_EVENT_UPDATE</li>
     *     <li>GUILD_SCHEDULED_EVENT_DELETE</li>
     *     <li>GUILD_SCHEDULED_EVENT_USER_ADD</li>
     *     <li>GUILD_SCHEDULED_EVENT_USER_REMOVE</li>
     * </ul>
     */
    GUILD_SCHEDULED_EVENTS(16),

    /**
     * Events which will be received by subscribing to AUTO_MODERATION_CONFIGURATION
     * <ul>
     *     <li>AUTO_MODERATION_RULE_CREATE</li>
     *     <li>AUTO_MODERATION_RULE_UPDATE</li>
     *     <li>AUTO_MODERATION_RULE_DELETE</li>
     * </ul>
     */
    AUTO_MODERATION_CONFIGURATION(20),

    /**
     * Events which will be received by subscribing to AUTO_MODERATION_EXECUTION
     * <ul>
     *     <li>AUTO_MODERATION_ACTION_EXECUTION</li>
     * </ul>
     */
    AUTO_MODERATION_EXECUTION(21),

    /**
     * Events which will be received by subscribing to GUILD_MESSAGE_POLLS
     * <ul>
     *     <li>MESSAGE_POLL_VOTE_ADD</li>
     *     <li>MESSAGE_POLL_VOTE_REMOVE</li>
     * </ul>
     */
    GUILD_MESSAGE_POLLS(24),

    /**
     * Events which will be received by subscribing to DIRECT_MESSAGE_POLLS
     * <ul>
     *     <li>MESSAGE_POLL_VOTE_ADD</li>
     *     <li>MESSAGE_POLL_VOTE_REMOVE</li>
     * </ul>
     */
    DIRECT_MESSAGE_POLLS(25);

    private final int value;

    Intent(final int shiftCount) {
        this.value = 1 << shiftCount;
    }

    public int getValue() {
        return value;
    }
}
