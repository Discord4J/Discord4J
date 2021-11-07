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
package discord4j.rest.util;

import discord4j.common.annotations.Experimental;

/**
 * Permissions are a way to limit and grant certain abilities to users.
 *
 * @see <a href="https://discord.com/developers/docs/topics/permissions#permissions">Permissions</a>
 */
public enum Permission {

    /** Allows creation of instant invites. */
    CREATE_INSTANT_INVITE(0x00000001, false),

    /** Allows kicking members. */
    KICK_MEMBERS(0x00000002, true),

    /** Allows banning members. */
    BAN_MEMBERS(0x00000004, true),

    /** Allows all permissions and bypasses channel permission overwrites. */
    ADMINISTRATOR(0x00000008, true),

    /** Allows management and editing of channels. */
    MANAGE_CHANNELS(0x00000010, true),

    /** Allows management and editing of the guild. */
    MANAGE_GUILD(0x00000020, true),

    /** Allows for the addition of reactions to messages. */
    ADD_REACTIONS(0x00000040, false),

    /** Allows for viewing of audit logs. */
    VIEW_AUDIT_LOG(0x00000080, false),

    /** Allows for using priority speaker in a voice channel. */
    PRIORITY_SPEAKER(0x00000100, false),

    /** Allows for Stream in voice channel. */
    STREAM(0x00000200, false),

    /** Allows guild members to view a channel, which includes reading messages in text channels. */
    VIEW_CHANNEL(0x00000400, false),

    /** Allows for sending messages in a channel. */
    SEND_MESSAGES(0x00000800, false),

    /** Allows for sending of /tts messages. */
    SEND_TTS_MESSAGES(0x00001000, false),

    /** Allows for deletion of other users messages. */
    MANAGE_MESSAGES(0x00002000, true),

    /** Links sent by users with this permission will be auto-embedded. */
    EMBED_LINKS(0x00004000, false),

    /** Allows for uploading images and files. */
    ATTACH_FILES(0x00008000, false),

    /** Allows for reading of message history. */
    READ_MESSAGE_HISTORY(0x00010000, false),

    /**
     * Allows for using the @everyone tag to notify all users in a channel, and the @here tag to notify all online users
     * in a channel.
     */
    MENTION_EVERYONE(0x00020000, false),

    /** Allows the usage of custom emojis from other servers. */
    USE_EXTERNAL_EMOJIS(0x00040000, false),

    /** Allows for viewing guild insights. */
    VIEW_GUILD_INSIGHTS(0x00080000, false),

    /** Allows for joining of a voice channel. */
    CONNECT(0x00100000, false),

    /** Allows for speaking in a voice channel. */
    SPEAK(0x00200000, false),

    /** Allows for muting members in a voice channel. */
    MUTE_MEMBERS(0x00400000, false),

    /** Allows for deafening of members in a voice channel. */
    DEAFEN_MEMBERS(0x00800000, false),

    /** Allows for moving of members between voice channels. */
    MOVE_MEMBERS(0x01000000, false),

    /** Allows for using voice-activity-detection in a voice channel. */
    USE_VAD(0x02000000, false),

    /** Allows for modification of own nickname. */
    CHANGE_NICKNAME(0x04000000, false),

    /** Allows for modification of other users nicknames. */
    MANAGE_NICKNAMES(0x08000000, false),

    /** Allows management and editing of roles. */
    MANAGE_ROLES(0x10000000, true),

    /** Allows management and editing of webhooks. */
    MANAGE_WEBHOOKS(0x20000000, true),

    /** Allows management and editing of emojis. */
    MANAGE_EMOJIS(0x40000000, true),

    /** Allows members to use slash commands in text channels. */
    USE_SLASH_COMMANDS(0x80000000, false),

    /**
     * Allows for requesting to speak in stage channels.
     */
    REQUEST_TO_SPEAK(0x100000000L, false),

    /** Allows for deleting and archiving threads, and viewing all private threads */
    MANAGE_THREADS(0x0400000000L, true),

    /** Allows for creating and participating in threads */
    USE_PUBLIC_THREADS(0x0800000000L, false),

    /** Allows for creating and participating in private threads */
    USE_PRIVATE_THREADS(0x1000000000L, false);

    /** Whether MFA is required. */
    private final boolean mfa;

    /** The permission value. */
    private final long value;

    /**
     * Constructs a {@code Permission}.
     *
     * @param value The permission value.
     * @param mfa Whether MFA is required.
     */
    Permission(final long value, final boolean mfa) {
        this.value = value;
        this.mfa = mfa;
    }

    /**
     * Gets whether the permission requires the owner account to use multi-factor authentication when used on a guild
     * that has server-side MFA enabled.
     *
     * @return {@code true} when the permission requires the owner account to use multi-factor authentication when used
     * on a guild that has server-side MFA enabled, {@code false} otherwise.
     */
    public boolean requiresMfa() {
        return mfa;
    }

    /**
     * Gets the permission's value.
     *
     * @return The permission's value.
     */
    public long getValue() {
        return value;
    }
}
