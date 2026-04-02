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
import discord4j.discordjson.json.SelectOptionData;
import discord4j.discordjson.json.component.ImmutableStringSelectComponentData;
import discord4j.discordjson.json.component.StringSelectComponentData;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A string select menu.
 *
 * @see <a href="https://discord.com/developers/docs/components/reference#string-select">String Select Menu</a>
 */
public class StringSelectMenu extends SelectMenu<StringSelectComponentData> implements ICanBeUsedInLabelComponent {

    /**
     * Creates a string select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @param options  The options that can be selected in the menu.
     * @return A select menu with the given data.
     */
    public static StringSelectMenu of(String customId, Option... options) {
        return of(customId, Arrays.asList(options));
    }

    /**
     * Creates a string select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @param options  The options that can be selected in the menu.
     * @return A select menu with the given data.
     */
    public static StringSelectMenu of(String customId, List<Option> options) {
        Objects.requireNonNull(options);
        ImmutableStringSelectComponentData.Builder builder = StringSelectComponentData.builder()
                .type(Type.SELECT_MENU_STRING.getValue())
                .customId(customId);

        builder.options(options.stream().map(Option::getData).collect(Collectors.toList()));

        return new StringSelectMenu(builder.build());
    }

    protected StringSelectMenu of(StringSelectComponentData data) {
        return new StringSelectMenu(data);
    }

    StringSelectMenu(StringSelectComponentData data) {
        super(data);
    }

    /**
     * Gets the select menu values, if any. Can be present with an empty list if no value was selected.
     *
     * @return the select menu's value
     */
    public Optional<List<String>> getValues() {
        return getData().values().toOptional();
    }

    /**
     * Creates a new string select menu with the same data as this one, but required.
     *
     * @return A new required select menu with the same data as this one.
     * @apiNote This value is ignored in messages
     */
    public StringSelectMenu required() {
        return required(true);
    }

    /**
     * Creates a new string select menu with the same data as this one, but depending on the value param it may be
     * required or not.
     *
     * @param value True if the select menu should be required otherwise False.
     * @return A new possibly required select menu with the same data as this one.
     * @apiNote This value is ignored in messages
     */
    public StringSelectMenu required(boolean value) {
        return of(StringSelectComponentData.builder().from(getData()).required(value).build());
    }

    /**
     * Creates a new select menu with the same data as this one, but disabled.
     *
     * @return A new disabled select menu with the same data as this one.
     * @apiNote The disabled field on String Selects is not currently allowed in modals and will trigger an error if
     * used
     */
    public StringSelectMenu disabled() {
        return this.disabled(true);
    }

    /**
     * Creates a new select menu with the same data as this one, but depending on the value param it may be disabled or
     * not.
     *
     * @param value True if the select menu should be disabled otherwise False.
     * @return A new possibly disabled select menu with the same data as this one.
     * @apiNote The disabled field on String Selects is not currently allowed in modals and will trigger an error if
     * used
     */
    public StringSelectMenu disabled(boolean value) {
        return of(StringSelectComponentData.builder().from(getData()).disabled(value).build());
    }

    /**
     * Creates a new select menu with the same data as this one, but with the given placeholder text.
     *
     * @param placeholder The new placeholder text.
     * @return A new select menu with the given placeholder text.
     */
    public StringSelectMenu withPlaceholder(String placeholder) {
        return of(StringSelectComponentData.builder().from(getData()).placeholder(placeholder).build());
    }

    /**
     * Creates a new select menu with the same data as this one, but with the given minimum values.
     *
     * @param minValues The new minimum values.
     * @return A new select menu with the given minimum values.
     */
    public StringSelectMenu withMinValues(int minValues) {
        return of(StringSelectComponentData.builder().from(getData()).minValues(minValues).build());
    }

    /**
     * Creates a new select menu with the same data as this one, but with the given maximum values.
     *
     * @param maxValues The new maximum values.
     * @return A new select menu with the given maximum values.
     */
    public StringSelectMenu withMaxValues(int maxValues) {
        return of(StringSelectComponentData.builder().from(getData()).maxValues(maxValues).build());
    }

    /**
     * An option displayed in a select menu.
     */
    public static class Option {

        /**
         * Creates a select menu option.
         *
         * @param label The user-facing name of the option
         * @param value A developer-defined identifier for the option.
         * @return A select menu option with the given data.
         */
        public static Option of(String label, String value) {
            return of(label, value, false);
        }

        /**
         * Creates a default select menu option.
         * <p>
         * Default options are selected by default.
         *
         * @param label The user-facing name of the option
         * @param value A developer-defined identifier for the option.
         * @return A default select menu option with the given data.
         */
        public static Option ofDefault(String label, String value) {
            return of(label, value, true);
        }

        private static Option of(String label, String value, boolean isDefault) {
            return new Option(SelectOptionData.builder()
                    .label(label)
                    .value(value)
                    .isDefault(isDefault)
                    .build());
        }

        private final SelectOptionData data;

        Option(SelectOptionData data) {
            this.data = data;
        }

        public SelectOptionData getData() {
            return data;
        }

        /**
         * Gets the option's label.
         *
         * @return The option's label.
         */
        public String getLabel() {
            return data.label();
        }

        /**
         * Gets the option's value.
         *
         * @return The option's value.
         */
        public String getValue() {
            return data.value();
        }

        /**
         * Gets the option's description.
         *
         * @return The option's description.
         */
        public Optional<String> getDescription() {
            return data.description().toOptional();
        }

        /**
         * Gets the option's emoji.
         *
         * @return The option's emoji.
         */
        public Optional<Emoji> getEmoji() {
            return data.emoji().toOptional()
                    .map(Emoji::of);
        }

        /**
         * Gets whether the option is default.
         *
         * @return Whether the option is default.
         */
        public boolean isDefault() {
            return data.isDefault().toOptional().orElse(false);
        }

        /**
         * Creates a new option with the same data as this one, but with the given description.
         *
         * @param description The additional description of the option.
         * @return A new option with the given description.
         */
        public Option withDescription(String description) {
            return new Option(SelectOptionData.builder().from(data).description(description).build());
        }

        /**
         * Creates a new option with the same data as this one, but with the given emoji.
         *
         * @param emoji An emoji to display with the option.
         * @return A new option with the given emoji.
         */
        public Option withEmoji(Emoji emoji) {
            return new Option(SelectOptionData.builder().from(data).emoji(emoji.asEmojiData()).build());
        }

        /**
         * Creates a new possibly-default option with the same data as this one.
         *
         * @param isDefault Whether the option should be default.
         * @return A new option with the given default state.
         */
        public Option withDefault(boolean isDefault) {
            return new Option(SelectOptionData.builder().from(data).isDefault(isDefault).build());
        }
    }

}
