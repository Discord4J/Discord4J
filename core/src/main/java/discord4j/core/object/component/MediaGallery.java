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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A media gallery component for message.
 *
 * @apiNote This component require {@link discord4j.core.object.entity.Message.Flag#IS_COMPONENTS_V2}
 * @see <a href="https://discord.com/developers/docs/interactions/message-components#???">Media Gallery</a>
 */
public class MediaGallery extends MessageComponent implements TopLevelMessageComponent, ICanBeUsedInContainerComponent {

    /**
     * Creates an {@link MediaGallery} with the given {@link MediaGalleryItem}.
     *
     * @param mediaGalleryItems The items for the gallery
     * @return A {@link MediaGallery} containing the given items
     */
    public static MediaGallery of(MediaGalleryItem... mediaGalleryItems) {
        return of(Arrays.asList(mediaGalleryItems));
    }

    /**
     * Creates an {@link MediaGallery} with the given {@link MediaGalleryItem}.
     *
     * @param mediaGalleryItems The items for the gallery
     * @return A {@link MediaGallery} containing the given items
     */
    public static MediaGallery of(List<MediaGalleryItem> mediaGalleryItems) {
        return new MediaGallery(MessageComponent.getBuilder(Type.MEDIA_GALLERY)
            .addAllItems(mediaGalleryItems.stream().map(MediaGalleryItem::getData).collect(Collectors.toList()))
            .build());
    }

    /**
     * Creates an {@link MediaGallery} with the given {@link MediaGalleryItem}.
     *
     * @param id the component id
     * @param mediaGalleryItems The items for the gallery
     * @return A {@link MediaGallery} containing the given items
     */
    public static MediaGallery of(int id, MediaGalleryItem... mediaGalleryItems) {
        return of(id, Arrays.asList(mediaGalleryItems));
    }

    /**
     * Creates an {@link MediaGallery} with the given {@link MediaGalleryItem}.
     *
     * @param id the component id
     * @param mediaGalleryItems The items for the gallery
     * @return A {@link MediaGallery} containing the given items
     */
    public static MediaGallery of(int id, List<MediaGalleryItem> mediaGalleryItems) {
        return new MediaGallery(MessageComponent.getBuilder(Type.MEDIA_GALLERY)
            .id(id)
            .addAllItems(mediaGalleryItems.stream().map(MediaGalleryItem::getData).collect(Collectors.toList()))
            .build());
    }

    protected MediaGallery(Integer id, List<MediaGalleryItem> mediaGalleryItems) {
        this(MessageComponent.getBuilder(Type.MEDIA_GALLERY)
            .id(Possible.ofNullable(id))
            .addAllItems(mediaGalleryItems.stream().map(MediaGalleryItem::getData).collect(Collectors.toList()))
            .build());
    }

    MediaGallery(ComponentData data) {
        super(data);
    }

    /**
     * Gets the media items.
     *
     * @return A list of {@link MediaGalleryItem}
     */
    public List<MediaGalleryItem> getItems() {
        return this.getData().items().toOptional()
            .orElse(Collections.emptyList()).stream()
            .map(MediaGalleryItem::new)
            .collect(Collectors.toList());
    }
}
