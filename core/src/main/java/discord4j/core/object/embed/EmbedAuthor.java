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
import discord4j.core.object.data.stored.embed.EmbedAuthorBean;

import java.util.Objects;

/** An image for a Discord {@link Embed embed}. */
public final class EmbedAuthor implements DiscordObject {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final EmbedAuthorBean data;

    /**
     * Constructs an {@code EmbedAuthor} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    EmbedAuthor(final ServiceMediator serviceMediator, final EmbedAuthorBean data) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    /**
     * Gets the name of the author.
     *
     * @return The name of the author.
     */
    public String getName() {
        return data.getName();
    }

    /**
     * Gets the URL of the author.
     *
     * @return The URL of the author.
     */
    public String getUrl() {
        return data.getUrl();
    }

    /**
     * Gets the URL of the author icon (only supports http(s) and attachments).
     *
     * @return The URL of the author icon (only supports http(s) and attachments).
     */
    public String getIconUrl() {
        return data.getIconUrl();
    }

    /**
     * Gets a proxied URL of the author icon.
     *
     * @return A proxied URL of the author icon.
     */
    public String getProxyIconUrl() {
        return data.getProxyIconUrl();
    }
}
