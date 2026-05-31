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

import discord4j.core.object.component.impl.option.StringSelectOption;
import discord4j.discordjson.json.component.ImmutableStringSelectComponentData;
import discord4j.discordjson.json.component.StringSelectComponentData;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represent a string select menu component.
 *
 * @see <a href="https://docs.discord.com/developers/components/reference#string-select">Discord documentation</a>
 */
public class StringSelectMenu extends BaseSelectMenu<StringSelectComponentData, StringSelectMenu> {

    /**
     * Construct a new {@link StringSelectMenu} with the provided custom id
     *
     * @param customId The custom id of the new {@link StringSelectMenu}
     * @return A new {@link StringSelectMenu} with the provided custom id
     */
    public static StringSelectMenu of(String customId) {
        return new StringSelectMenu(StringSelectComponentData.builder()
                .customId(customId)
                .build());
    }

    /**
     * Construct a new {@link StringSelectMenu} with the provided component and custom ids
     *
     * @param componentId The component id of the new {@link StringSelectMenu}
     * @param customId    The custom id of the new {@link StringSelectMenu}
     * @return A new {@link StringSelectMenu} with the provided component and custom ids
     */
    public static StringSelectMenu of(int componentId, String customId) {
        return new StringSelectMenu(StringSelectComponentData.builder()
                .id(componentId)
                .customId(customId)
                .build());
    }

    /**
     * Construct a new {@link StringSelectMenu} with the provided component data
     *
     * @param data The component data
     * @return A new {@link StringSelectMenu} with the provided component data
     */
    public static StringSelectMenu of(StringSelectComponentData data) {
        return new StringSelectMenu(data);
    }

    protected StringSelectMenu(StringSelectComponentData data) {
        super(data);
    }

    //region StringSelectMenu-specific

    /**
     * Return the options for this {@link StringSelectMenu}
     *
     * @return The {@link List} of {@link StringSelectOption} for this {@link StringSelectMenu}
     */
    public List<StringSelectOption> getOptions() {
        return this.getData()
                .options()
                .stream()
                .map(StringSelectOption::fromData)
                .collect(Collectors.toList());
    }

    /**
     * Create a new {@link StringSelectMenu} with the added {@link StringSelectOption}
     *
     * @param option The {@link StringSelectOption} to add
     * @return A new {@link StringSelectMenu} with the added {@link StringSelectOption}
     */
    public StringSelectMenu addOption(StringSelectOption option) {
        return this.create(builder -> builder.addOption(option.getData()));
    }

    /**
     * Create a new {@link StringSelectMenu} with the added {@link StringSelectOption}
     *
     * @param options The {@link StringSelectOption} to add
     * @return A new {@link StringSelectMenu} with the added {@link StringSelectOption}
     */
    public StringSelectMenu addOptions(StringSelectOption... options) {
        return this.create(builder -> builder.addAllOptions(Arrays.stream(options).map(StringSelectOption::getData).collect(Collectors.toList())));
    }

    /**
     * Create a new {@link StringSelectMenu} with the added {@link StringSelectOption}
     *
     * @param options The {@link StringSelectOption} to add
     * @return A new {@link StringSelectMenu} with the added {@link StringSelectOption}
     */
    public StringSelectMenu addOptions(List<StringSelectOption> options) {
        return this.create(builder -> builder.addAllOptions(options.stream().map(StringSelectOption::getData).collect(Collectors.toList())));
    }

    /**
     * Create a new {@link StringSelectMenu} with the provided {@link StringSelectOption}
     *
     * @param options The {@link StringSelectOption} to set
     * @return A new {@link StringSelectMenu} with the set {@link StringSelectOption}
     */
    public StringSelectMenu withOptions(StringSelectOption... options) {
        return this.create(builder -> builder.options(Arrays.stream(options).map(StringSelectOption::getData).collect(Collectors.toList())));
    }

    /**
     * Create a new {@link StringSelectMenu} with the provided {@link StringSelectOption}
     *
     * @param options The {@link StringSelectOption} to set
     * @return A new {@link StringSelectMenu} with the set {@link StringSelectOption}
     */
    public StringSelectMenu withOptions(List<StringSelectOption> options) {
        return this.create(builder -> builder.options(options.stream().map(StringSelectOption::getData).collect(Collectors.toList())));
    }
    //endregion

    //region Attributes

    /**
     * Create a new {@link StringSelectMenu} with the provided component id
     *
     * @param componentId The new component id
     * @return A new {@link StringSelectMenu} with the provided component id
     */
    public StringSelectMenu withComponentId(Integer componentId) {
        return this.create(builder -> builder.id(componentId));
    }

    /**
     * Create a new {@link StringSelectMenu} with the provided custom id
     *
     * @param customId The new custom id
     * @return A new {@link StringSelectMenu} with the provided custom id
     */
    public StringSelectMenu withCustomId(String customId) {
        return this.create(builder -> builder.customId(customId));
    }

    /**
     * Create a new {@link StringSelectMenu} with the provided disabled state
     *
     * @param disabled The new disabled state
     * @return A new {@link StringSelectMenu} with the provided disabled state
     */
    @Override
    public StringSelectMenu withDisabled(boolean disabled) {
        return this.create(builder -> builder.disabled(disabled));
    }

    /**
     * Create a new {@link StringSelectMenu} with the provided placeholder
     *
     * @param placeholder The new placeholder
     * @return A new {@link StringSelectMenu} with the provided placeholder
     */
    @Override
    public StringSelectMenu withPlaceholder(String placeholder) {
        return this.create(builder -> builder.placeholder(placeholder));
    }

    /**
     * Create a new {@link StringSelectMenu} with the provided min values count
     *
     * @param minValues The new min values count
     * @return A new {@link StringSelectMenu} with the provided min values count
     */
    @Override
    public StringSelectMenu withMinValues(int minValues) {
        return this.create(builder -> builder.minValues(minValues));
    }

    /**
     * Create a new {@link StringSelectMenu} with the provided max values count
     *
     * @param maxValues The new max values count
     * @return A new {@link StringSelectMenu} with the provided max values count
     */
    @Override
    public StringSelectMenu withMaxValues(int maxValues) {
        return this.create(builder -> builder.maxValues(maxValues));
    }

    /**
     * Create a new {@link StringSelectMenu} with the provided required state
     *
     * @param required The new required state
     * @return A new {@link StringSelectMenu} with the provided required state
     */
    @Override
    public StringSelectMenu withRequired(boolean required) {
        return this.create(builder -> builder.required(required));
    }
    //endregion

    private StringSelectMenu create(Consumer<ImmutableStringSelectComponentData.Builder> builderConsumer) {
        ImmutableStringSelectComponentData.Builder dataBuilder =
                StringSelectComponentData.builder().from(this.getData());
        builderConsumer.accept(dataBuilder);
        return new StringSelectMenu(dataBuilder.build());
    }
}
