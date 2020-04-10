package discord4j.gateway.intent;

public enum Intent {

    /**
     * Events which will be received by subscribing to GUILDS
     * - GUILD_CREATE
     * - GUILD_DELETE
     * - GUILD_ROLE_CREATE
     * - GUILD_ROLE_UPDATE
     * - GUILD_ROLE_DELETE
     * - CHANNEL_CREATE
     * - CHANNEL_UPDATE
     * - CHANNEL_DELETE
     * - CHANNEL_PINS_UPDATE
     */
    GUILDS(0),

    /**
     * Events which will be received by subscribing to GUILD_MEMBERS
     * - GUILD_MEMBER_ADD
     * - GUILD_MEMBER_UPDATE
     * - GUILD_MEMBER_REMOVE
     */
    GUILD_MEMBERS(1),

    /**
     * Events which will be received by subscribing to GUILD_BANS
     * - GUILD_BAN_ADD
     * - GUILD_BAN_REMOVE
     */
    GUILD_BANS(2),

    /**
     * Events which will be received by subscribing to GUILD_EMOJIS
     * - GUILD_EMOJIS_UPDATE
     */
    GUILD_EMOJIS(3),

    /**
     * Events which will be received by subscribing to GUILD_INTEGRATIONS
     * - GUILD_INTEGRATIONS_UPDATE
     */
    GUILD_INTEGRATIONS(4),

    /**
     * Events which will be received by subscribing to GUILD_WEBHOOKS
     * - WEBHOOKS_UPDATE
     */
    GUILD_WEBHOOKS(5),

    /**
     * Events which will be received by subscribing to GUILD_INVITES
     * - INVITE_CREATE
     * - INVITE_DELETE
     */
    GUILD_INVITES(6),

    /**
     * Events which will be received by subscribing to GUILD_VOICE_STATES
     * - VOICE_STATE_UPDATE
     */
    GUILD_VOICE_STATES(7),

    /**
     * Events which will be received by subscribing to GUILD_PRESENCES
     * - PRESENCE_UPDATE
     */
    GUILD_PRESENCES(8),

    /**
     * Events which will be received by subscribing to GUILD_MESSAGES
     * - MESSAGE_CREATE
     * - MESSAGE_UPDATE
     * - MESSAGE_DELETE
     */
    GUILD_MESSAGES(9),

    /**
     * Events which will be received by subscribing to GUILD_MESSAGE_REACTIONS
     * - MESSAGE_REACTION_ADD
     * - MESSAGE_REACTION_REMOVE
     * - MESSAGE_REACTION_REMOVE_ALL
     * - MESSAGE_REACTION_REMOVE_EMOJI
     */
    GUILD_MESSAGE_REACTIONS(10),

    /**
     * Events which will be received by subscribing to GUILD_MESSAGE_TYPING
     * - TYPING_START
     */
    GUILD_MESSAGE_TYPING(11),

    /**
     * Events which will be received by subscribing to DIRECT_MESSAGES
     * - CHANNEL_CREATE
     * - MESSAGE_CREATE
     * - MESSAGE_UPDATE
     * - MESSAGE_DELETE
     */
    DIRECT_MESSAGES(12),

    /**
     * Events which will be received by subscribing to DIRECT_MESSAGE_REACTIONS
     * - MESSAGE_REACTION_ADD
     * - MESSAGE_REACTION_REMOVE
     * - MESSAGE_REACTION_REMOVE_ALL
     * - MESSAGE_REACTION_REMOVE_EMOJI
     */
    DIRECT_MESSAGE_REACTIONS(13),

    /**
     * Events which will be received by subscribing to DIRECT_MESSAGE_TYPING
     * - TYPING_START
     */
    DIRECT_MESSAGE_TYPING(14);

    private final int value;

    Intent(final int shiftCount) {
        this.value = 1 << shiftCount;
    }

    public int getValue() {
        return value;
    }
}
