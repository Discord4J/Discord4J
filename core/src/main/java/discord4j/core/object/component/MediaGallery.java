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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MediaGallery extends MessageComponent {

    public static MediaGallery of(MediaGalleryItem... mediaGalleryItems) {
        return of(Arrays.asList(mediaGalleryItems));
    }

    public static MediaGallery of(List<MediaGalleryItem> mediaGalleryItems) {
        return new MediaGallery(MessageComponent.getBuilder(Type.MEDIA_GALLERY).addAllItems(mediaGalleryItems.stream().map(MediaGalleryItem::getData).collect(Collectors.toList())).build());
    }

    MediaGallery(ComponentData data) {
        super(data);
    }

    public List<MediaGalleryItem> getItems() {
        return this.getData().items().toOptional()
            .orElse(Collections.emptyList()).stream()
            .map(MediaGalleryItem::new)
            .collect(Collectors.toList());
    }
}
