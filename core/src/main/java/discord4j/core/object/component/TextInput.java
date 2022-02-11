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

package discord4j.core.object.component;

import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.ImmutableComponentData;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * A modal-only text input field
 *
 * @see <a href="https://discord.com/developers/docs/interactions/message-components#text-inputs">Text Inputs</a>
 */
public class TextInput extends ActionComponent {

    /**
     * Creates a {@link TextInput.Style#SHORT short} button.
     *
     * @param customId A developer-defined identifier for the button.
     * @return A button with the given data.
     */
    public static TextInput small(String customId) {
        return of(Style.SHORT, customId, null, null, null, null, null);
    }

    /**
     * Creates a {@link TextInput.Style#SHORT short} button.
     *
     * @param customId A developer-defined identifier for the button.
     * @param label The text that appears above the input
     * @return A button with the given data.
     */
    public static TextInput small(String customId, String label) {
        return of(Style.SHORT, customId, label, null, null, null, null);
    }

    /**
     * Creates a {@link TextInput.Style#SHORT short} button.
     *
     * @param customId A developer-defined identifier for the button.
     * @param label The text that appears above the input
     * @param placeholder The placeholder text to be displayed
     * @return A button with the given data.
     */
    public static TextInput small(String customId, String label, String placeholder) {
        return of(Style.SHORT, customId, label, null, null, null, placeholder);
    }

    /**
     * Creates a {@link TextInput.Style#SHORT short} button.
     *
     * @param customId A developer-defined identifier for the button.
     * @param label The text that appears above the input
     * @param minLength The minimum length the user is required to input
     * @param maxLength The maximum length the user is required to input
     * @return A button with the given data.
     */
    public static TextInput small(String customId, String label, int minLength, int maxLength) {
        return of(Style.SHORT, customId, label, minLength, maxLength, null, null);
    }

    /**
     * Creates a {@link TextInput.Style#PARAGRAPH paragraph} button.
     *
     * @param customId A developer-defined identifier for the button.
     * @return A button with the given data.
     */
    public static TextInput paragraph(String customId) {
        return of(Style.PARAGRAPH, customId, null, null, null, null, null);
    }

    /**
     * Creates a {@link TextInput.Style#PARAGRAPH paragraph} button.
     *
     * @param customId A developer-defined identifier for the button.
     * @param label The text that appears above the input
     * @return A button with the given data.
     */
    public static TextInput paragraph(String customId, String label) {
        return of(Style.PARAGRAPH, customId, label, null, null, null, null);
    }

    /**
     * Creates a {@link TextInput.Style#PARAGRAPH paragraph} button.
     *
     * @param customId A developer-defined identifier for the button.
     * @param label The text that appears above the input
     * @param placeholder The placeholder text to display
     * @return A button with the given data.
     */
    public static TextInput paragraph(String customId, String label, String placeholder) {
        return of(Style.PARAGRAPH, customId, label, null, null, null, placeholder);
    }

    /**
     * Creates a {@link TextInput.Style#PARAGRAPH paragraph} button.
     *
     * @param customId A developer-defined identifier for the button.
     * @param label The text that appears above the input
     * @param minLength The minimum length the user is required to input
     * @param maxLength The maximum length the user is required to input
     * @return A button with the given data.
     */
    public static TextInput paragraph(String customId, String label, int minLength, int maxLength) {
        return of(Style.PARAGRAPH, customId, label, minLength, maxLength, null, null);
    }

    private static TextInput of(Style style, String customId, @Nullable String label, @Nullable Integer minLength,
                                @Nullable Integer maxLength, @Nullable String value, @Nullable String placeholder) {
        ImmutableComponentData.Builder builder = ComponentData.builder()
                .type(MessageComponent.Type.TEXT_INPUT.getValue())
                .style(style.getValue())
                .customId(customId);

        if (label != null) {
            builder.label(label);
        }

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

    TextInput(ComponentData data) {
        super(data);
    }

    /**
     * Gets the text input's style.
     *
     * @return the style of the text input
     */
    public Style getStyle() {
        return getData().style().toOptional()
                .map(Style::of)
                .orElseThrow(IllegalStateException::new); // style should always be present on text inputs
    }

    /**
     * Gets the text input's custom id.
     *
     * @return the custom id of the text input
     */
    public String getCustomId() {
        return getData().customId().toOptional()
                .orElseThrow(IllegalStateException::new); // custom id should always be present on text inputs
    }

    /**
     * Gets the text input's label
     *
     * @return The text input's label
     */
    public Optional<String> getLabel() {
        return getData().label().toOptional();
    }

    /**
     * Gets the text input's minimum length requirement. If not present, defaults to {@code 0}.
     *
     * @return The text input's minimum length
     */
    public int getMinLength() {
        return getData().minLength().toOptional().orElse(0);
    }

    /**
     * Gets the text input's maximum length requirement. If not present, defaults to {@code 4000}
     *
     * @return The text input's maximum length
     */
    public int getMaxLength() {
        return getData().maxLength().toOptional().orElse(4000);
    }

    /**
     * Gets whether the text input is required to be filled. Defaults to {@code false}
     *
     * @return Whether the text input is required
     */
    public boolean isRequired() {
        return getData().required().toOptional().orElse(false);
    }

    /**
     * Gets text input's value, if any.
     *
     * @return The text input's value
     */
    public Optional<String> getValue() {
        return getData().value().toOptional();
    }

    /**
     * Gets the text input's placeholder text, if any.
     *
     * @return The text input's placeholder
     */
    public Optional<String> getPlaceholder() {
        return getData().placeholder().toOptional();
    }

    /**
     * Creates a new text input with the same data as this one, but required.
     *
     * @return A new required text input with the same data as this one.
     */
    public TextInput required() {
        return required(true);
    }

    /**
     * Creates a new text input with the same data as this one, but depending on the value param it may be required
     * or not.
     *
     * @param value True if the text input should be required otherwise False.
     * @return A new possibly required button with the same data as this one.
     */
    public TextInput required(boolean value) {
        return new TextInput(ComponentData.builder().from(getData()).required(value).build());
    }

    /**
     * Creates a new text input with the same data as this, but with a pre-filled value.
     *
     * @param value The pre-filled text value
     * @return A new text input with the same data as this one.
     */
    public TextInput prefilled(String value) {
        return new TextInput(ComponentData.builder().from(getData()).value(value).build());
    }

    /**
     * Creates a new text input with the same data as this, but with placeholder text.
     *
     * @param value The placeholder text value
     * @return A new text input with the same data as this one.
     */
    public TextInput placeholder(String value) {
        return new TextInput(ComponentData.builder().from(getData()).placeholder(value).build());
    }

    /**
     * A text input's style is what determines its size and behavior
     *
     * @see <a href="https://discord.com/developers/docs/interactions/message-components#text-inputs-text-input-styles">Text Input Styles</a>
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
                case 1: return SHORT;
                case 2: return PARAGRAPH;
                default: return UNKNOWN;
            }
        }
    }
}
