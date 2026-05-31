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
package discord4j.core.object.component.impl.selectmenu;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.SelectDefaultValueData;
import discord4j.discordjson.json.component.ImmutableUserSelectComponentData;
import discord4j.discordjson.json.component.UserSelectComponentData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represent a user select menu component.
 *
 * @see <a href="https://docs.discord.com/developers/components/reference#user-select">Discord documentation</a>
 */
public class UserSelectMenu extends BaseSelectMenu<UserSelectComponentData, UserSelectMenu> {

    public static final String DEFAULT_VALUE_DATA_TYPE = "user";

    /**
     * Construct a new {@link UserSelectMenu} with the provided custom id
     *
     * @param customId The custom id of the new {@link UserSelectMenu}
     * @return A new {@link UserSelectMenu} with the provided custom id
     */
    public static UserSelectMenu of(String customId) {
        return new UserSelectMenu(UserSelectComponentData.builder()
                .customId(customId)
                .build());
    }

    /**
     * Construct a new {@link UserSelectMenu} with the provided component and custom ids
     *
     * @param componentId The component id of the new {@link UserSelectMenu}
     * @param customId    The custom id of the new {@link UserSelectMenu}
     * @return A new {@link UserSelectMenu} with the provided component and custom ids
     */
    public static UserSelectMenu of(int componentId, String customId) {
        return new UserSelectMenu(UserSelectComponentData.builder()
                .id(componentId)
                .customId(customId)
                .build());
    }

    /**
     * Construct a new {@link UserSelectMenu} with the provided component data
     *
     * @param data The component data
     * @return A new {@link UserSelectMenu} with the provided component data
     */
    public static UserSelectMenu of(UserSelectComponentData data) {
        return new UserSelectMenu(data);
    }

    protected UserSelectMenu(UserSelectComponentData data) {
        super(data);
    }

    //region UserSelectMenu-specific

    /**
     * Create a new {@link UserSelectMenu} with the added default value
     *
     * @param id The id of the user to add as a default value
     * @return A new {@link UserSelectMenu} with the added default value
     */
    public UserSelectMenu addDefaultValue(Snowflake id) {
        return this.create(builder -> builder.addDefaultValue(
                SelectDefaultValueData.builder()
                        .id(Id.of(id.asString()))
                        .type(UserSelectMenu.DEFAULT_VALUE_DATA_TYPE)
                        .build())
        );
    }

    /**
     * Create a new {@link UserSelectMenu} with the added default values
     *
     * @param ids The ids of the users to add as default values
     * @return A new {@link UserSelectMenu} with the added default values
     */
    public UserSelectMenu addDefaultValues(Snowflake... ids) {
        return this.create(builder -> builder.addAllDefaultValues(
                Arrays.stream(ids)
                        .map(id -> {
                            return SelectDefaultValueData.builder()
                                    .id(Id.of(id.asString()))
                                    .type(UserSelectMenu.DEFAULT_VALUE_DATA_TYPE)
                                    .build();

                        })
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Create a new {@link UserSelectMenu} with the added default values
     *
     * @param ids The ids of the users to add as default values
     * @return A new {@link UserSelectMenu} with the added default values
     */
    public UserSelectMenu addDefaultValues(List<Snowflake> ids) {
        return this.create(builder -> builder.addAllDefaultValues(
                ids.stream()
                        .map(id -> {
                            return SelectDefaultValueData.builder()
                                    .id(Id.of(id.asString()))
                                    .type(UserSelectMenu.DEFAULT_VALUE_DATA_TYPE)
                                    .build();

                        })
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Create a new {@link UserSelectMenu} with the provided default values
     *
     * @param ids The ids of the users to set as default values
     * @return A new {@link UserSelectMenu} with the provided default values
     */
    public UserSelectMenu withDefaultValues(Snowflake... ids) {
        return this.create(builder -> builder.defaultValues(
                Arrays.stream(ids)
                        .map(id -> {
                            return SelectDefaultValueData.builder()
                                    .id(Id.of(id.asString()))
                                    .type(UserSelectMenu.DEFAULT_VALUE_DATA_TYPE)
                                    .build();

                        })
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Create a new {@link UserSelectMenu} with the provided default values
     *
     * @param ids The ids of the users to set as default values
     * @return A new {@link UserSelectMenu} with the provided default values
     */
    public UserSelectMenu withDefaultValues(List<Snowflake> ids) {
        return this.create(builder -> builder.defaultValues(
                ids.stream()
                        .map(id -> {
                            return SelectDefaultValueData.builder()
                                    .id(Id.of(id.asString()))
                                    .type(UserSelectMenu.DEFAULT_VALUE_DATA_TYPE)
                                    .build();

                        })
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Return the default values for this {@link UserSelectMenu}
     *
     * @return The {@link List} of default values for this {@link UserSelectMenu}
     */
    public List<Snowflake> getDefaultValues() {
        return this.getData()
                .defaultValues()
                .toOptional()
                .orElse(Collections.emptyList())
                .stream()
                .map(SelectDefaultValueData::id)
                .map(Snowflake::of)
                .collect(Collectors.toList());
    }
    //endregion

    //region Attributes

    /**
     * Create a new {@link UserSelectMenu} with the provided component id
     *
     * @param componentId The new component id
     * @return A new {@link UserSelectMenu} with the provided component id
     */
    public UserSelectMenu withComponentId(Integer componentId) {
        return this.create(builder -> builder.id(componentId));
    }

    /**
     * Create a new {@link UserSelectMenu} with the provided custom id
     *
     * @param customId The new custom id
     * @return A new {@link UserSelectMenu} with the provided custom id
     */
    public UserSelectMenu withCustomId(String customId) {
        return this.create(builder -> builder.customId(customId));
    }

    /**
     * Create a new {@link UserSelectMenu} with the provided disabled state
     *
     * @param disabled The new disabled state
     * @return A new {@link UserSelectMenu} with the provided disabled state
     */
    @Override
    public UserSelectMenu withDisabled(boolean disabled) {
        return this.create(builder -> builder.disabled(disabled));
    }

    /**
     * Create a new {@link UserSelectMenu} with the provided placeholder
     *
     * @param placeholder The new placeholder
     * @return A new {@link UserSelectMenu} with the provided placeholder
     */
    @Override
    public UserSelectMenu withPlaceholder(String placeholder) {
        return this.create(builder -> builder.placeholder(placeholder));
    }

    /**
     * Create a new {@link UserSelectMenu} with the provided min values count
     *
     * @param minValues The new min values count
     * @return A new {@link UserSelectMenu} with the provided min values count
     */
    @Override
    public UserSelectMenu withMinValues(int minValues) {
        return this.create(builder -> builder.minValues(minValues));
    }

    /**
     * Create a new {@link UserSelectMenu} with the provided max values count
     *
     * @param maxValues The new max values count
     * @return A new {@link UserSelectMenu} with the provided max values count
     */
    @Override
    public UserSelectMenu withMaxValues(int maxValues) {
        return this.create(builder -> builder.maxValues(maxValues));
    }

    /**
     * Create a new {@link UserSelectMenu} with the provided required state
     *
     * @param required The new required state
     * @return A new {@link UserSelectMenu} with the provided required state
     */
    @Override
    public UserSelectMenu withRequired(boolean required) {
        return this.create(builder -> builder.required(required));
    }
    //endregion

    private UserSelectMenu create(Consumer<ImmutableUserSelectComponentData.Builder> builderConsumer) {
        ImmutableUserSelectComponentData.Builder dataBuilder = UserSelectComponentData.builder().from(this.getData());
        builderConsumer.accept(dataBuilder);
        return new UserSelectMenu(dataBuilder.build());
    }
}
