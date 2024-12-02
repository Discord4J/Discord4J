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
package discord4j.core.util;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Permission;

public final class MentionUtil {
    /**
     * The <i>raw</i> mention for notifying all users in a channel.
     *
     * <p>The use of this mention requires the {@link Permission#MENTION_EVERYONE} permission, as well
     * as being permitted by the "allowed mentions" of any messages being sent.</p>
     *
     * @see Permission#MENTION_EVERYONE
     * @see AllowedMentions
     */
    public static final String EVERYONE = "@everyone";

    /**
     * The <i>raw</i> mention for notifying all <i>online</i> users in a channel.
     *
     * <p>The use of this mention requires the {@link Permission#MENTION_EVERYONE} permission, as well
     * as being permitted by the "allowed mentions" of any messages being sent.</p>
     *
     * @see Permission#MENTION_EVERYONE
     * @see AllowedMentions
     */
    public static final String HERE = "@here";

    private MentionUtil() {
    }

    /**
     * Gets a <i>raw</i> mention for a {@link Guild.ResourceNavigation}.
     *
     * <p>This is the format utilized to directly mention a Guild Resource Navigation.</p>
     *
     * @param resourceNavigation The type of the resource navigation to mention.
     * @return The <i>raw</i> mention.
     */
    public static String forGuildResourceNavigation(final Guild.ResourceNavigation resourceNavigation) {
        return "<id:" + resourceNavigation.getValue() + ">";
    }

    /**
     * Gets a <i>raw</i> mention for a {@link Channel}.
     *
     * <p>This is the format utilized to directly mention another channel.</p>
     *
     * @param id The id of the channel to mention.
     * @return The <i>raw</i> mention.
     */
    public static String forChannel(final Snowflake id) {
        return "<#" + id.asString() + ">";
    }

    /**
     * Gets a <i>raw</i> mention for a {@link Role}.
     *
     * <p>The use of this mention requires being permitted by the "allowed mentions" of any messages being sent.</p>
     *
     * <p>This is the format utilized to directly mention another role (assuming the
     * role exists in context of the mention).</p>
     *
     * @param id The id of the role to mention.
     * @return The <i>raw</i> mention.
     */
    public static String forRole(final Snowflake id) {
        return "<@&" + id.asString() + ">";
    }

    /**
     * Gets a <i>raw</i> mention for a {@link User}.
     *
     * <p>The use of this mention requires being permitted by the "allowed mentions" of any messages being sent.</p>
     *
     * <p>This is the format utilized to directly mention another user (assuming the
     * user exists in context of the mention).</p>
     *
     * @param id The id of the user to mention.
     * @return The <i>raw</i> mention.
     */
    public static String forUser(final Snowflake id) {
        return "<@" + id.asString() + ">";
    }
}
