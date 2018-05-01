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

import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;

import java.awt.Color;
import java.util.Set;

public final class ChangeKey<T> {

    public static final ChangeKey<String> NAME = changeKey("name");
    public static final ChangeKey<String> ICON = changeKey("icon_hash");
    public static final ChangeKey<String> SPLASH = changeKey("splash_hash");
    public static final ChangeKey<Snowflake> OWNER = changeKey("owner_id");
    public static final ChangeKey<String> REGION = changeKey("region");
    public static final ChangeKey<Snowflake> AFK_CHANNEL = changeKey("afk_channel_id");
    public static final ChangeKey<Integer> AFK_TIMEOUT = changeKey("afk_timeout");
    public static final ChangeKey<Guild.MfaLevel> MFA_LEVEL = changeKey("mfa_level");
    public static final ChangeKey<Guild.VerificationLevel> VERIFICATION_LEVEL = changeKey("verification_level");
    public static final ChangeKey<Guild.ContentFilterLevel> CONTENT_FILTER_LEVEL = changeKey("explicit_content_filter");
    public static final ChangeKey<Guild.NotificationLevel> NOTIFICATION_LEVEL = changeKey("default_message_notifications");
    public static final ChangeKey<String> VANITY_URL = changeKey("vanity_url_code");
    public static final ChangeKey<Set<Role>> ROLES_ADD = changeKey("$add");
    public static final ChangeKey<Set<Role>> ROLES_REMOVE = changeKey("$remove");
    public static final ChangeKey<Integer> PRUNE_DAYS = changeKey("prune_delete_days");
    public static final ChangeKey<Boolean> WIDGET_ENABLED = changeKey("widget_enabled");
    public static final ChangeKey<Snowflake> WIDGET_CHANNEL = changeKey("widget_channel_id");
    public static final ChangeKey<Integer> POSITION = changeKey("position");
    public static final ChangeKey<String> TOPIC = changeKey("topic");
    public static final ChangeKey<Integer> BITRATE = changeKey("bitrate");
    public static final ChangeKey<Set<PermissionOverwrite>> OVERWRITES = changeKey("permission_overwrites");
    public static final ChangeKey<Boolean> NSFW = changeKey("nsfw");
    public static final ChangeKey<Snowflake> APPLICATION_ID = changeKey("application_id");
    public static final ChangeKey<PermissionSet> PERMISSIONS = changeKey("permission");
    public static final ChangeKey<Color> COLOR = changeKey("color");
    public static final ChangeKey<Boolean> HOIST = changeKey("hoist");
    public static final ChangeKey<Boolean> MENTIONABLE = changeKey("mentionable");
    public static final ChangeKey<PermissionSet> ALLOW = changeKey("allow");
    public static final ChangeKey<PermissionSet> DENY = changeKey("deny");
    public static final ChangeKey<String> INVITE_CODE = changeKey("invite_code");
    public static final ChangeKey<Snowflake> INVITE_CHANNEL_ID = changeKey("channel_id");
    public static final ChangeKey<Snowflake> INVITER_ID = changeKey("inviter_id");
    public static final ChangeKey<Integer> INVITE_MAX_USES = changeKey("max_uses");
    public static final ChangeKey<Integer> INVITE_USES = changeKey("uses");
    public static final ChangeKey<Integer> INVITE_MAX_AGE = changeKey("max_age");
    public static final ChangeKey<Boolean> INVITE_TEMPORARY = changeKey("temporary");
    public static final ChangeKey<Boolean> USER_DEAFENED = changeKey("deaf");
    public static final ChangeKey<Boolean> USER_MUTED = changeKey("mute");
    public static final ChangeKey<String> USER_NICK = changeKey("nick");
    public static final ChangeKey<String> USER_AVATAR = changeKey("avatar_hash");
    public static final ChangeKey<Snowflake> ID = changeKey("id");
    public static final ChangeKey<Channel.Type> CHANNEL_TYPE = changeKey("type");

    private static <T> ChangeKey<T> changeKey(String name) {
        return new ChangeKey<>(name);
    }

    private final String name;

    private ChangeKey(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
