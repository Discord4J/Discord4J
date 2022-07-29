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

import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.SelectOptionData;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A message select menu.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/message-components#select-menus">Select Menus</a>
 */
public class SelectMenu extends ActionComponent {

    /**
     * Creates a select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @param options The options that can be selected in the menu.
     * @return A select menu with the given data.
     */
    public static SelectMenu of(String customId, Option... options) {
        return of(customId, Arrays.asList(options));
    }

    /**
     * Creates a select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @param options The options that can be selected in the menu.
     * @return A select menu with the given data.
     */
    public static SelectMenu of(String customId, /*~~>*/List<Option> options) {
        return new SelectMenu(ComponentData.builder()
                .type(Type.SELECT_MENU.getValue())
                .customId(customId)
                .options(options.stream().map(opt -> opt.data).collect(Collectors.toList()))
                .build());
    }

    SelectMenu(ComponentData data) {
        super(data);
    }

    /**
     * Gets the select menu's custom id.
     *
     * @return The select menu's custom id.
     */
    public String getCustomId() {
        return getData().customId().toOptional().orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the select menu values, if any. Can be present with an empty list if no value was selected.
     *
     * @return the select menu's value
     */
    public Optional</*~~>*/List<String>> getValues() {
        return getData().values().toOptional();
    }

    /**
     * Gets the text displayed if no option is selected.
     *
     * @return The text displayed if no option is selected.
     */
    public Optional<String> getPlaceholder() {
        return getData().placeholder().toOptional();
    }

    /**
     * Gets the minimum number of options that must be chosen.
     *
     * @return The minimum number of options that must be chosen.
     */
    public int getMinValues() {
        return getData().minValues().toOptional().orElse(1);
    }

    /**
     * Gets the maximum number of options that must be chosen.
     *
     * @return The maximum number of options that must be chosen.
     */
    public int getMaxValues() {
        return getData().maxValues().toOptional().orElse(1);
    }

    /**
     * Gets the options that can be selected in the menu.
     *
     * @return The options that can be selected in the menu.
     */
    public /*~~>*/List<Option> getOptions() {
        // should always be present for select menus
        List<SelectOptionData> options = getData().options().toOptional().orElseThrow(IllegalStateException::new);

        return options.stream()
                .map(Option::new)
                .collect(Collectors.toList());
    }

    /**
     * Gets whether the select menu is disabled (i.e., the user is prevented from selecting any options).
     *
     * @return Whether the select menu is disabled.
     */
    public boolean isDisabled() {
        return getData().disabled().toOptional().orElse(false);
    }

    /**
     * Creates a new select menu with the same data as this one, but disabled.
     *
     * @return A new disabled select menu with the same data as this one.
     */
    public SelectMenu disabled() {
        return disabled(true);
    }

    /**
     * Creates a new select menu with the same data as this one, but depending on the value param it may be disabled or
     * not.
     *
     * @param value True if the select menu should be disabled otherwise False.
     * @return A new possibly disabled select menu with the same data as this one.
     */
    public SelectMenu disabled(boolean value) {
        return new SelectMenu(ComponentData.builder().from(getData()).disabled(value).build());
    }

    /**
     * Creates a new select menu with the same data as this one, but with the given placeholder text.
     *
     * @param placeholder The new placeholder text.
     * @return A new select menu with the given placeholder text.
     */
    public SelectMenu withPlaceholder(String placeholder) {
        return new SelectMenu(ComponentData.builder().from(getData()).placeholder(placeholder).build());
    }

    /**
     * Creates a new select menu with the same data as this one, but with the given minimum values.
     *
     * @param minValues The new minimum values.
     * @return A new select menu with the given minimum values.
     */
    public SelectMenu withMinValues(int minValues) {
        return new SelectMenu(ComponentData.builder().from(getData()).minValues(minValues).build());
    }

    /**
     * Creates a new select menu with the same data as this one, but with the given maximum values.
     *
     * @param maxValues The new maximum values.
     * @return A new select menu with the given maximum values.
     */
    public SelectMenu withMaxValues(int maxValues) {
        return new SelectMenu(ComponentData.builder().from(getData()).maxValues(maxValues).build());
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
        public Optional<ReactionEmoji> getEmoji() {
            return data.emoji().toOptional()
                    .map(ReactionEmoji::of);
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
        public Option withEmoji(ReactionEmoji emoji) {
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
