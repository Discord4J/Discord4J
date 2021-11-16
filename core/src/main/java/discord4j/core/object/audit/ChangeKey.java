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

import com.fasterxml.jackson.databind.JsonNode;
import discord4j.common.util.Snowflake;
import discord4j.core.object.ExtendedPermissionOverwrite;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.rest.util.Color;
import discord4j.rest.util.PermissionSet;

import java.util.Set;
import java.util.function.BiFunction;

import static discord4j.core.object.audit.AuditLogChangeParser.*;

/**
 * Represents the various audit log change key.
 * See <a href="https://discord.com/developers/docs/resources/audit-log#audit-log-change-object-audit-log-change-key">
 * Audit Log Change Key</a>
 *
 * @param <T> The type of the audit log change key.
 */
public final class ChangeKey<T> {

    /** Name changed */
    public static final ChangeKey<String> NAME = changeKey("name", STRING_PARSER);
    /** Description changed */
    public static final ChangeKey<String> DESCRIPTION = changeKey("description", STRING_PARSER);
    /** Icon changed */
    public static final ChangeKey<String> ICON = changeKey("icon_hash", STRING_PARSER);
    /** Invite splash page artwork changed */
    public static final ChangeKey<String> SPLASH = changeKey("splash_hash", STRING_PARSER);
    /** Discovery splash changed */
    public static final ChangeKey<String> DISCOVERY_SPLASH = changeKey("discovery_splash_hash", STRING_PARSER);
    /** Banner changed */
    public static final ChangeKey<String> BANNER = changeKey("banner_hash", STRING_PARSER);
    /** Owner changed */
    public static final ChangeKey<Snowflake> OWNER = changeKey("owner_id", SNOWFLAKE_PARSER);
    /** Region changed */
    public static final ChangeKey<String> REGION = changeKey("region", STRING_PARSER);
    /** Preferred locale changed */
    public static final ChangeKey<String> PREFERRED_LOCALE = changeKey("preferred_locale", STRING_PARSER);
    /** Afk channel changed */
    public static final ChangeKey<Snowflake> AFK_CHANNEL = changeKey("afk_channel_id", SNOWFLAKE_PARSER);
    /** Afk timeout duration changed */
    public static final ChangeKey<Integer> AFK_TIMEOUT = changeKey("afk_timeout", INTEGER_PARSER);
    /** Id of the rules channel changed */
    public static final ChangeKey<Snowflake> RULES_CHANNEL = changeKey("rules_channel_id", SNOWFLAKE_PARSER);
    /** Od of the public updates channel changed */
    public static final ChangeKey<Snowflake> PUBLIC_UPDATES_CHANNEL = changeKey("public_updates_channel_id", SNOWFLAKE_PARSER);
    /** Two-factor auth requirement changed */
    public static final ChangeKey<Guild.MfaLevel> MFA_LEVEL = changeKey("mfa_level", INTEGER_PARSER.andThen(Guild.MfaLevel::of));
    /** Required verification level changed */
    public static final ChangeKey<Guild.VerificationLevel> VERIFICATION_LEVEL = changeKey("verification_level", INTEGER_PARSER.andThen(Guild.VerificationLevel::of));
    /** Change in whose messages are scanned and deleted for explicit content in the server */
    public static final ChangeKey<Guild.ContentFilterLevel> CONTENT_FILTER_LEVEL = changeKey("explicit_content_filter", INTEGER_PARSER.andThen(Guild.ContentFilterLevel::of));
    /** Default message notification level changed */
    public static final ChangeKey<Guild.NotificationLevel> NOTIFICATION_LEVEL = changeKey("default_message_notifications", INTEGER_PARSER.andThen(Guild.NotificationLevel::of));
    /** Invite vanity url changed */
    public static final ChangeKey<String> VANITY_URL = changeKey("vanity_url_code", STRING_PARSER);
    /** New role added */
    public static final ChangeKey<Set<AuditLogRole>> ROLES_ADD = changeKey("$add", AUDIT_LOG_ROLES_PARSER);
    /** Role removed */
    public static final ChangeKey<Set<AuditLogRole>> ROLES_REMOVE = changeKey("$remove", AUDIT_LOG_ROLES_PARSER);
    /** Change in number of days after which inactive and role-unassigned members are kicked */
    public static final ChangeKey<Integer> PRUNE_DAYS = changeKey("prune_delete_days", INTEGER_PARSER);
    /** Server widget enabled/disable */
    public static final ChangeKey<Boolean> WIDGET_ENABLED = changeKey("widget_enabled", BOOLEAN_PARSER);
    /** Channel id of the server widget changed */
    public static final ChangeKey<Snowflake> WIDGET_CHANNEL = changeKey("widget_channel_id", SNOWFLAKE_PARSER);
    /** Id of the system channel changed */
    public static final ChangeKey<Snowflake> SYSTEM_CHANNEL = changeKey("system_channel_id", SNOWFLAKE_PARSER);
    /** Text or voice channel position changed */
    public static final ChangeKey<Integer> POSITION = changeKey("position", INTEGER_PARSER);
    /** Text channel topic changed */
    public static final ChangeKey<String> TOPIC = changeKey("topic", STRING_PARSER);
    /** Voice channel bitrate changed */
    public static final ChangeKey<Integer> BITRATE = changeKey("bitrate", INTEGER_PARSER);
    /** Permissions on a channel changed */
    public static final ChangeKey<Set<ExtendedPermissionOverwrite>> OVERWRITES = changeKey("permission_overwrites", OVERWRITES_PARSER);
    /** Channel nsfw restriction changed */
    public static final ChangeKey<Boolean> NSFW = changeKey("nsfw", BOOLEAN_PARSER);
    /** Application id of the added or removed webhook or bot */
    public static final ChangeKey<Snowflake> APPLICATION_ID = changeKey("application_id", SNOWFLAKE_PARSER);
    /** Amount of seconds a user has to wait before sending another message changed */
    public static final ChangeKey<Integer> RATE_LIMIT_PER_USER = changeKey("rate_limit_per_user", INTEGER_PARSER);
    /** Permissions for a role changed */
    public static final ChangeKey<PermissionSet> PERMISSIONS = changeKey("permissions", PERMISSION_SET_PARSER);
    /** Role color changed */
    public static final ChangeKey<Color> COLOR = changeKey("color", INTEGER_PARSER.andThen(Color::of));
    /** Role is now displayed/no longer displayed separate from online users */
    public static final ChangeKey<Boolean> HOIST = changeKey("hoist", BOOLEAN_PARSER);
    /** Role is now mentionable/unmentionable */
    public static final ChangeKey<Boolean> MENTIONABLE = changeKey("mentionable", BOOLEAN_PARSER);
    /** A permission on a text or voice channel was allowed for a role */
    public static final ChangeKey<PermissionSet> ALLOW = changeKey("allow", PERMISSION_SET_PARSER);
    /** A permission on a text or voice channel was denied for a role */
    public static final ChangeKey<PermissionSet> DENY = changeKey("deny", PERMISSION_SET_PARSER);
    /** Invite code changed */
    public static final ChangeKey<String> INVITE_CODE = changeKey("code", STRING_PARSER);
    /** Channel for invite code or guild scheduled event changed */
    public static final ChangeKey<Snowflake> INVITE_CHANNEL_ID = changeKey("channel_id", SNOWFLAKE_PARSER);
    /** Person who created invite code changed */
    public static final ChangeKey<Snowflake> INVITER_ID = changeKey("inviter_id", SNOWFLAKE_PARSER);
    /** Change to max number of times invite code can be used */
    public static final ChangeKey<Integer> INVITE_MAX_USES = changeKey("max_uses", INTEGER_PARSER);
    /** Number of times invite code used changed */
    public static final ChangeKey<Integer> INVITE_USES = changeKey("uses", INTEGER_PARSER);
    /** How long invite code lasts changed */
    public static final ChangeKey<Integer> INVITE_MAX_AGE = changeKey("max_age", INTEGER_PARSER);
    /** Invite code is temporary/never expires */
    public static final ChangeKey<Boolean> INVITE_TEMPORARY = changeKey("temporary", BOOLEAN_PARSER);
    /** User server deafened/undeafened */
    public static final ChangeKey<Boolean> USER_DEAFENED = changeKey("deaf", BOOLEAN_PARSER);
    /** User server muted/unmuted */
    public static final ChangeKey<Boolean> USER_MUTED = changeKey("mute", BOOLEAN_PARSER);
    /** User nickname changed */
    public static final ChangeKey<String> USER_NICK = changeKey("nick", STRING_PARSER);
    /** User avatar changed */
    public static final ChangeKey<String> USER_AVATAR = changeKey("avatar_hash", STRING_PARSER);
    /** The id of the changed entity - sometimes used in conjunction with other keys */
    public static final ChangeKey<Snowflake> ID = changeKey("id", SNOWFLAKE_PARSER);
    /** Type of entity created */
    public static final ChangeKey<Channel.Type> CHANNEL_TYPE = changeKey("type",
            INTEGER_PARSER.andThen(Channel.Type::of));
    /** Integration emoticons enabled/disabled */
    public static final ChangeKey<Boolean> ENABLE_EMOTICONS = changeKey("enable_emoticons", BOOLEAN_PARSER);
    /** Integration expiring subscriber behavior changed */
    public static final ChangeKey<Integer> EXPIRE_BEHAVIOR = changeKey("expire_behavior", INTEGER_PARSER);
    /** Integration expire grace period changed */
    public static final ChangeKey<Integer> EXPIRE_GRACE_PERIOD = changeKey("expire_grace_period", INTEGER_PARSER);
    /** New user limit in a voice channel */
    public static final ChangeKey<Integer> USER_LIMIT = changeKey("user_limit", INTEGER_PARSER);
    /** entity type of guild scheduled event changed */
    public static final ChangeKey<Integer> ENTITY_TYPE = changeKey("entity_type", INTEGER_PARSER);
    /** channel ID for guild scheduled event changed */
    public static final ChangeKey<String> LOCATION = changeKey("location", STRING_PARSER);
    /** status of guild scheduled event changed */
    public static final ChangeKey<Integer> STATUS = changeKey("status", INTEGER_PARSER);


    private static <T> ChangeKey<T> changeKey(String name, BiFunction<AuditLogEntry, JsonNode, T> parser) {
        return new ChangeKey<>(name, parser);
    }

    private final String name;

    private final BiFunction<AuditLogEntry, JsonNode, T> parser;

    private ChangeKey(String name, BiFunction<AuditLogEntry, JsonNode, T> parser) {
        this.name = name;
        this.parser = parser;
    }

    public T parseValue(AuditLogEntry entry, JsonNode value) {
        return parser.apply(entry, value);
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
