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
import discord4j.discordjson.json.component.ImmutableMentionableSelectComponentData;
import discord4j.discordjson.json.component.MentionableSelectComponentData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represent a mentionable select menu component.
 *
 * @see <a href="https://docs.discord.com/developers/components/reference#mentionable-select">Discord documentation</a>
 */
public class MentionableSelectMenu extends BaseSelectMenu<MentionableSelectComponentData, MentionableSelectMenu> {

    /**
     * Construct a new {@link MentionableSelectMenu} with the provided custom id
     *
     * @param customId The custom id of the new {@link MentionableSelectMenu}
     * @return A new {@link MentionableSelectMenu} with the provided custom id
     */
    public static MentionableSelectMenu of(String customId) {
        return new MentionableSelectMenu(MentionableSelectComponentData.builder()
                .customId(customId)
                .build());
    }

    /**
     * Construct a new {@link MentionableSelectMenu} with the provided component and custom ids
     *
     * @param componentId The component id of the new {@link MentionableSelectMenu}
     * @param customId    The custom id of the new {@link MentionableSelectMenu}
     * @return A new {@link MentionableSelectMenu} with the provided component and custom ids
     */
    public static MentionableSelectMenu of(int componentId, String customId) {
        return new MentionableSelectMenu(MentionableSelectComponentData.builder()
                .id(componentId)
                .customId(customId)
                .build());
    }

    /**
     * Construct a new {@link MentionableSelectMenu} with the provided component data
     *
     * @param data The component data
     * @return A new {@link MentionableSelectMenu} with the provided component data
     */
    public static MentionableSelectMenu of(MentionableSelectComponentData data) {
        return new MentionableSelectMenu(data);
    }

    protected MentionableSelectMenu(MentionableSelectComponentData data) {
        super(data);
    }

    //region MentionableSelectMenu-specific

    /**
     * Create a new {@link MentionableSelectMenu} with the added default user value
     *
     * @param id The id of the user to add as a default value
     * @return A new {@link MentionableSelectMenu} with the added default user value
     */
    public MentionableSelectMenu addDefaultUserValue(Snowflake id) {
        return this.addDefaultValue(UserSelectMenu.DEFAULT_VALUE_DATA_TYPE, id);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the added default user values
     *
     * @param ids The ids of the users to add as default values
     * @return A new {@link MentionableSelectMenu} with the added default user values
     */
    public MentionableSelectMenu addDefaultUserValues(Snowflake... ids) {
        return this.addDefaultValues(UserSelectMenu.DEFAULT_VALUE_DATA_TYPE, ids);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the added default user values
     *
     * @param ids The ids of the users to add as default values
     * @return A new {@link MentionableSelectMenu} with the added default user values
     */
    public MentionableSelectMenu addDefaultUserValues(List<Snowflake> ids) {
        return this.addDefaultValues(UserSelectMenu.DEFAULT_VALUE_DATA_TYPE, ids);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the added default role value
     *
     * @param id The id of the role to add as a default value
     * @return A new {@link MentionableSelectMenu} with the added default role value
     */
    public MentionableSelectMenu addDefaultRoleValue(Snowflake id) {
        return this.addDefaultValue(RoleSelectMenu.DEFAULT_VALUE_DATA_TYPE, id);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the added default role values
     *
     * @param ids The ids of the roles to add as default values
     * @return A new {@link MentionableSelectMenu} with the added default role values
     */
    public MentionableSelectMenu addDefaultRoleValues(Snowflake... ids) {
        return this.addDefaultValues(RoleSelectMenu.DEFAULT_VALUE_DATA_TYPE, ids);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the added default role values
     *
     * @param ids The ids of the roles to add as default values
     * @return A new {@link MentionableSelectMenu} with the added default role values
     */
    public MentionableSelectMenu addDefaultRoleValues(List<Snowflake> ids) {
        return this.addDefaultValues(RoleSelectMenu.DEFAULT_VALUE_DATA_TYPE, ids);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the added default channel value
     *
     * @param id The id of the channel to add as a default value
     * @return A new {@link MentionableSelectMenu} with the added default channel value
     */
    public MentionableSelectMenu addDefaultChannelValue(Snowflake id) {
        return this.addDefaultValue(ChannelSelectMenu.DEFAULT_VALUE_DATA_TYPE, id);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the added default channel values
     *
     * @param ids The ids of the channels to add as default values
     * @return A new {@link MentionableSelectMenu} with the added default channel values
     */
    public MentionableSelectMenu addDefaultChannelValues(Snowflake... ids) {
        return this.addDefaultValues(ChannelSelectMenu.DEFAULT_VALUE_DATA_TYPE, ids);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the added default channel values
     *
     * @param ids The ids of the channels to add as default values
     * @return A new {@link MentionableSelectMenu} with the added default channel values
     */
    public MentionableSelectMenu addDefaultChannelValues(List<Snowflake> ids) {
        return this.addDefaultValues(ChannelSelectMenu.DEFAULT_VALUE_DATA_TYPE, ids);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the added default value
     *
     * @param type The type of the default value
     * @param id   The id of the default value
     * @return A new {@link MentionableSelectMenu} with the added default value
     */
    public MentionableSelectMenu addDefaultValue(String type, Snowflake id) {
        return this.create(builder -> builder.addDefaultValue(
                SelectDefaultValueData.builder()
                        .id(Id.of(id.asString()))
                        .type(type)
                        .build())
        );
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the added default values
     *
     * @param type The type of the default values
     * @param ids  The ids of the default values
     * @return A new {@link MentionableSelectMenu} with the added default values
     */
    public MentionableSelectMenu addDefaultValues(String type, Snowflake... ids) {
        return this.create(builder -> builder.addAllDefaultValues(
                Arrays.stream(ids)
                        .map(id -> {
                            return SelectDefaultValueData.builder()
                                    .id(Id.of(id.asString()))
                                    .type(type)
                                    .build();

                        })
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the added default values
     *
     * @param type The type of the default values
     * @param ids  The ids of the default values
     * @return A new {@link MentionableSelectMenu} with the added default values
     */
    public MentionableSelectMenu addDefaultValues(String type, List<Snowflake> ids) {
        return this.create(builder -> builder.addAllDefaultValues(
                ids.stream()
                        .map(id -> {
                            return SelectDefaultValueData.builder()
                                    .id(Id.of(id.asString()))
                                    .type(type)
                                    .build();

                        })
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided default user value
     *
     * @param id The id of the user to set as a default value
     * @return A new {@link MentionableSelectMenu} with the provided default user value
     */
    public MentionableSelectMenu withDefaultUserValue(Snowflake id) {
        return this.withDefaultValue(UserSelectMenu.DEFAULT_VALUE_DATA_TYPE, id);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided default user values
     *
     * @param ids The ids of the users to set as default values
     * @return A new {@link MentionableSelectMenu} with the provided default user values
     */
    public MentionableSelectMenu withDefaultUserValues(Snowflake... ids) {
        return this.withDefaultValues(UserSelectMenu.DEFAULT_VALUE_DATA_TYPE, ids);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided default user values
     *
     * @param ids The ids of the users to set as default values
     * @return A new {@link MentionableSelectMenu} with the provided default user values
     */
    public MentionableSelectMenu withDefaultUserValues(List<Snowflake> ids) {
        return this.withDefaultValues(UserSelectMenu.DEFAULT_VALUE_DATA_TYPE, ids);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided default role value
     *
     * @param id The id of the role to set as a default value
     * @return A new {@link MentionableSelectMenu} with the provided default role value
     */
    public MentionableSelectMenu withDefaultRoleValue(Snowflake id) {
        return this.withDefaultValue(RoleSelectMenu.DEFAULT_VALUE_DATA_TYPE, id);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided default role values
     *
     * @param ids The ids of the roles to set as default values
     * @return A new {@link MentionableSelectMenu} with the provided default role values
     */
    public MentionableSelectMenu withDefaultRoleValues(Snowflake... ids) {
        return this.withDefaultValues(RoleSelectMenu.DEFAULT_VALUE_DATA_TYPE, ids);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided default role values
     *
     * @param ids The ids of the roles to set as default values
     * @return A new {@link MentionableSelectMenu} with the provided default role values
     */
    public MentionableSelectMenu withDefaultRoleValues(List<Snowflake> ids) {
        return this.withDefaultValues(RoleSelectMenu.DEFAULT_VALUE_DATA_TYPE, ids);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided default channel value
     *
     * @param id The id of the channel to set as a default value
     * @return A new {@link MentionableSelectMenu} with the provided default channel value
     */
    public MentionableSelectMenu withDefaultChannelValue(Snowflake id) {
        return this.withDefaultValue(ChannelSelectMenu.DEFAULT_VALUE_DATA_TYPE, id);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided default channel values
     *
     * @param ids The ids of the channels to set as default values
     * @return A new {@link MentionableSelectMenu} with the provided default channel values
     */
    public MentionableSelectMenu withDefaultChannelValues(Snowflake... ids) {
        return this.withDefaultValues(ChannelSelectMenu.DEFAULT_VALUE_DATA_TYPE, ids);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided default channel values
     *
     * @param ids The ids of the channels to set as default values
     * @return A new {@link MentionableSelectMenu} with the provided default channel values
     */
    public MentionableSelectMenu withDefaultChannelValues(List<Snowflake> ids) {
        return this.withDefaultValues(ChannelSelectMenu.DEFAULT_VALUE_DATA_TYPE, ids);
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided default value
     *
     * @param type The type of the default value
     * @param id   The id of the default value
     * @return A new {@link MentionableSelectMenu} with the provided default value
     */
    public MentionableSelectMenu withDefaultValue(String type, Snowflake id) {
        return this.create(builder -> builder.defaultValues(
                SelectDefaultValueData.builder()
                        .id(Id.of(id.asString()))
                        .type(type)
                        .build())
        );
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided default values
     *
     * @param type The type of the default values
     * @param ids  The ids of the default values
     * @return A new {@link MentionableSelectMenu} with the provided default values
     */
    public MentionableSelectMenu withDefaultValues(String type, Snowflake... ids) {
        return this.create(builder -> builder.defaultValues(
                Arrays.stream(ids)
                        .map(id -> {
                            return SelectDefaultValueData.builder()
                                    .id(Id.of(id.asString()))
                                    .type(type)
                                    .build();

                        })
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided default values
     *
     * @param type The type of the default values
     * @param ids  The ids of the default values
     * @return A new {@link MentionableSelectMenu} with the provided default values
     */
    public MentionableSelectMenu withDefaultValues(String type, List<Snowflake> ids) {
        return this.create(builder -> builder.defaultValues(
                ids.stream()
                        .map(id -> {
                            return SelectDefaultValueData.builder()
                                    .id(Id.of(id.asString()))
                                    .type(type)
                                    .build();

                        })
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Return the default values for this {@link MentionableSelectMenu}
     *
     * @return The {@link List} of default values for this {@link MentionableSelectMenu}
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
     * Create a new {@link MentionableSelectMenu} with the provided component id
     *
     * @param componentId The new component id
     * @return A new {@link MentionableSelectMenu} with the provided component id
     */
    public MentionableSelectMenu withComponentId(Integer componentId) {
        return this.create(builder -> builder.id(componentId));
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided custom id
     *
     * @param customId The new custom id
     * @return A new {@link MentionableSelectMenu} with the provided custom id
     */
    public MentionableSelectMenu withCustomId(String customId) {
        return this.create(builder -> builder.customId(customId));
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided disabled state
     *
     * @param disabled The new disabled state
     * @return A new {@link MentionableSelectMenu} with the provided disabled state
     */
    @Override
    public MentionableSelectMenu withDisabled(boolean disabled) {
        return this.create(builder -> builder.disabled(disabled));
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided placeholder
     *
     * @param placeholder The new placeholder
     * @return A new {@link MentionableSelectMenu} with the provided placeholder
     */
    @Override
    public MentionableSelectMenu withPlaceholder(String placeholder) {
        return this.create(builder -> builder.placeholder(placeholder));
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided min values count
     *
     * @param minValues The new min values count
     * @return A new {@link MentionableSelectMenu} with the provided min values count
     */
    @Override
    public MentionableSelectMenu withMinValues(int minValues) {
        return this.create(builder -> builder.minValues(minValues));
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided max values count
     *
     * @param maxValues The new max values count
     * @return A new {@link MentionableSelectMenu} with the provided max values count
     */
    @Override
    public MentionableSelectMenu withMaxValues(int maxValues) {
        return this.create(builder -> builder.maxValues(maxValues));
    }

    /**
     * Create a new {@link MentionableSelectMenu} with the provided required state
     *
     * @param required The new required state
     * @return A new {@link MentionableSelectMenu} with the provided required state
     */
    @Override
    public MentionableSelectMenu withRequired(boolean required) {
        return this.create(builder -> builder.required(required));
    }
    //endregion

    private MentionableSelectMenu create(Consumer<ImmutableMentionableSelectComponentData.Builder> builderConsumer) {
        ImmutableMentionableSelectComponentData.Builder dataBuilder =
                MentionableSelectComponentData.builder().from(this.getData());
        builderConsumer.accept(dataBuilder);
        return new MentionableSelectMenu(dataBuilder.build());
    }
}
