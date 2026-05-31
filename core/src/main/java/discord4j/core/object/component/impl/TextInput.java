/*
 *  This file is part of Discord4J.
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

import discord4j.core.object.component.attribute.ICanBeRequiredComponent;
import discord4j.core.object.component.usage.ICanBeUsedInLabelComponent;
import discord4j.core.object.component.attribute.ICanHavePlaceholderComponent;
import discord4j.core.object.component.attribute.ICanHaveValueComponent;
import discord4j.core.object.component.kind.ActionComponent;
import discord4j.discordjson.json.component.ImmutableTextInputComponentData;
import discord4j.discordjson.json.component.TextInputComponentData;
import discord4j.discordjson.possible.Possible;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A modal-only text input field
 *
 * @see <a href="https://discord.com/developers/docs/components/reference#text-input">Text Inputs</a>
 */
public class TextInput
        extends ActionComponent<TextInputComponentData>
        implements ICanBeUsedInLabelComponent, ICanBeRequiredComponent<TextInputComponentData, TextInput>,
        ICanHavePlaceholderComponent<TextInputComponentData, TextInput>,
        ICanHaveValueComponent {

    /**
     * Creates a {@link TextInput.Style#SHORT short} text input.
     *
     * @param customId A developer-defined identifier for the text input.
     * @return A text input with the given data.
     */
    public static TextInput small(String customId) {
        return of(Style.SHORT, customId, null, null, null, null);
    }

    /**
     * Creates a {@link TextInput.Style#SHORT short} text input.
     *
     * @param customId  A developer-defined identifier for the text input.
     * @param minLength The minimum length the user is required to input
     * @param maxLength The maximum length the user is required to input
     * @return A text input with the given data.
     */
    public static TextInput small(String customId, int minLength, int maxLength) {
        return of(Style.SHORT, customId, minLength, maxLength, null, null);
    }

    /**
     * Creates a {@link TextInput.Style#SHORT short} text input.
     *
     * @param id       the component id
     * @param customId A developer-defined identifier for the text input.
     * @return A text input with the given data.
     */
    public static TextInput small(int id, String customId) {
        return of(id, Style.SHORT, customId, null, null, null, null);
    }

    /**
     * Creates a {@link TextInput.Style#PARAGRAPH paragraph} text input.
     *
     * @param customId A developer-defined identifier for the text input.
     * @return A text input with the given data.
     */
    public static TextInput paragraph(String customId) {
        return of(Style.PARAGRAPH, customId, null, null, null, null);
    }

    /**
     * Creates a {@link TextInput.Style#PARAGRAPH paragraph} text input.
     *
     * @param customId  A developer-defined identifier for the text input.
     * @param minLength The minimum length the user is required to input
     * @param maxLength The maximum length the user is required to input
     * @return A text input with the given data.
     */
    public static TextInput paragraph(String customId, int minLength, int maxLength) {
        return of(Style.PARAGRAPH, customId, minLength, maxLength, null, null);
    }

    /**
     * Creates a {@link TextInput.Style#PARAGRAPH paragraph} text input.
     *
     * @param id       the component id
     * @param customId A developer-defined identifier for the text input.
     * @return A text input with the given data.
     */
    public static TextInput paragraph(int id, String customId) {
        return of(id, Style.PARAGRAPH, customId, null, null, null, null);
    }

    private static TextInput of(int id, Style style, String customId,
                                @Nullable Integer minLength,
                                @Nullable Integer maxLength, @Nullable String value, @Nullable String placeholder) {
        ImmutableTextInputComponentData.Builder builder = TextInputComponentData.builder()
                .id(id)
                .style(style.getValue())
                .customId(customId);

        if (minLength != null) {
            builder.minLength(minLength);
        }

        if (maxLength != null) {
            builder.maxLength(maxLength);
        }

        if (value != null) {
            builder.value(value);
        }

        if (placeholder != null) {
            builder.placeholder(placeholder);
        }

        return new TextInput(builder.build());
    }

    public static TextInput of(Style style, String customId, @Nullable Integer minLength,
                               @Nullable Integer maxLength, @Nullable String value, @Nullable String placeholder) {
        ImmutableTextInputComponentData.Builder builder = TextInputComponentData.builder()
                .style(style.getValue())
                .customId(customId);

        if (minLength != null) {
            builder.minLength(minLength);
        }

        if (maxLength != null) {
            builder.maxLength(maxLength);
        }

        if (value != null) {
            builder.value(value);
        }

        if (placeholder != null) {
            builder.placeholder(placeholder);
        }

        return new TextInput(builder.build());
    }

    public static TextInput of(TextInputComponentData data) {
        return new TextInput(data);
    }

    protected TextInput(Integer id, Style style, String customId, @Nullable Integer minLength,
                        @Nullable Integer maxLength, @Nullable String value, @Nullable String placeholder) {
        this(TextInputComponentData.builder()
                .id(Possible.ofNullable(id))
                .style(style.getValue())
                .customId(customId)
                .minLength(Possible.ofNullable(minLength))
                .maxLength(Possible.ofNullable(maxLength))
                .value(Possible.ofNullable(value))
                .placeholder(Possible.ofNullable(placeholder))
                .build());
    }

    private TextInput(TextInputComponentData data) {
        super(data);
    }

    /**
     * Gets the text input's style.
     *
     * @return the style of the text input
     */
    public Style getStyle() {
        return Style.of(this.getData().style());
    }

    /**
     * Gets the text input's minimum length requirement. If not present, defaults to {@code 0}.
     *
     * @return The text input's minimum length
     */
    public int getMinLength() {
        return this.getData().minLength().toOptional().orElse(0);
    }

    /**
     * Gets the text input's maximum length requirement. If not present, defaults to {@code 4000}
     *
     * @return The text input's maximum length
     */
    public int getMaxLength() {
        return this.getData().maxLength().toOptional().orElse(4000);
    }

    public TextInput withComponentId(int id) {
        return this.create(builder -> builder.id(id));
    }

    public TextInput withStyle(Style style) {
        return this.create(builder -> builder.style(style.getValue()));
    }

    public TextInput withMinLength(int minLength) {
        return this.create(builder -> builder.minLength(minLength));
    }

    public TextInput withMaxLength(int maxLength) {
        return this.create(builder -> builder.maxLength(maxLength));
    }

    @Override
    public TextInput withPlaceholder(String placeholder) {
        return this.create(builder -> builder.placeholder(placeholder));
    }

    @Override
    public TextInput withRequired(boolean required) {
        return this.create(builder -> builder.required(required));
    }

    /**
     * Creates a new text input with the same data as this, but with a pre-filled value.
     *
     * @param value The pre-filled text value
     * @return A new text input with the same data as this one.
     */
    public TextInput prefilled(String value) {
        return this.create(builder -> builder.value(value));
    }

    private TextInput create(Consumer<ImmutableTextInputComponentData.Builder> builderConsumer) {
        ImmutableTextInputComponentData.Builder dataBuilder = TextInputComponentData.builder().from(this.getData());
        builderConsumer.accept(dataBuilder);
        return new TextInput(dataBuilder.build());
    }

    /**
     * A text input's style is what determines its size and behavior
     *
     * @see
     * <a href="https://discord.com/developers/docs/interactions/message-components#text-inputs-text-input-styles">Text Input Styles</a>
     */
    public enum Style {
        UNKNOWN(-1),
        SHORT(1),
        PARAGRAPH(2);

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
                    return SHORT;
                case 2:
                    return PARAGRAPH;
                default:
                    return UNKNOWN;
            }
        }
    }
}
