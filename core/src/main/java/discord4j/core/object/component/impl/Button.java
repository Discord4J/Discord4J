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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.component.impl;

import discord4j.common.util.Snowflake;
import discord4j.core.object.component.attribute.ICanBeDisabledComponent;
import discord4j.core.object.component.impl.button.ActionButton;
import discord4j.core.object.component.impl.button.LinkButton;
import discord4j.core.object.component.kind.ActionComponent;
import discord4j.core.object.component.usage.ICanBeUsedAsAccessoryComponent;
import discord4j.core.object.component.usage.ICanBeUsedInActionRowComponent;
import discord4j.core.object.emoji.Emoji;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.component.ButtonComponentData;
import discord4j.discordjson.json.component.ImmutableButtonComponentData;
import discord4j.discordjson.possible.Possible;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * A message button.
 *
 * @see <a href="https://discord.com/developers/docs/components/reference#button">Buttons</a>
 */
public class Button
        extends ActionComponent<ButtonComponentData>
        implements ICanBeUsedInActionRowComponent, ICanBeUsedAsAccessoryComponent,
        ICanBeDisabledComponent<ButtonComponentData, Button> {

    /**
     * Creates a {@link Button.Style#PRIMARY primary} button.
     *
     * @param customId A developer-defined identifier for the button.
     * @return A button with the given data.
     */
    public static ActionButton primary(String customId) {
        return ActionButton.primary(customId);
    }

    /**
     * Creates a {@link Button.Style#SECONDARY secondary} button.
     *
     * @param customId A developer-defined identifier for the button.
     * @return A button with the given data.
     */
    public static ActionButton secondary(String customId) {
        return ActionButton.secondary(customId);
    }

    /**
     * Creates a {@link Button.Style#SUCCESS success} button.
     *
     * @param customId A developer-defined identifier for the button.
     * @return A button with the given data.
     */
    public static ActionButton success(String customId) {
        return ActionButton.success(customId);
    }

    /**
     * Creates a {@link Button.Style#DANGER danger} button.
     *
     * @param customId A developer-defined identifier for the button.
     * @return A button with the given data.
     */
    public static ActionButton danger(String customId) {
        return ActionButton.danger(customId);
    }

    /**
     * Creates a {@link Button.Style#LINK link} button.
     *
     * @param url The url to navigate to when clicked.
     * @return A button with the given data.
     */
    public static LinkButton link(String url) {
        return LinkButton.of(url);
    }

    /**
     * Creates a {@link Button.Style#PREMIUM premium} button.
     *
     * @param skuId the associated sku id
     * @return A button with the given data.
     */
    public static Button premium(Snowflake skuId) {
        return of(Button.Style.PREMIUM, null, null, null, null, skuId.asString());
    }

    public static Button of(Style style) {
        return of(style, null, null, null, null, null);
    }

    public static Button of(Style style, @Nullable String customId, @Nullable Emoji emoji,
                            @Nullable String label, @Nullable String url, @Nullable String skuId) {
        ImmutableButtonComponentData.Builder builder = ButtonComponentData.builder()
                .style(style.getValue());

        if (customId != null) {
            builder.customId(customId);
        }

        if (emoji != null) {
            builder.emoji(emoji.asEmojiData());
        }

        if (label != null) {
            builder.label(Possible.of(label));
        }

        if (url != null) {
            builder.url(url);
        }

        if (skuId != null) {
            builder.skuId(skuId);
        }

        return new Button(builder.build());
    }

    public static Button of(int id, Style style, @Nullable String customId, @Nullable Emoji emoji,
                            @Nullable String label, @Nullable String url, @Nullable String skuId) {
        ImmutableButtonComponentData.Builder builder = ButtonComponentData.builder()
                .id(id)
                .style(style.getValue());

        if (customId != null) {
            builder.customId(customId);
        }

        if (emoji != null) {
            builder.emoji(emoji.asEmojiData());
        }

        if (label != null) {
            builder.label(label);
        }

        if (url != null) {
            builder.url(url);
        }

        if (skuId != null) {
            builder.skuId(skuId);
        }

        return new Button(builder.build());
    }

    public static Button of(ButtonComponentData data) {
        return new Button(data);
    }

    protected Button(Integer id, Style style, @Nullable String customId, @Nullable Emoji emoji,
                     @Nullable String label, @Nullable String url, @Nullable String skuId) {
        super(ButtonComponentData.builder()
                .id(Possible.ofNullable(id))
                .style(style.getValue())
                .customId(Possible.ofNullable(customId))
                .emoji(Possible.ofNullable(emoji).map(Emoji::asEmojiData))
                .label(Possible.ofNullable(label))
                .url(Possible.ofNullable(url))
                .skuId(Possible.ofNullable(skuId).map(Id::of))
                .build()
        );
    }

    protected Button(ButtonComponentData data) {
        super(data);
    }

    /**
     * Gets the button's style.
     *
     * @return The button's style.
     */
    public Style getStyle() {
        return Style.of(getData().style());
    }

    /**
     * Gets button's label.
     *
     * @return The button's label.
     */
    public Optional<String> getLabel() {
        return getData().label().toOptional();
    }

    /**
     * Gets the button's emoji.
     *
     * @return The button's emoji.
     */
    public Optional<Emoji> getEmoji() {
        return getData().emoji().toOptional()
                .map(Emoji::of);
    }

    /**
     * Gets the button's url.
     *
     * @return The button's url.
     */
    public Optional<String> getUrl() {
        return getData().url().toOptional();
    }

    /**
     * Get the button's sku id.
     *
     * @return The button's sku id if present.
     */
    public Optional<Snowflake> getSkuId() {
        return getData().skuId().toOptional().map(Snowflake::of);
    }

    @Override
    public Button withDisabled(boolean disabled) {
        return new Button(ButtonComponentData.builder().from(getData()).disabled(disabled).build());
    }

    /**
     * A button's style is what determines its color and whether it has a custom id or a url.
     *
     * @see
     * <a href="https://discord.com/developers/docs/interactions/message-components#buttons-button-styles">Button Styles</a>
     */
    public enum Style {
        UNKNOWN(-1),
        PRIMARY(1),
        SECONDARY(2),
        SUCCESS(3),
        DANGER(4),
        LINK(5),
        PREMIUM(6);

        private final int value;

        Style(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Style of(int value) {
            switch (value) {
                case 1:
                    return PRIMARY;
                case 2:
                    return SECONDARY;
                case 3:
                    return SUCCESS;
                case 4:
                    return DANGER;
                case 5:
                    return LINK;
                case 6:
                    return PREMIUM;
                default:
                    return UNKNOWN;
            }
        }
    }
}
