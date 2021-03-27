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
package discord4j.core.object.audit;

import discord4j.common.util.Snowflake;
import discord4j.core.object.ExtendedPermissionOverwrite;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.Channel;
import discord4j.rest.util.Color;
import discord4j.rest.util.PermissionSet;

import java.util.Set;

/**
 * Represents the various audit log change key.
 * See <a href="https://discord.com/developers/docs/resources/audit-log#audit-log-change-object-audit-log-change-key">
 * Audit Log Change Key</a>
 *
 * @param <T> The type of the audit log change key.
 */
public final class ChangeKey<T> {

    /** Name changed */
    public static final ChangeKey<String> NAME = changeKey("name");
    /** Description changed */
    public static final ChangeKey<String> DESCRIPTION = changeKey("description");
    /** Icon changed */
    public static final ChangeKey<String> ICON = changeKey("icon_hash");
    /** Invite splash page artwork changed */
    public static final ChangeKey<String> SPLASH = changeKey("splash_hash");
    /** Discovery splash changed */
    public static final ChangeKey<String> DISCOVERY_SPLASH = changeKey("discovery_splash_hash");
    /** Banner changed */
    public static final ChangeKey<String> BANNER = changeKey("banner_hash");
    /** Owner changed */
    public static final ChangeKey<Snowflake> OWNER = changeKey("owner_id");
    /** Region changed */
    public static final ChangeKey<String> REGION = changeKey("region");
    /** Preferred locale changed */
    public static final ChangeKey<String> PREFERRED_LOCALE = changeKey("preferred_locale");
    /** Afk channel changed */
    public static final ChangeKey<Snowflake> AFK_CHANNEL = changeKey("afk_channel_id");
    /** Afk timeout duration changed */
    public static final ChangeKey<Integer> AFK_TIMEOUT = changeKey("afk_timeout");
    /** Id of the rules channel changed */
    public static final ChangeKey<Snowflake> RULES_CHANNEL = changeKey("rules_channel_id");
    /** Od of the public updates channel changed */
    public static final ChangeKey<Snowflake> PUBLIC_UPDATES_CHANNEL = changeKey("public_updates_channel_id");
    /** Two-factor auth requirement changed */
    public static final ChangeKey<Guild.MfaLevel> MFA_LEVEL = changeKey("mfa_level");
    /** Required verification level changed */
    public static final ChangeKey<Guild.VerificationLevel> VERIFICATION_LEVEL = changeKey("verification_level");
    /** Change in whose messages are scanned and deleted for explicit content in the server */
    public static final ChangeKey<Guild.ContentFilterLevel> CONTENT_FILTER_LEVEL = changeKey("explicit_content_filter");
    /** Default message notification level changed */
    public static final ChangeKey<Guild.NotificationLevel> NOTIFICATION_LEVEL =
            changeKey("default_message_notifications");
    /** Invite vanity url changed */
    public static final ChangeKey<String> VANITY_URL = changeKey("vanity_url_code");
    /** New role added */
    public static final ChangeKey<Set<Role>> ROLES_ADD = changeKey("$add");
    /** Role removed */
    public static final ChangeKey<Set<Role>> ROLES_REMOVE = changeKey("$remove");
    /** Change in number of days after which inactive and role-unassigned members are kicked */
    public static final ChangeKey<Integer> PRUNE_DAYS = changeKey("prune_delete_days");
    /** Server widget enabled/disable */
    public static final ChangeKey<Boolean> WIDGET_ENABLED = changeKey("widget_enabled");
    /** Channel id of the server widget changed */
    public static final ChangeKey<Snowflake> WIDGET_CHANNEL = changeKey("widget_channel_id");
    /** Id of the system channel changed */
    public static final ChangeKey<Snowflake> SYSTEM_CHANNEL = changeKey("system_channel_id");
    /** Text or voice channel position changed */
    public static final ChangeKey<Integer> POSITION = changeKey("position");
    /** Text channel topic changed */
    public static final ChangeKey<String> TOPIC = changeKey("topic");
    /** Voice channel bitrate changed */
    public static final ChangeKey<Integer> BITRATE = changeKey("bitrate");
    /** Permissions on a channel changed */
    public static final ChangeKey<Set<ExtendedPermissionOverwrite>> OVERWRITES = changeKey("permission_overwrites");
    /** Channel nsfw restriction changed */
    public static final ChangeKey<Boolean> NSFW = changeKey("nsfw");
    /** Application id of the added or removed webhook or bot */
    public static final ChangeKey<Snowflake> APPLICATION_ID = changeKey("application_id");
    /** Amount of seconds a user has to wait before sending another message changed */
    public static final ChangeKey<Integer> RATE_LIMIT_PER_USER = changeKey("rate_limit_per_user");
    /** Permissions for a role changed */
    public static final ChangeKey<PermissionSet> PERMISSIONS = changeKey("permissions");
    /** Role color changed */
    public static final ChangeKey<Color> COLOR = changeKey("color");
    /** Role is now displayed/no longer displayed separate from online users */
    public static final ChangeKey<Boolean> HOIST = changeKey("hoist");
    /** Role is now mentionable/unmentionable */
    public static final ChangeKey<Boolean> MENTIONABLE = changeKey("mentionable");
    /** A permission on a text or voice channel was allowed for a role */
    public static final ChangeKey<PermissionSet> ALLOW = changeKey("allow");
    /** A permission on a text or voice channel was denied for a role */
    public static final ChangeKey<PermissionSet> DENY = changeKey("deny");
    /** Invite code changed */
    public static final ChangeKey<String> INVITE_CODE = changeKey("code");
    /** Channel for invite code changed */
    public static final ChangeKey<Snowflake> INVITE_CHANNEL_ID = changeKey("channel_id");
    /** Person who created invite code changed */
    public static final ChangeKey<Snowflake> INVITER_ID = changeKey("inviter_id");
    /** Change to max number of times invite code can be used */
    public static final ChangeKey<Integer> INVITE_MAX_USES = changeKey("max_uses");
    /** Number of times invite code used changed */
    public static final ChangeKey<Integer> INVITE_USES = changeKey("uses");
    /** How long invite code lasts changed */
    public static final ChangeKey<Integer> INVITE_MAX_AGE = changeKey("max_age");
    /** Invite code is temporary/never expires */
    public static final ChangeKey<Boolean> INVITE_TEMPORARY = changeKey("temporary");
    /** User server deafened/undeafened */
    public static final ChangeKey<Boolean> USER_DEAFENED = changeKey("deaf");
    /** User server muted/unmuted */
    public static final ChangeKey<Boolean> USER_MUTED = changeKey("mute");
    /** User nickname changed */
    public static final ChangeKey<String> USER_NICK = changeKey("nick");
    /** User avatar changed */
    public static final ChangeKey<String> USER_AVATAR = changeKey("avatar_hash");
    /** The id of the changed entity - sometimes used in conjunction with other keys */
    public static final ChangeKey<Snowflake> ID = changeKey("id");
    /** Type of entity created */
    public static final ChangeKey<Channel.Type> CHANNEL_TYPE = changeKey("type");
    /** Integration emoticons enabled/disabled */
    public static final ChangeKey<Boolean> ENABLE_EMOTICONS = changeKey("enable_emoticons");
    /** Integration expiring subscriber behavior changed */
    public static final ChangeKey<Integer> EXPIRE_BEHAVIOR = changeKey("expire_behavior");
    /** Integration expire grace period changed */
    public static final ChangeKey<Integer> EXPIRE_GRACE_PERIOD = changeKey("expire_grace_period");
    /** New user limit in a voice channel */
    public static final ChangeKey<Integer> USER_LIMIT = changeKey("user_limit");

    private static <T> ChangeKey<T> changeKey(String name) {
        return new ChangeKey<>(name);
    }

    private final String name;

    private ChangeKey(String name) {
        this.name = name;
    }

    /**
     * Gets the name of audit log change key.
     *
     * @return The name of audit log change key.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ChangeKey{" +
                "name='" + name + '\'' +
                '}';
    }
}
