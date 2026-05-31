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
import discord4j.core.object.entity.channel.Channel;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.SelectDefaultValueData;
import discord4j.discordjson.json.component.ChannelSelectComponentData;
import discord4j.discordjson.json.component.ImmutableChannelSelectComponentData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represent a channel select menu component.
 *
 * @see <a href="https://docs.discord.com/developers/components/reference#channel-select">Discord documentation</a>
 */
public class ChannelSelectMenu extends BaseSelectMenu<ChannelSelectComponentData, ChannelSelectMenu> {

    public static final String DEFAULT_VALUE_DATA_TYPE = "channel";

    /**
     * Construct a new {@link ChannelSelectMenu} with the provided custom id
     *
     * @param customId The custom id of the new {@link ChannelSelectMenu}
     * @return A new {@link ChannelSelectMenu} with the provided custom id
     */
    public static ChannelSelectMenu of(String customId) {
        return new ChannelSelectMenu(ChannelSelectComponentData.builder()
                .customId(customId)
                .build());
    }

    /**
     * Construct a new {@link ChannelSelectMenu} with the provided component and custom ids
     *
     * @param componentId The component id of the new {@link ChannelSelectMenu}
     * @param customId    The custom id of the new {@link ChannelSelectMenu}
     * @return A new {@link ChannelSelectMenu} with the provided component and custom ids
     */
    public static ChannelSelectMenu of(int componentId, String customId) {
        return new ChannelSelectMenu(ChannelSelectComponentData.builder()
                .id(componentId)
                .customId(customId)
                .build());
    }

    /**
     * Construct a new {@link ChannelSelectMenu} with the provided component data
     *
     * @param data The component data
     * @return A new {@link ChannelSelectMenu} with the provided component data
     */
    public static ChannelSelectMenu of(ChannelSelectComponentData data) {
        return new ChannelSelectMenu(data);
    }

    protected ChannelSelectMenu(ChannelSelectComponentData data) {
        super(data);
    }

    //region ChannelSelectMenu-specific

    /**
     * Create a new {@link ChannelSelectMenu} with the added {@link Channel.Type}
     *
     * @param channelType The {@link Channel.Type} to add
     * @return A new {@link ChannelSelectMenu} with the added {@link Channel.Type}
     */
    public ChannelSelectMenu addChannelType(Channel.Type channelType) {
        return this.create(builder -> builder.addChannelType(channelType.getValue()));
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the added {@link Channel.Type}s
     *
     * @param channelTypes The {@link Channel.Type}s to add
     * @return A new {@link ChannelSelectMenu} with the added {@link Channel.Type}s
     */
    public ChannelSelectMenu addChannelTypes(Channel.Type... channelTypes) {
        return this.create(builder -> builder.addAllChannelTypes(
                Arrays.stream(channelTypes)
                        .map(Channel.Type::getValue)
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the added {@link Channel.Type}s
     *
     * @param channelTypes The {@link Channel.Type}s to add
     * @return A new {@link ChannelSelectMenu} with the added {@link Channel.Type}s
     */
    public ChannelSelectMenu addChannelTypes(List<Channel.Type> channelTypes) {
        return this.create(builder -> builder.addAllChannelTypes(
                channelTypes.stream()
                        .map(Channel.Type::getValue)
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the provided {@link Channel.Type}s
     *
     * @param channelTypes The {@link Channel.Type}s to set
     * @return A new {@link ChannelSelectMenu} with the set {@link Channel.Type}s
     */
    public ChannelSelectMenu withChannelTypes(Channel.Type... channelTypes) {
        return this.create(builder -> builder.channelTypes(
                Arrays.stream(channelTypes)
                        .map(Channel.Type::getValue)
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the provided {@link Channel.Type}s
     *
     * @param channelTypes The {@link Channel.Type}s to set
     * @return A new {@link ChannelSelectMenu} with the set {@link Channel.Type}s
     */
    public ChannelSelectMenu withChannelTypes(List<Channel.Type> channelTypes) {
        return this.create(builder -> builder.channelTypes(
                channelTypes.stream()
                        .map(Channel.Type::getValue)
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the added default value
     *
     * @param id The id of the channel to add as a default value
     * @return A new {@link ChannelSelectMenu} with the added default value
     */
    public ChannelSelectMenu addDefaultValue(Snowflake id) {
        return this.create(builder -> builder.addDefaultValue(
                SelectDefaultValueData.builder()
                        .id(Id.of(id.asString()))
                        .type(ChannelSelectMenu.DEFAULT_VALUE_DATA_TYPE)
                        .build())
        );
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the added default values
     *
     * @param ids The ids of the channels to add as default values
     * @return A new {@link ChannelSelectMenu} with the added default values
     */
    public ChannelSelectMenu addDefaultValues(Snowflake... ids) {
        return this.create(builder -> builder.addAllDefaultValues(
                Arrays.stream(ids)
                        .map(id -> {
                            return SelectDefaultValueData.builder()
                                    .id(Id.of(id.asString()))
                                    .type(ChannelSelectMenu.DEFAULT_VALUE_DATA_TYPE)
                                    .build();

                        })
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the added default values
     *
     * @param ids The ids of the channels to add as default values
     * @return A new {@link ChannelSelectMenu} with the added default values
     */
    public ChannelSelectMenu addDefaultValues(List<Snowflake> ids) {
        return this.create(builder -> builder.addAllDefaultValues(
                ids.stream()
                        .map(id -> {
                            return SelectDefaultValueData.builder()
                                    .id(Id.of(id.asString()))
                                    .type(ChannelSelectMenu.DEFAULT_VALUE_DATA_TYPE)
                                    .build();

                        })
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the provided default value
     *
     * @param id The id of the channel to set as a default value
     * @return A new {@link ChannelSelectMenu} with the provided default value
     */
    public ChannelSelectMenu withDefaultValue(Snowflake id) {
        return this.create(builder -> builder.defaultValues(
                SelectDefaultValueData.builder()
                        .id(Id.of(id.asString()))
                        .type(ChannelSelectMenu.DEFAULT_VALUE_DATA_TYPE)
                        .build())
        );
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the provided default values
     *
     * @param ids The ids of the channels to set as default values
     * @return A new {@link ChannelSelectMenu} with the provided default values
     */
    public ChannelSelectMenu withDefaultValues(Snowflake... ids) {
        return this.create(builder -> builder.defaultValues(
                Arrays.stream(ids)
                        .map(id -> {
                            return SelectDefaultValueData.builder()
                                    .id(Id.of(id.asString()))
                                    .type(ChannelSelectMenu.DEFAULT_VALUE_DATA_TYPE)
                                    .build();

                        })
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the provided default values
     *
     * @param ids The ids of the channels to set as default values
     * @return A new {@link ChannelSelectMenu} with the provided default values
     */
    public ChannelSelectMenu withDefaultValues(List<Snowflake> ids) {
        return this.create(builder -> builder.defaultValues(
                ids.stream()
                        .map(id -> {
                            return SelectDefaultValueData.builder()
                                    .id(Id.of(id.asString()))
                                    .type(ChannelSelectMenu.DEFAULT_VALUE_DATA_TYPE)
                                    .build();

                        })
                        .collect(Collectors.toList()))
        );
    }

    /**
     * Return the default values for this {@link ChannelSelectMenu}
     *
     * @return The {@link List} of default values for this {@link ChannelSelectMenu}
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
     * Create a new {@link ChannelSelectMenu} with the provided component id
     *
     * @param componentId The new component id
     * @return A new {@link ChannelSelectMenu} with the provided component id
     */
    public ChannelSelectMenu withComponentId(Integer componentId) {
        return this.create(builder -> builder.id(componentId));
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the provided custom id
     *
     * @param customId The new custom id
     * @return A new {@link ChannelSelectMenu} with the provided custom id
     */
    public ChannelSelectMenu withCustomId(String customId) {
        return this.create(builder -> builder.customId(customId));
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the provided disabled state
     *
     * @param disabled The new disabled state
     * @return A new {@link ChannelSelectMenu} with the provided disabled state
     */
    @Override
    public ChannelSelectMenu withDisabled(boolean disabled) {
        return this.create(builder -> builder.disabled(disabled));
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the provided placeholder
     *
     * @param placeholder The new placeholder
     * @return A new {@link ChannelSelectMenu} with the provided placeholder
     */
    @Override
    public ChannelSelectMenu withPlaceholder(String placeholder) {
        return this.create(builder -> builder.placeholder(placeholder));
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the provided min values count
     *
     * @param minValues The new min values count
     * @return A new {@link ChannelSelectMenu} with the provided min values count
     */
    @Override
    public ChannelSelectMenu withMinValues(int minValues) {
        return this.create(builder -> builder.minValues(minValues));
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the provided max values count
     *
     * @param maxValues The new max values count
     * @return A new {@link ChannelSelectMenu} with the provided max values count
     */
    @Override
    public ChannelSelectMenu withMaxValues(int maxValues) {
        return this.create(builder -> builder.maxValues(maxValues));
    }

    /**
     * Create a new {@link ChannelSelectMenu} with the provided required state
     *
     * @param required The new required state
     * @return A new {@link ChannelSelectMenu} with the provided required state
     */
    @Override
    public ChannelSelectMenu withRequired(boolean required) {
        return this.create(builder -> builder.required(required));
    }
    //endregion

    private ChannelSelectMenu create(Consumer<ImmutableChannelSelectComponentData.Builder> builderConsumer) {
        ImmutableChannelSelectComponentData.Builder dataBuilder =
                ChannelSelectComponentData.builder().from(this.getData());
        builderConsumer.accept(dataBuilder);
        return new ChannelSelectMenu(dataBuilder.build());
    }
}
