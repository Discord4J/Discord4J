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

/**
 * A file component for message.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/message-components#???">File</a>
 */
public class File extends MessageComponent {

    /**
     * Creates an {@code File} with the given {@code UnfurledMediaItem}.
     *
     * @param file The file component with an {@code attachment://} reference
     * @return An {@code SectionComponent} containing the given components
     */
    public static File of(UnfurledMediaItem file) {
        return new File(MessageComponent.getBuilder(Type.FILE).file(file.getData()).build());
    }

    /**
     * Creates an {@code File} with the given {@code UnfurledMediaItem}.
     *
     * @param file The file component with an {@code attachment://} reference
     * @param spoiler Sets the spoiler
     * @return An {@code SectionComponent} containing the given components
     */
    public static File of(UnfurledMediaItem file, boolean spoiler) {
        return new File(MessageComponent.getBuilder(Type.FILE).file(file.getData()).spoiler(spoiler).build());
    }

    File(ComponentData data) {
        super(data);
    }

    /**
     * Gets the media file attached to this component.
     *
     * @return a {@link UnfurledMediaItem}
     */
    public UnfurledMediaItem getFile() {
        return new UnfurledMediaItem(this.getData().file().get());
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
