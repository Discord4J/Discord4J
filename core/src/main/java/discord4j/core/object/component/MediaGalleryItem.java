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

import discord4j.discordjson.json.MediaGalleryItemData;
import discord4j.discordjson.possible.Possible;

import java.util.Optional;

/**
 * Represents a Media Gallery item.
 */
public class MediaGalleryItem {

    /**
     * Creates an {@code MediaGalleryItem}.
     *
     * @param mediaItem The {@link UnfurledMediaItem} item for display
     * @return An {@code MediaGalleryItem} containing the given items
     */
    public static MediaGalleryItem of(UnfurledMediaItem mediaItem) {
        return new MediaGalleryItem(MediaGalleryItemData.builder().media(mediaItem.getData()).build());
    }

    /**
     * Creates an {@code MediaGalleryItem}.
     *
     * @param mediaItem The {@link UnfurledMediaItem} item for display
     * @param description The description for this item
     * @return An {@code MediaGalleryItem} containing the given items
     */
    public static MediaGalleryItem of(UnfurledMediaItem mediaItem, String description) {
        return new MediaGalleryItem(MediaGalleryItemData.builder()
            .media(mediaItem.getData())
            .description(Possible.of(Optional.of(description)))
            .build());
    }

    /**
     * Creates an {@code MediaGalleryItem}.
     *
     * @param mediaItem The {@link UnfurledMediaItem} item for display
     * @param spoiler If this component it's a spoiler
     * @return An {@code MediaGalleryItem} containing the given items
     */
    public static MediaGalleryItem of(UnfurledMediaItem mediaItem, boolean spoiler) {
        return new MediaGalleryItem(MediaGalleryItemData.builder()
            .media(mediaItem.getData())
            .spoiler(spoiler)
            .build());
    }

    /**
     * Creates an {@code MediaGalleryItem}.
     *
     * @param mediaItem The {@link UnfurledMediaItem} item for display
     * @param description The description for this item
     * @param spoiler If this component it's a spoiler
     * @return An {@code MediaGalleryItem} containing the given items
     */
    public static MediaGalleryItem of(UnfurledMediaItem mediaItem, String description, boolean spoiler) {
        return new MediaGalleryItem(MediaGalleryItemData.builder()
            .media(mediaItem.getData())
            .description(Possible.of(Optional.of(description)))
            .spoiler(spoiler)
            .build());
    }

    private final MediaGalleryItemData data;

    protected MediaGalleryItem(UnfurledMediaItem mediaItem, String description, boolean spoiler) {
        this(MediaGalleryItemData.builder()
            .media(mediaItem.getData())
            .description(Possible.of(Optional.of(description)))
            .spoiler(spoiler)
            .build());
    }

    MediaGalleryItem(MediaGalleryItemData data) {
        this.data = data;
    }

    public MediaGalleryItemData getData() {
        return this.data;
    }

    public UnfurledMediaItem getMedia() {
        return new UnfurledMediaItem(this.getData().media());
    }

    public String getDescription() {
        return Possible.flatOpt(this.getData().description()).orElse("");
    }

    /**
     * Gets if this component is a spoiler.
     *
     * @return {@code true} if is spoiler, false otherwise
     */
    public boolean isSpoiler() {
        return this.getData().spoiler().toOptional().orElse(false);
    }
}
