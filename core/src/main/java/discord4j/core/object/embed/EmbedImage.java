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
import discord4j.core.object.data.stored.embed.EmbedImageBean;

import java.util.Objects;

/** An image for a Discord {@link Embed embed}. */
public final class EmbedImage implements DiscordObject {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final EmbedImageBean data;

    /**
     * Constructs an {@code EmbedImage} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    EmbedImage(final ServiceMediator serviceMediator, final EmbedImageBean data) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    /**
     * Gets the source URL of the image (only supports http(s) and attachments).
     *
     * @return The source URL of the image (only supports http(s) and attachments).
     */
    public String getUrl() {
        return data.getUrl();
    }

    /**
     * Gets a proxied URL of the image.
     *
     * @return A proxied URL of the image.
     */
    public String getProxyUrl() {
        return data.getProxyUrl();
    }

    /**
     * Gets the height of the image.
     *
     * @return The height of the image.
     */
    public int getHeight() {
        return data.getHeight();
    }

    /**
     * Gets the width of the image.
     *
     * @return The width of the image.
     */
    public int getWidth() {
        return data.getWidth();
    }
}
