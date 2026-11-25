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

import discord4j.core.object.emoji.Emoji;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.ImmutableComponentData;
import discord4j.discordjson.possible.Possible;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * An item of an {@link OverflowMenuGroup}, shown when the {@link OverflowMenu} is opened.
 *
 * @see <a href="https://discord.com/developers/docs/components/reference#overflow-menu">Overflow Menu</a>
 */
public class MenuActionItem extends ActionComponent {

    /**
     * Creates a {@link Style#DEFAULT default} menu action item.
     *
     * @param customId A developer-defined identifier for the item.
     * @param label The text that appears on the item.
     * @return An item with the given data.
     */
    public static MenuActionItem defaultStyle(final String customId, final String label) {
        return of(null, Style.DEFAULT, customId, null, label, null);
    }

    /**
     * Creates a {@link Style#DEFAULT default} menu action item.
     *
     * @param customId A developer-defined identifier for the item.
     * @param emoji The emoji that appears on the item.
     * @param label The text that appears on the item.
     * @return An item with the given data.
     */
    public static MenuActionItem defaultStyle(final String customId, final Emoji emoji, final String label) {
        return of(null, Style.DEFAULT, customId, emoji, label, null);
    }

    /**
     * Creates a {@link Style#DEFAULT default} menu action item.
     *
     * @param id the component id
     * @param customId A developer-defined identifier for the item.
     * @param label The text that appears on the item.
     * @return An item with the given data.
     */
    public static MenuActionItem defaultStyle(final int id, final String customId, final String label) {
        return of(id, Style.DEFAULT, customId, null, label, null);
    }

    /**
     * Creates a {@link Style#DEFAULT default} menu action item.
     *
     * @param id the component id
     * @param customId A developer-defined identifier for the item.
     * @param emoji The emoji that appears on the item.
     * @param label The text that appears on the item.
     * @return An item with the given data.
     */
    public static MenuActionItem defaultStyle(final int id, final String customId, final Emoji emoji,
                                              final String label) {
        return of(id, Style.DEFAULT, customId, emoji, label, null);
    }

    /**
     * Creates a {@link Style#DANGER danger} menu action item.
     *
     * @param customId A developer-defined identifier for the item.
     * @param label The text that appears on the item.
     * @return An item with the given data.
     */
    public static MenuActionItem danger(final String customId, final String label) {
        return of(null, Style.DANGER, customId, null, label, null);
    }

    /**
     * Creates a {@link Style#DANGER danger} menu action item.
     *
     * @param customId A developer-defined identifier for the item.
     * @param emoji The emoji that appears on the item.
     * @param label The text that appears on the item.
     * @return An item with the given data.
     */
    public static MenuActionItem danger(final String customId, final Emoji emoji, final String label) {
        return of(null, Style.DANGER, customId, emoji, label, null);
    }

    /**
     * Creates a {@link Style#DANGER danger} menu action item.
     *
     * @param id the component id
     * @param customId A developer-defined identifier for the item.
     * @param label The text that appears on the item.
     * @return An item with the given data.
     */
    public static MenuActionItem danger(final int id, final String customId, final String label) {
        return of(id, Style.DANGER, customId, null, label, null);
    }

    /**
     * Creates a {@link Style#DANGER danger} menu action item.
     *
     * @param id the component id
     * @param customId A developer-defined identifier for the item.
     * @param emoji The emoji that appears on the item.
     * @param label The text that appears on the item.
     * @return An item with the given data.
     */
    public static MenuActionItem danger(final int id, final String customId, final Emoji emoji,
                                        final String label) {
        return of(id, Style.DANGER, customId, emoji, label, null);
    }

    /**
     * Creates a {@link Style#LINK link} menu action item.
     *
     * @param url The url to navigate to when clicked.
     * @param label The text that appears on the item.
     * @return An item with the given data.
     */
    public static MenuActionItem link(final String url, final String label) {
        return of(null, Style.LINK, null, null, label, url);
    }

    /**
     * Creates a {@link Style#LINK link} menu action item.
     *
     * @param url The url to navigate to when clicked.
     * @param emoji The emoji that appears on the item.
     * @param label The text that appears on the item.
     * @return An item with the given data.
     */
    public static MenuActionItem link(final String url, final Emoji emoji, final String label) {
        return of(null, Style.LINK, null, emoji, label, url);
    }

    /**
     * Creates a {@link Style#LINK link} menu action item.
     *
     * @param id the component id
     * @param url The url to navigate to when clicked.
     * @param label The text that appears on the item.
     * @return An item with the given data.
     */
    public static MenuActionItem link(final int id, final String url, final String label) {
        return of(id, Style.LINK, null, null, label, url);
    }

    /**
     * Creates a {@link Style#LINK link} menu action item.
     *
     * @param id the component id
     * @param url The url to navigate to when clicked.
     * @param emoji The emoji that appears on the item.
     * @param label The text that appears on the item.
     * @return An item with the given data.
     */
    public static MenuActionItem link(final int id, final String url, final Emoji emoji, final String label) {
        return of(id, Style.LINK, null, emoji, label, url);
    }

    private static MenuActionItem of(final @Nullable Integer id, final Style style,
                                     final @Nullable String customId, final @Nullable Emoji emoji,
                                     final String label, final @Nullable String url) {
        final ImmutableComponentData.Builder builder = MessageComponent.getBuilder(Type.MENU_ACTION_ITEM)
                .style(style.getValue())
                .label(Possible.of(Optional.of(label)));

        if (id != null) {
            builder.id(id);
        }

        if (customId != null) {
            builder.customId(customId);
        }

        if (emoji != null) {
            builder.emoji(emoji.asEmojiData());
        }

        if (url != null) {
            builder.url(url);
        }

        return new MenuActionItem(builder.build());
    }

    /**
     * Creates a new item with the given description, keeping the rest of the data.
     *
     * @param description The description shown below the label, or {@code null} to remove it.
     * @return A new item with the given description.
     */
    public MenuActionItem withDescription(final @Nullable String description) {
        return new MenuActionItem(ImmutableComponentData.builder()
                .from(this.getData())
                .description(Possible.of(Optional.ofNullable(description)))
                .build());
    }

    MenuActionItem(final ComponentData data) {
        super(data);
    }

    /**
     * Gets the label of this item.
     *
     * @return The label of this item
     */
    public String getLabel() {
        return Possible.flatOpt(this.getData().label()).orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the style of this item.
     *
     * @return The style of this item
     */
    public Style getStyle() {
        return Style.of(this.getData().style().toOptional().orElseThrow(IllegalStateException::new));
    }

    /**
     * Gets the description of this item.
     *
     * @return The description of this item, or an empty {@link Optional} if not present
     */
    public Optional<String> getDescription() {
        return Possible.flatOpt(this.getData().description());
    }

    /**
     * Gets the url of this item.
     *
     * @return The url of this item, or an empty {@link Optional} if not present
     */
    public Optional<String> getURL() {
        return this.getData().url().toOptional();
    }

    /**
     * Gets the emoji of this item.
     *
     * @return The emoji of this item, or an empty {@link Optional} if not present
     */
    public Optional<Emoji> getEmoji() {
        return this.getData().emoji().toOptional()
                .map(Emoji::of);
    }

    /**
     * The style of a {@link MenuActionItem}.
     */
    public enum Style {
        UNKNOWN(-1),
        DEFAULT(1),
        DANGER(2),
        LINK(3);

        private final int value;

        Style(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static Style of(final int value) {
            switch (value) {
                case 1:
                    return DEFAULT;
                case 2:
                    return DANGER;
                case 3:
                    return LINK;
                default:
                    return UNKNOWN;
            }
        }
    }
}
