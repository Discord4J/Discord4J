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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.channel.*;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.ChannelData;
import discord4j.common.util.Snowflake;
import reactor.util.annotation.Nullable;

/** An utility class for entity processing. */
public final class EntityUtil {

    /**
     * The UNIX time that represents Discord's epoch (January 1, 2015).
     *
     * @deprecated Use {@link Snowflake#DISCORD_EPOCH}.
     */
    @Deprecated
    public static final long DISCORD_EPOCH = 1420070400000L;

    public static String getEmojiString(ReactionEmoji emoji) {
        if (emoji instanceof ReactionEmoji.Unicode) {
            return ((ReactionEmoji.Unicode) emoji).getRaw();
        } else {
            ReactionEmoji.Custom custom = ((ReactionEmoji.Custom) emoji);
            return custom.getName() + ":" + custom.getId().asString();
        }
    }

    /**
     * An utility that converts some instance of {@code ChannelBean} to its associated {@code Channel}
     * {@link Channel.Type type}. That is to say, {@code data.getType() == Channel#getType().getValue()}.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The {@code ChannelBean} to convert.
     * @return The converted {@code Channel}.
     */
    public static Channel getChannel(final GatewayDiscordClient gateway, final ChannelData data) {
        switch (Channel.Type.of(data.type())) {
            case GUILD_TEXT: return new TextChannel(gateway, data);
            case DM: return new PrivateChannel(gateway, data);
            case GUILD_VOICE: return new VoiceChannel(gateway, data);
            case GUILD_CATEGORY: return new Category(gateway, data);
            case GUILD_NEWS: return new NewsChannel(gateway, data);
            case GUILD_STORE: return new StoreChannel(gateway, data);
            default: return throwUnsupportedDiscordValue(data);
        }
    }

    /**
     * Throws an {@link UnsupportedOperationException} for an unknown Discord value. This method is intended to be used
     * in enum value constructs such as {@link Channel.Type#of(int)} when the value has not been properly supported.
     *
     * @param value The unknown Discord value.
     * @param <T> The return type. Used to simulate bottom type.
     * @return Diverging function, never returns.
     */
    public static <T> T throwUnsupportedDiscordValue(final Object value) {
        throw new UnsupportedOperationException("Unknown Value: " + value);
    }

    /**
     * An utility that checks for equality between an entity and a generic object.
     *
     * @param entity The entity to compare to.
     * @param obj The object to compare to.
     * @return {@code true} if the two objects are equal, {@code false} otherwise.
     */
    public static boolean equals(final Entity entity, @Nullable final Object obj) {
        return entity.getClass().isInstance(obj) && ((Entity) obj).getId().equals(entity.getId());
    }

    /**
     * An utility that gets the hash code of an entity.
     *
     * @param entity The entity to get a hash code from.
     * @return The hash code of the entity.
     */
    public static int hashCode(final Entity entity) {
        return entity.getId().hashCode();
    }

    private EntityUtil() {}
}
