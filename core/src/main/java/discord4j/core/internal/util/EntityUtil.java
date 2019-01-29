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
package discord4j.core.internal.util;

import discord4j.core.internal.ServiceMediator;
import discord4j.core.internal.data.PrivateChannelBean;
import discord4j.core.internal.data.stored.CategoryBean;
import discord4j.core.internal.data.stored.ChannelBean;
import discord4j.core.internal.data.stored.TextChannelBean;
import discord4j.core.internal.data.stored.VoiceChannelBean;
import discord4j.core.object.entity.*;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.rest.json.response.ChannelResponse;

import javax.annotation.Nullable;

/** An utility class for entity processing. */
public final class EntityUtil {

    /** The UNIX time that represents Discord's epoch (January 1, 2015). */
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
     * An utility that converts some instance of {@code ChannelResponse} to its associated {@code ChannelBean}
     * {@link Channel.Type type}. That is to say, {@code response.getType() == ChannelBean#getType()}.
     *
     * @param response The {@code ChannelResponse} to convert.
     * @return The converted {@code ChannelBean}.
     */
    public static ChannelBean getChannelBean(final ChannelResponse response) {
        switch (Channel.Type.of(response.getType())) {
            case GUILD_TEXT: return new TextChannelBean(response);
            case DM: return new PrivateChannelBean(response);
            case GUILD_VOICE: return new VoiceChannelBean(response);
            case GUILD_CATEGORY: return new CategoryBean(response);
            default: return throwUnsupportedDiscordValue(response);
        }
    }

    /**
     * An utility that converts some instance of {@code ChannelBean} to its associated {@code Channel}
     * {@link Channel.Type type}. That is to say, {@code bean.getType() == Channel#getType().getValue()}.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param bean The {@code ChannelBean} to convert.
     * @return The converted {@code Channel}.
     */
    public static Channel getChannel(final ServiceMediator serviceMediator, final ChannelBean bean) {
        switch (Channel.Type.of(bean.getType())) {
            case GUILD_TEXT: return new TextChannel(serviceMediator, (TextChannelBean) bean);
            case DM: return new PrivateChannel(serviceMediator, (PrivateChannelBean) bean);
            case GUILD_VOICE: return new VoiceChannel(serviceMediator, (VoiceChannelBean) bean);
            case GUILD_CATEGORY: return new Category(serviceMediator, (CategoryBean) bean);
            default: return throwUnsupportedDiscordValue(bean);
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
