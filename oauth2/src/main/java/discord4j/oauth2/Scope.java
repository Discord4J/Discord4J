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

package discord4j.oauth2;

import discord4j.oauth2.object.AccessToken;

/**
 * A permission granted to an {@link AccessToken} which grants access to an associated user-specific resource.
 *
 * @see <a href="https://discord.com/developers/docs/topics/oauth2#shared-resources-oauth2-scopes">OAuth2 Scopes</a>
 */
public enum Scope {

    /** Allows retrieving data from list of user's game activities. **/
    ACTIVITIES_READ("activities.read", true),

    /** Allows updating user's activity. **/
    ACTIVITIES_WRITE("activities.write", true),

    /** Allows reading build data for user's applications. **/
    APPLICATIONS_BUILDS_READ("applications.builds.read", false),

    /** Allows uploading builds for user's applications. **/
    APPLICATIONS_BUILDS_UPLOAD("applications.builds.upload", true),

    /** Allows using slash commands in user's selected guild. **/
    APPLICATIONS_COMMANDS("applications.commands", false),

    /** Allows updating slash commands using an {@link AccessToken} exchanged via a client credentials grant. **/
    APPLICATIONS_COMMANDS_UPDATE("applications.commands.update", false),

    /** Allows your app to update permissions for its commands in a guild a user has permissions to. **/
    APPLICATIONS_COMMANDS_PERMISSIONS_UPDATE("applications.commands.permissions.update", false),

    /** Allows reading entitlements for user's applications. **/
    APPLICATIONS_ENTITLEMENTS("applications.entitlements", false),

    /** Allows reading and updating store data for user's applications. **/
    APPLICATIONS_STORE_UPDATE("applications.store.update", false),

    /** Joins a bot to user's selected guild. **/
    BOT("bot", false),

    /** Allows retrieving user's linked third-party accounts. **/
    CONNECTIONS("connections", false),

    /** Allows your app to see information about the user's DMs and group DMs. **/
    DM_CHANNELS_READ("dm_channels.read", true),

    /** Allows retrieving user's email and email verification status. **/
    EMAIL("email", false),

    /** Allows joining user to a group DM. **/
    GDM_JOIN("gdm.join", false),

    /** Allows retrieving partial data from list of user's guilds. **/
    GUILDS("guilds", false),

    /** Allows joining user to a guild. **/
    GUILDS_JOIN("guilds.join", false),

    /** Allows returning user's member information in a guild. **/
    GUILDS_MEMBERS_READ("guilds.members.read", false),

    /**
     * Allows retrieving user's <a href="https://discord.com/developers/docs/resources/user#user-object">data</a>
     * excluding {@code email} and {@code verified}.
     **/
    IDENTIFY("identify", false),

    /**
     * Allows reading all messages from user's local Discord client via RPC. Also allows reading messages from user's
     * channels that have been created by the Discord application associated with the {@link AccessToken} of this
     * {@code Scope}.
     **/
    MESSAGES_READ("messages.read", false),

    /** Allows retrieving user's friends and implicit relationships. **/
    RELATIONSHIPS_READ("relationships.read", true),

    /** Allows your app to update a user's connection and metadata for the app. **/
    ROLE_CONNECTIONS_WRITE("role_connections.write", false),

    /** Allows control of user's local Discord client via RPC. **/
    RPC("rpc", true),

    /** for local rpc server access, this allows you to control a user's local Discord client. **/
    RPC_ACTIVITIES_WRITE("rpc.activities.write", true),

    /** Allows receiving notifications from user's local Discord client via RPC. **/
    RPC_NOTIFICATIONS_READ("rpc.notifications.read", true),

    /** For local rpc server access, this allows you to read a user's voice settings and listen for voice events. **/
    RPC_VOICE_READ("rpc.voice.read", true),

    /** For local rpc server access, this allows you to update a user's voice settings. **/
    RPC_VOICE_WRITE("rpc.voice.write", true),

    /** Allows your app to connect to voice on user's behalf and see all the voice members. **/
    VOICE("voice", true),

    /**
     * Generates a webhook which can be used to execute the webhook in user's selected channel.
     *
     * @see <a href="Webhooks">https://discord.com/developers/docs/topics/oauth2#webhooks</a>
     **/
    WEBHOOK_INCOMING("webhook.incoming", false);

    private final String value;
    private final boolean requiresDiscordApproval;

    Scope(final String value, final boolean requiresDiscordApproval) {
        this.value = value;
        this.requiresDiscordApproval = requiresDiscordApproval;
    }

    public String getValue() {
        return value;
    }

    /**
     * Gets whether this {@code Scope} requires approval from Discord to use.
     *
     * @return Whether this {@code Scope} requires approval from Discord to use.
     */
    public boolean requiresDiscordApproval() {
        return requiresDiscordApproval;
    }

    /**
     * Constructs a {@code Scope} from the given value.
     *
     * @param value The value to construct a {@code Scope} from.
     * @return The {@code Scope} from the given value.
     */
    public static Scope of(final String value) {
        switch (value) {
            case "activities.read": return ACTIVITIES_READ;
            case "activities.write": return ACTIVITIES_WRITE;
            case "applications.builds.read": return APPLICATIONS_BUILDS_READ;
            case "applications.builds.upload": return APPLICATIONS_BUILDS_UPLOAD;
            case "applications.commands": return APPLICATIONS_COMMANDS;
            case "applications.commands.update": return APPLICATIONS_COMMANDS_UPDATE;
            case "applications.commands.permissions.update": return APPLICATIONS_COMMANDS_PERMISSIONS_UPDATE;
            case "applications.entitlements": return APPLICATIONS_ENTITLEMENTS;
            case "applications.store.update": return APPLICATIONS_STORE_UPDATE;
            case "bot": return BOT;
            case "connections": return CONNECTIONS;
            case "dm_channels.read": return DM_CHANNELS_READ;
            case "email": return EMAIL;
            case "gdm.join": return GDM_JOIN;
            case "guilds": return GUILDS;
            case "guilds.join": return GUILDS_JOIN;
            case "guilds.members.read": return GUILDS_MEMBERS_READ;
            case "identify": return IDENTIFY;
            case "messages.read": return MESSAGES_READ;
            case "relationships.read": return RELATIONSHIPS_READ;
            case "role_connections.write": return ROLE_CONNECTIONS_WRITE;
            case "rpc": return RPC;
            case "rpc.activities.write": return RPC_ACTIVITIES_WRITE;
            case "rpc.notifications.read": return RPC_NOTIFICATIONS_READ;
            case "rpc.voice.read": return RPC_VOICE_READ;
            case "rpc.voice.write": return RPC_VOICE_WRITE;
            case "voice": return VOICE;
            case "webhook.incoming": return WEBHOOK_INCOMING;
            default: throw new IllegalArgumentException();
        }
    }
}
