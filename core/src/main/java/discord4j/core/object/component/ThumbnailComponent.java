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
import discord4j.discordjson.json.ImmutableComponentData;
import discord4j.discordjson.possible.Possible;

import java.util.Optional;

public class ThumbnailComponent extends MessageComponent {

    public static ThumbnailComponent of(UnfurledMediaItem media) {
        return new ThumbnailComponent(MessageComponent.getBuilder(Type.THUMBNAIL).media(media.getData()).build());
    }

    public static ThumbnailComponent of(UnfurledMediaItem media, String description) {
        return new ThumbnailComponent(MessageComponent.getBuilder(Type.THUMBNAIL).media(media.getData()).description(Possible.of(Optional.of(description))).build());
    }

    public static ThumbnailComponent of(UnfurledMediaItem media, String description, boolean spoiler) {
        return new ThumbnailComponent(MessageComponent.getBuilder(Type.THUMBNAIL).media(media.getData()).description(Possible.of(Optional.of(description))).spoiler(spoiler).build());
    }

    ThumbnailComponent(ComponentData data) {
        super(data);
    }

    public UnfurledMediaItem getMedia() {
        return new UnfurledMediaItem(this.getData().media().get());
    }

    public String getDescription() {
        return Possible.flatOpt(this.getData().description()).orElse("");
    }

    public boolean isSpoiler() {
        return this.getData().spoiler().get();
    }

}
