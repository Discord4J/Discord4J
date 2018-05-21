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
package discord4j.core.object.embed;

import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.DiscordObject;
import discord4j.core.object.data.stored.embed.EmbedFieldBean;

import java.util.Objects;

/** A field for a Discord {@link Embed embed}. */
public final class EmbedField implements DiscordObject {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final EmbedFieldBean data;

    /**
     * Constructs an {@code EmbedField} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    EmbedField(final ServiceMediator serviceMediator, final EmbedFieldBean data) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    /**
     * Gets the name of the field.
     *
     * @return The name of the field.
     */
    public String getName() {
        return data.getName();
    }

    /**
     * Gets the value of the field.
     *
     * @return The value of the field.
     */
    public String getValue() {
        return data.getValue();
    }

    /**
     * Gets whether or not this field should display inline.
     *
     * @return {@code true} if this field should display inline, {@code false} otherwise.
     */
    public boolean isInline() {
        return data.isInline();
    }
}
