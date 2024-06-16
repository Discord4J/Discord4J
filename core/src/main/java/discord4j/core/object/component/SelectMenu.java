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

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.ImmutableComponentData;
import discord4j.discordjson.json.SelectDefaultValueData;
import discord4j.discordjson.json.SelectOptionData;
import reactor.util.annotation.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A message select menu.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/message-components#select-menus">Select Menus</a>
 */
public class SelectMenu extends ActionComponent {

    /**
     * Creates a string select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @param options The options that can be selected in the menu.
     * @return A select menu with the given data.
     */
    public static SelectMenu of(String customId, Option... options) {
        return of(Type.SELECT_MENU, customId, Arrays.asList(options), null, null);
    }

    /**
     * Creates a string select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @param options The options that can be selected in the menu.
     * @return A select menu with the given data.
     */
    public static SelectMenu of(String customId, List<Option> options) {
        Objects.requireNonNull(options);
        return of(Type.SELECT_MENU, customId, options, null, null);
    }

    /**
     * Creates a role select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @return A select menu with the given data.
     */
    public static SelectMenu ofRole(String customId) {
        return of(Type.SELECT_MENU_ROLE, customId, null, null, null);
    }

    /**
     * Creates a role select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @param defaultValues The default values for auto-populated select menus.
     * @return A select menu with the given data.
     */
    public static SelectMenu ofRole(String customId, List<DefaultValue> defaultValues) {
        return of(Type.SELECT_MENU_ROLE, customId, null, null, defaultValues);
    }

    /**
     * Creates a user select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @return A select menu with the given data.
     */
    public static SelectMenu ofUser(String customId) {
        return of(Type.SELECT_MENU_USER, customId, null, null, null);
    }

    /**
     * Creates a user select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @param defaultValues The default values for auto-populated select menus.
     * @return A select menu with the given data.
     */
    public static SelectMenu ofUser(String customId, List<DefaultValue> defaultValues) {
        return of(Type.SELECT_MENU_USER, customId, null, null, defaultValues);
    }

    /**
     * Creates a mentionable select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @return A select menu with the given data.
     */
    public static SelectMenu ofMentionable(String customId) {
        return of(Type.SELECT_MENU_MENTIONABLE, customId, null, null, null);
    }

    /**
     * Creates a mentionable select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @param defaultValues The default values for auto-populated select menus.
     * @return A select menu with the given data.
     */
    public static SelectMenu ofMentionable(String customId, List<DefaultValue> defaultValues) {
        return of(Type.SELECT_MENU_MENTIONABLE, customId, null, null, defaultValues);
    }

    /**
     * Creates a channel select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @param channelTypes The allowed channel types.
     * @return A select menu with the given data.
     */
    public static SelectMenu ofChannel(String customId, Channel.Type... channelTypes) {
        return of(Type.SELECT_MENU_CHANNEL, customId, null, Arrays.asList(channelTypes), null);
    }

    /**
     * Creates a channel select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @param defaultValues The default values for auto-populated select menus.
     * @param channelTypes The allowed channel types.
     * @return A select menu with the given data.
     */
    public static SelectMenu ofChannel(String customId, List<DefaultValue> defaultValues, Channel.Type... channelTypes) {
        return of(Type.SELECT_MENU_CHANNEL, customId, null, Arrays.asList(channelTypes), defaultValues);
    }

    /**
     * Creates a channel select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @param channelTypes The allowed channel types.
     * @return A select menu with the given data.
     */
    public static SelectMenu ofChannel(String customId, List<Channel.Type> channelTypes) {
        return of(Type.SELECT_MENU_CHANNEL, customId, null, channelTypes, null);
    }

    /**
     * Creates a channel select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @param defaultValues The default values for auto-populated select menus.
     * @param channelTypes The allowed channel types.
     * @return A select menu with the given data.
     */
    public static SelectMenu ofChannel(String customId, List<DefaultValue> defaultValues, List<Channel.Type> channelTypes) {
        return of(Type.SELECT_MENU_CHANNEL, customId, null, channelTypes, defaultValues);
    }

    private static SelectMenu of(Type type, String customId, @Nullable List<Option> options, @Nullable List<Channel.Type> channelTypes, @Nullable List<DefaultValue> defaultValues) {
        ImmutableComponentData.Builder builder = ComponentData.builder()
                .type(type.getValue())
                .customId(customId);

        if (options != null) {
            builder.options(options.stream().map(opt -> opt.data).collect(Collectors.toList()));
        }

        if (channelTypes != null) {
            builder.channelTypes(channelTypes.stream()
                    .map(Channel.Type::getValue)
                    .collect(Collectors.toList()));
        }

        if (defaultValues != null) {
            builder.defaultValues(defaultValues.stream()
                    .map(DefaultValue::getData)
                    .collect(Collectors.toList()));
        }

        return new SelectMenu(builder.build());
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
    public Optional<List<String>> getValues() {
        return getData().values().toOptional();
    }

    /**
     * Returns a set of acceptable channel types the user may select.
     * Only applies to {@link Type#SELECT_MENU_CHANNEL} type menus, if empty, no restriction on channel types is placed.
     *
     * @return A set of channel types a user may select. Empty set means no restriction is applied.
     */
    public Set<Channel.Type> getAllowedChannelTypes() {
        return getData().channelTypes().toOptional()
                .orElse(Collections.emptyList())
                .stream()
                .map(Channel.Type::of)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Channel.Type.class)));
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
     * Gets the options that can be selected in the menu. List can be empty
     * if {@link #getType() type} of select menu is not {@link MessageComponent.Type#SELECT_MENU}
     *
     * @return The options that can be selected in the menu.
     */
    public List<Option> getOptions() {
        return getData().options().toOptional()
                .orElse(Collections.emptyList()).stream()
                .map(Option::new)
                .collect(Collectors.toList());
    }

    /**
     * Gets the default values for auto-populated select menus. List can be empty
     * if {@link #getType() type} of select menu is not {@link MessageComponent.Type#SELECT_MENU_USER}
     * nor {@link MessageComponent.Type#SELECT_MENU_ROLE} nor {@link MessageComponent.Type#SELECT_MENU_MENTIONABLE}
     *
     * @return The default values for auto-populated select menus.
     */
    public List<DefaultValue> getDefaultValues() {
        return getData().defaultValues().toOptional()
            .orElse(Collections.emptyList()).stream()
            .map(DefaultValue::new)
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
     * Creates a new select menu with the same data as this one, but with the given allowed channel types.
     *
     * @param types The new allowed channel types.
     * @return A new select menu with the given allowed channel types.
     */
    public SelectMenu withAllowedChannelTypes(Iterable<Channel.Type> types) {
        if (getType() != Type.SELECT_MENU_CHANNEL) {
            throw new IllegalArgumentException("Select menu with type " + getType() + " can't have channel types restriction");
        }

        return new SelectMenu(ComponentData.builder().from(getData())
                .channelTypes(StreamSupport.stream(types.spliterator(), false)
                        .map(Channel.Type::getValue)
                        .collect(Collectors.toList()))
                .build());
    }

    /**
     * Creates a new select menu with the same data as this one, but with the given allowed channel types.
     *
     * @param types The new allowed channel types.
     * @return A new select menu with the given allowed channel types.
     */
    public SelectMenu withAllowedChannelTypes(Channel.Type... types) {
        if (getType() != Type.SELECT_MENU_CHANNEL) {
            throw new IllegalArgumentException("Select menu with type " + getType() + " can't have channel types restriction");
        }

        return new SelectMenu(ComponentData.builder().from(getData())
                .channelTypes(Arrays.stream(types)
                        .map(Channel.Type::getValue)
                        .collect(Collectors.toList()))
                .build());
    }

    /**
     * Creates a new select menu with the same data as this one, but with the given default values.
     *
     * @param values The new default values.
     * @return A new select menu with the given default values.
     */
    public SelectMenu withDefaultValues(Iterable<DefaultValue> values) {
        if (getType() != Type.SELECT_MENU_USER && getType() != Type.SELECT_MENU_ROLE && getType() != Type.SELECT_MENU_MENTIONABLE) {
            throw new IllegalArgumentException("Select menu with type " + getType() + " can't have default values");
        }

        List<SelectDefaultValueData> defaultValues = StreamSupport.stream(values.spliterator(), false)
            .map(DefaultValue::getData)
            .collect(Collectors.toList());

        // Validate default values
        if (defaultValues.size() > this.getMaxValues()) {
            throw new IllegalArgumentException("Default values count (" + defaultValues.size() + ") can't be greater than max values count (" + this.getMaxValues() + ")!");
        }

        if (defaultValues.size() < this.getMinValues()) {
            throw new IllegalArgumentException("Default values count (" + defaultValues.size() + ") can't be less than min values count (" + this.getMinValues() + ")!");
        }

        for (SelectDefaultValueData defaultValue : defaultValues) {
            if (getType() == Type.SELECT_MENU_USER && !defaultValue.type().equals(DefaultValue.Type.USER.value)) {
                throw new IllegalArgumentException("Default value type must be USER for user select menu");
            }

            if (getType() == Type.SELECT_MENU_ROLE && !defaultValue.type().equals(DefaultValue.Type.ROLE.value)) {
                throw new IllegalArgumentException("Default value type must be ROLE for role select menu");
            }

            if (getType() == Type.SELECT_MENU_MENTIONABLE && !defaultValue.type().equals(DefaultValue.Type.USER.value) && !defaultValue.type().equals(DefaultValue.Type.ROLE.value)) {
                throw new IllegalArgumentException("Default value type must be USER or ROLE for mentionable select menu");
            }

            if (getType() == Type.SELECT_MENU_CHANNEL && !defaultValue.type().equals(DefaultValue.Type.CHANNEL.value)) {
                throw new IllegalArgumentException("Default value type must be CHANNEL for channel select menu");
            }
        }

        return new SelectMenu(ComponentData.builder().from(getData())
                .defaultValues(defaultValues)
                .build());
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

    /**
     * A default value for auto populated select menus.
     */
    public static class DefaultValue {

        /**
         * The type of default value.
         */
        public enum Type {

            /** An unknown type */
            UNKNOWN(""),

            /** A user */
            USER("user"),

            /** A role */
            ROLE("role"),

            /** A channel */
            CHANNEL("channel");

            public static Type of(String value) {
                for (Type type : values()) {
                    if (type.getValue().equals(value)) {
                        return type;
                    }
                }

                return UNKNOWN;
            }

            /** The underlying value as represented by Discord */
            private final String value;

            /**
             * Constructs a {@code SelectMenu.DefaultValue.Type}.
             *
             * @param value The underlying value as represented by Discord.
             */
            Type(String value) {
                this.value = value;
            }

            /**
             * Gets the value of the type.
             *
             * @return The value of the type.
             */
            public String getValue() {
                return value;
            }
        }

        /**
         * Creates a select menu option.
         *
         * @param snowflake The id of the default value.
         * @param type The type of the default value.
         * @return A default value with the given data.
         */
        public static DefaultValue of(Snowflake snowflake, Type type) {
            return new DefaultValue(SelectDefaultValueData.builder()
                .id(snowflake.asString())
                .type(type.getValue())
                .build());
        }

        /** The underlying data */
        private final SelectDefaultValueData data;

        /**
         * Constructs a {@code SelectMenu.DefaultValue}.
         *
         * @param data The underlying data.
         */
        DefaultValue(SelectDefaultValueData data) {
            this.data = data;
        }

        /**
         * Gets the id of the default value.
         *
         * @return The id of the default value.
         */
        public Snowflake getId() {
            return Snowflake.of(data.id());
        }

        /**
         * Gets the type of the default value.
         *
         * @return The type of the default value.
         */
        public Type getType() {
            return Type.of(data.type());
        }

        /**
         * Gets the internal data.
         *
         * @return The internal data.
         */
        public SelectDefaultValueData getData() {
            return data;
        }
    }
}
