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
 * A text display component for message.
 * <br>
 * <b>Note:</b> the components in the entire message support a max of {@link #MAX_DISPLAY_CHARACTERS_LENGTH} characters in TextDisplay.
 * <br>
 * If consider 4000 for limit then consider you can:
 * <ul>
 *     <li>two {@link TextDisplay} each with 2000 characters</li>
 *     <li>one {@link TextDisplay} each with 4000 characters</li>
 *     <li>three {@link TextDisplay}, one with 2000 characters and two with 1000 characters</li>
 * </ul>
 * @see <a href="https://discord.com/developers/docs/interactions/message-components#???">Text Display</a>
 */
public class TextDisplay extends MessageComponent {

    /** The maximum amount of characters that can be in the sum of these components. */
    public static final int MAX_DISPLAY_CHARACTERS_LENGTH = 4000;

    /**
     * Creates an {@code TextDisplay}.
     *
     * @param content The content
     * @return An {@code TextDisplay}
     */
    public static TextDisplay of(String content) {
        return new TextDisplay(MessageComponent.getBuilder(Type.TEXT_DISPLAY).content(content).build());
    }

    TextDisplay(ComponentData data) {
        super(data);
    }

    /**
     * Gets the content for this text display.
     *
     * @return The content
     */
    public String getContent() {
        return this.getData().content().get();
    }
}
