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
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.ImmutableComponentData;
import discord4j.discordjson.json.SelectDefaultValueData;
import discord4j.discordjson.json.component.ChannelSelectComponentData;
import discord4j.discordjson.json.component.MentionableSelectComponentData;
import discord4j.discordjson.json.component.RoleSelectComponentData;
import discord4j.discordjson.json.component.StringSelectComponentData;
import discord4j.discordjson.json.component.UserSelectComponentData;
import discord4j.discordjson.json.component.type.SelectComponentData;
import discord4j.discordjson.json.component.type.SelectComponentDataBase;
import discord4j.discordjson.possible.Possible;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A select menu.
 *
 * @see <a href="https://discord.com/developers/docs/components/reference#string-select">String Select Menu</a>
 * @see <a href="https://discord.com/developers/docs/components/reference#user-select">User Select Menu</a>
 * @see <a href="https://discord.com/developers/docs/components/reference#role-select">Role Select Menu</a>
 * @see <a href="https://discord.com/developers/docs/components/reference#mentionable-select">Mentionable Select Menu</a>
 * @see <a href="https://discord.com/developers/docs/components/reference#channel-select">Channel Select Menu</a>
 */
public abstract class SelectMenu<D extends SelectComponentDataBase> extends ActionComponent<D> implements ICanBeUsedInLabelComponent {

    /**
     * Returns a set of acceptable channel types the user may select.
     * Only applies to {@link MessageComponent.Type#SELECT_MENU_CHANNEL} type menus, if empty, no restriction on channel types is placed.
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
     * if {@link #getType() type} of select menu is not {@link MessageComponent.Type#SELECT_MENU_STRING}
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
     * @apiNote The disabled field on Selects is not currently allowed in modals and will trigger an error if
     * used
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
     * @apiNote The disabled field on Selects is not currently allowed in modals and will trigger an error if
     * used
     */
    public SelectMenu disabled(boolean value) {
        return new SelectMenu(ComponentData.builder().from(getData()).disabled(value).build());
    }

    /**
     * Gets whether the select menu is required (i.e., the user is prevented from selecting any options).
     *
     * @return Whether the select menu is disabled.
     * @apiNote This value is ignored in messages
     */
    public boolean isRequired() {
        return getData().required().toOptional().orElse(true);
    }

    /**
     * Creates a new select menu with the same data as this one, but depending on the value param it may be
     * required
     * or not.
     *
     * @param value True if the select menu should be required otherwise False.
     * @return A new possibly required select menu with the same data as this one.
     * @apiNote This value is ignored in messages
     */
    public SelectMenu required(boolean value) {
        return new SelectMenu(ComponentData.builder().from(getData()).required(value).build());
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
