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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.component;

import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.possible.Possible;

import java.util.Optional;

/**
 * A thumbnail component for message.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/message-components#???">Thumbnail</a>
 */
public class Thumbnail extends MessageComponent {

    /**
     * Creates an {@code Thumbnail}.
     *
     * @param media The media
     * @return An {@code Thumbnail}
     */
    public static Thumbnail of(UnfurledMediaItem media) {
        return new Thumbnail(MessageComponent.getBuilder(Type.THUMBNAIL).media(media.getData()).build());
    }

    /**
     * Creates an {@code Thumbnail}.
     *
     * @param media The media
     * @param description The description
     * @return An {@code Thumbnail}
     */
    public static Thumbnail of(UnfurledMediaItem media, String description) {
        return new Thumbnail(MessageComponent.getBuilder(Type.THUMBNAIL).media(media.getData()).description(Possible.of(Optional.of(description))).build());
    }

    /**
     * Creates an {@code Thumbnail}.
     *
     * @param media The media
     * @param description The description
     * @param spoiler If this component is a spoiler
     * @return An {@code Thumbnail}
     */
    public static Thumbnail of(UnfurledMediaItem media, String description, boolean spoiler) {
        return new Thumbnail(MessageComponent.getBuilder(Type.THUMBNAIL).media(media.getData()).description(Possible.of(Optional.of(description))).spoiler(spoiler).build());
    }

    Thumbnail(ComponentData data) {
        super(data);
    }

    /**
     * Gets the media related to this thumbnail.
     *
     * @return An {@code UnfurledMediaItem}
     */
    public UnfurledMediaItem getMedia() {
        return new UnfurledMediaItem(this.getData().media().get());
    }

    /**
     * Gets the description for this thumbnail.
     *
     * @return The description
     */
    public String getDescription() {
        return Possible.flatOpt(this.getData().description()).orElse("");
    }

    /**
     * Gets if this component is a spoiler.
     *
     * @return {@code true} if is spoiler, false otherwise
     */
    public boolean isSpoiler() {
        return this.getData().spoiler().get();
    }

}
