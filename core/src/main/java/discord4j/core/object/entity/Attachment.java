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
package discord4j.core.object.entity;

import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.data.stored.AttachmentBean;
import discord4j.core.object.util.Snowflake;
import discord4j.core.util.EntityUtil;

import java.util.Objects;
import java.util.OptionalInt;

/**
 * A Discord attachment.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#attachment-object">Attachment Object</a>
 */
public final class Attachment implements Entity {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final AttachmentBean data;

    /**
     * Constructs an {@code Attachment} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Attachment(final ServiceMediator serviceMediator, final AttachmentBean data) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.getId());
    }

    /**
     * Gets the name of the file attached.
     *
     * @return The name of the file attached.
     */
    public String getFilename() {
        return data.getFileName();
    }

    /**
     * Gets the size of the file in bytes.
     *
     * @return The size of the file in bytes.
     */
    public int getSize() {
        return data.getSize();
    }

    /**
     * Gets the source URL of the file.
     *
     * @return The source URL of the file.
     */
    public String getUrl() {
        return data.getUrl();
    }

    /**
     * Gets a proxied URL of the file.
     *
     * @return A proxied URL of the file.
     */
    public String getProxyUrl() {
        return data.getProxyUrl();
    }

    /**
     * Gets the height of the file, if present.
     *
     * @return The height of the file, if present.
     */
    public OptionalInt getHeight() {
        return (data.getHeight() == null) ? OptionalInt.empty() : OptionalInt.of(data.getHeight());
    }

    /**
     * Gets the width of the file, if present.
     *
     * @return The width of the file, if present.
     */
    public OptionalInt getWidth() {
        return (data.getWidth() == null) ? OptionalInt.empty() : OptionalInt.of(data.getWidth());
    }

    @Override
    public boolean equals(final Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return EntityUtil.hashCode(this);
    }
}
