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
package discord4j.core.object.component.impl;

import discord4j.core.object.component.Component;
import discord4j.core.object.component.usage.ICanBeUsedAsAccessoryComponent;
import discord4j.core.object.component.impl.item.UnfurledMediaItem;
import discord4j.discordjson.json.component.ThumbnailComponentData;
import discord4j.discordjson.possible.Possible;

import java.util.Optional;

/**
 * A thumbnail component for message.
 *
 * @apiNote This component require {@link discord4j.core.object.entity.Message.Flag#IS_COMPONENTS_V2}
 * @see <a href="https://discord.com/developers/docs/components/reference#thumbnail">Thumbnail</a>
 */
public class Thumbnail extends Component<ThumbnailComponentData> implements ICanBeUsedAsAccessoryComponent {

    /**
     * Creates a {@link Thumbnail}.
     *
     * @param media The media for this thumbnail
     * @return A {@link Thumbnail}
     */
    public static Thumbnail of(UnfurledMediaItem media) {
        return new Thumbnail(ThumbnailComponentData.builder()
                .media(media.getData())
                .build());
    }

    /**
     * Creates a {@link Thumbnail}.
     *
     * @param media       The media for this thumbnail
     * @param description The description for this thumbnail
     * @return A {@link Thumbnail}
     */
    public static Thumbnail of(UnfurledMediaItem media, String description) {
        return new Thumbnail(ThumbnailComponentData.builder()
                .media(media.getData())
                .description(Possible.ofNullable(description))
                .build());
    }

    /**
     * Creates a {@link Thumbnail}.
     *
     * @param media   The media for this thumbnail
     * @param spoiler If this component is a spoiler
     * @return A {@link Thumbnail}
     */
    public static Thumbnail of(UnfurledMediaItem media, boolean spoiler) {
        return new Thumbnail(ThumbnailComponentData.builder()
                .media(media.getData())
                .spoiler(spoiler)
                .build());
    }

    /**
     * Creates a {@link Thumbnail}.
     *
     * @param media       The media for this thumbnail
     * @param description The description for this thumbnail
     * @param spoiler     If this component is a spoiler
     * @return A {@link Thumbnail}
     */
    public static Thumbnail of(UnfurledMediaItem media, String description, boolean spoiler) {
        return new Thumbnail(ThumbnailComponentData.builder()
                .media(media.getData())
                .description(Possible.ofNullable(description))
                .spoiler(spoiler)
                .build());
    }

    /**
     * Creates a {@link Thumbnail}.
     *
     * @param id    the component id
     * @param media The media for this thumbnail
     * @return A {@link Thumbnail}
     */
    public static Thumbnail of(int id, UnfurledMediaItem media) {
        return new Thumbnail(ThumbnailComponentData.builder()
                .id(id)
                .media(media.getData())
                .build());
    }

    /**
     * Creates a {@link Thumbnail}.
     *
     * @param id          the component id
     * @param media       The media for this thumbnail
     * @param description The description for this thumbnail
     * @return A {@link Thumbnail}
     */
    public static Thumbnail of(int id, UnfurledMediaItem media, String description) {
        return new Thumbnail(ThumbnailComponentData.builder()
                .id(id)
                .media(media.getData())
                .description(Possible.ofNullable(description))
                .build());
    }

    /**
     * Creates a {@link Thumbnail}.
     *
     * @param id      the component id
     * @param media   The media for this thumbnail
     * @param spoiler If this component is a spoiler
     * @return A {@link Thumbnail}
     */
    public static Thumbnail of(int id, UnfurledMediaItem media, boolean spoiler) {
        return new Thumbnail(ThumbnailComponentData.builder()
                .id(id)
                .media(media.getData())
                .spoiler(spoiler)
                .build());
    }

    /**
     * Creates a {@link Thumbnail}.
     *
     * @param id          the component id
     * @param media       The media for this thumbnail
     * @param description The description for this thumbnail
     * @param spoiler     If this component is a spoiler
     * @return A {@link Thumbnail}
     */
    public static Thumbnail of(int id, UnfurledMediaItem media, String description, boolean spoiler) {
        return new Thumbnail(ThumbnailComponentData.builder()
                .id(id)
                .media(media.getData())
                .description(Possible.ofNullable(description))
                .spoiler(spoiler)
                .build());
    }

    protected Thumbnail(Integer id, UnfurledMediaItem media, String description, boolean spoiler) {
        this(ThumbnailComponentData.builder()
                .id(Possible.ofNullable(id))
                .media(media.getData())
                .description(Possible.ofNullable(description))
                .spoiler(spoiler)
                .build());
    }

    public static Thumbnail of(ThumbnailComponentData data) {
        return new Thumbnail(data);
    }

    private Thumbnail(ThumbnailComponentData data) {
        super(data);
    }

    /**
     * Gets the media related to this thumbnail.
     *
     * @return An {@link UnfurledMediaItem}
     */
    public UnfurledMediaItem getMedia() {
        return new UnfurledMediaItem(this.getData().media());
    }

    /**
     * Gets the description for this thumbnail.
     *
     * @return The description
     */
    public Optional<String> getDescription() {
        return this.getData().description().toOptional();
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
