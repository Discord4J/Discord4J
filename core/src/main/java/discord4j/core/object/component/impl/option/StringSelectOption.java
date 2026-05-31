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
package discord4j.core.object.component.impl.option;

import discord4j.core.object.emoji.Emoji;
import discord4j.discordjson.json.component.StringSelectComponentData;

import java.util.Optional;

/**
 * An option for a {@link discord4j.core.object.component.impl.selectmenu.StringSelectMenu}
 *
 * @see discord4j.core.object.component.impl.selectmenu.StringSelectMenu
 */
public class StringSelectOption {

    private final StringSelectComponentData.StringSelectOptionData optionData;

    /**
     * Construct a new {@link StringSelectOption} with the provided label and value
     *
     * @param label The label of the option
     * @param value The value of the option
     * @return A new {@link StringSelectOption} with the provided label and value
     */
    public static StringSelectOption of(String label, String value) {
        return new StringSelectOption(StringSelectComponentData.StringSelectOptionData.builder()
                .label(label)
                .value(value)
                .build());
    }

    /**
     * Construct a new {@link StringSelectOption} with the provided label and value and set as default
     *
     * @param label The label of the option
     * @param value The value of the option
     * @return A new {@link StringSelectOption} with the provided label, value, and set as default
     */
    public static StringSelectOption ofDefault(String label, String value) {
        return new StringSelectOption(StringSelectComponentData.StringSelectOptionData.builder()
                .label(label)
                .value(value)
                .isDefault(true)
                .build());
    }

    /**
     * Get the label of this option
     *
     * @return The label of this option
     */
    public String getLabel() {
        return this.optionData.label();
    }

    /**
     * Create a new {@link StringSelectOption} with the provided label
     *
     * @param label The new label
     * @return A new {@link StringSelectOption} with the provided label
     */
    public StringSelectOption withLabel(String label) {
        return new StringSelectOption(StringSelectComponentData.StringSelectOptionData.builder()
                .from(this.optionData)
                .label(label)
                .build());
    }

    /**
     * Get the value of this option
     *
     * @return The value of this option
     */
    public String getValue() {
        return this.optionData.value();
    }

    /**
     * Create a new {@link StringSelectOption} with the provided value
     *
     * @param value The new value
     * @return A new {@link StringSelectOption} with the provided value
     */
    public StringSelectOption withValue(String value) {
        return new StringSelectOption(StringSelectComponentData.StringSelectOptionData.builder()
                .from(this.optionData)
                .value(value)
                .build());
    }

    /**
     * Check if this option is the default option
     *
     * @return True if this option is the default option, false otherwise
     */
    public boolean isDefault() {
        return this.optionData.isDefault().toOptional().orElse(false);
    }

    /**
     * Create a new {@link StringSelectOption} with the provided default value
     *
     * @param defaultValue The new default value
     * @return A new {@link StringSelectOption} with the provided default value
     */
    public StringSelectOption withDefault(boolean defaultValue) {
        return new StringSelectOption(StringSelectComponentData.StringSelectOptionData.builder()
                .from(this.optionData)
                .isDefault(defaultValue)
                .build());
    }

    /**
     * Get the emoji of this option
     *
     * @return An {@link Optional} containing the emoji of this option, or {@link Optional#empty()} if no emoji is set
     */
    public Optional<Emoji> getEmoji() {
        return this.optionData.emoji().toOptional().map(Emoji::of);
    }

    /**
     * Create a new {@link StringSelectOption} with the provided emoji
     *
     * @param emoji The emoji to set
     * @return A new {@link StringSelectOption} with the provided emoji
     */
    public StringSelectOption withEmoji(Emoji emoji) {
        return new StringSelectOption(StringSelectComponentData.StringSelectOptionData.builder()
                .from(this.optionData)
                .emoji(emoji.asEmojiData())
                .build());
    }

    /**
     * Get the description of this option
     *
     * @return An {@link Optional} containing the description of this option, or {@link Optional#empty()} if no
     * description is set
     */
    public Optional<String> getDescription() {
        return this.optionData.description().toOptional();
    }

    /**
     * Set the description of this option
     *
     * @param description The description to set
     * @return A new {@link StringSelectOption} with the provided description
     */
    public StringSelectOption withDescription(String description) {
        return new StringSelectOption(StringSelectComponentData.StringSelectOptionData.builder()
                .from(this.optionData)
                .description(description)
                .build());
    }

    /**
     * Create a new {@link StringSelectOption} from the provided data
     *
     * @param optionData The data to create the option from
     * @return A new {@link StringSelectOption} from the provided data
     */
    public static StringSelectOption fromData(StringSelectComponentData.StringSelectOptionData optionData) {
        return new StringSelectOption(optionData);
    }

    protected StringSelectOption(StringSelectComponentData.StringSelectOptionData optionData) {
        this.optionData = optionData;
    }

    /**
     * Get the raw data of this option
     *
     * @return The raw data of this option
     */
    public StringSelectComponentData.StringSelectOptionData getData() {
        return this.optionData;
    }

}
