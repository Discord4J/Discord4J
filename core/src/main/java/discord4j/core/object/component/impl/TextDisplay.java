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
import discord4j.core.object.component.usage.ICanBeUsedInContainerComponent;
import discord4j.core.object.component.usage.ICanBeUsedInSectionComponent;
import discord4j.core.object.component.kind.TopLevelComponent;
import discord4j.core.object.component.kind.TopLevelModalComponent;
import discord4j.discordjson.json.component.TextDisplayComponentData;
import discord4j.discordjson.possible.Possible;

/**
 * A text display component for message.
 * <br>
 * <b>Note:</b> the components in the entire message support a max of {@value #MAX_DISPLAY_CHARACTERS_LENGTH}
 * characters.
 * <br>
 * For example, you can have:
 * <ul>
 *     <li>two {@link TextDisplay}, each with 2000 characters</li>
 *     <li>one {@link TextDisplay}, each with 4000 characters</li>
 *     <li>three {@link TextDisplay}, one with 2000 characters and two with 1000 characters</li>
 * </ul>
 *
 * @apiNote This component requires {@link discord4j.core.object.entity.Message.Flag#IS_COMPONENTS_V2} if used in
 * messages
 * @see <a href="https://discord.com/developers/docs/components/reference#text-display">Text Display</a>
 */
public class TextDisplay extends Component<TextDisplayComponentData> implements TopLevelComponent,
        TopLevelModalComponent, ICanBeUsedInContainerComponent, ICanBeUsedInSectionComponent {

    /**
     * The maximum amount of characters that can be in the sum of these components.
     */
    public static final int MAX_DISPLAY_CHARACTERS_LENGTH = 4000;

    /**
     * Creates a {@link TextDisplay}.
     *
     * @param content The content
     * @return A {@link TextDisplay}
     */
    public static TextDisplay of(String content) {
        return new TextDisplay(TextDisplayComponentData.builder()
                .content(content)
                .build());
    }

    /**
     * Creates a {@link TextDisplay}.
     *
     * @param id      the component id
     * @param content The content
     * @return A {@link TextDisplay}
     */
    public static TextDisplay of(int id, String content) {
        return new TextDisplay(TextDisplayComponentData.builder()
                .id(id)
                .content(content)
                .build());
    }

    protected TextDisplay(Integer id, String content) {
        this(TextDisplayComponentData.builder()
                .id(Possible.ofNullable(id))
                .content(content)
                .build());
    }

    public static TextDisplay of(TextDisplayComponentData data) {
        return new TextDisplay(data);
    }

    private TextDisplay(TextDisplayComponentData data) {
        super(data);
    }

    /**
     * Gets the content for this text display.
     *
     * @return The content
     */
    public String getContent() {
        return this.getData().content();
    }
}
