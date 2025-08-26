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

import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.ImmutableComponentData;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A string select menu.
 *
 * @see <a href="https://discord.com/developers/docs/components/reference#string-select">String Select Menu</a>
 */
public class StringSelectMenu extends SelectMenu implements ICanBeUsedInLabelComponent {

    /**
     * Creates a string select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @param options The options that can be selected in the menu.
     * @return A select menu with the given data.
     */
    public static StringSelectMenu of(String customId, Option... options) {
        return of(customId, Arrays.asList(options));
    }

    /**
     * Creates a string select menu.
     *
     * @param customId A developer-defined identifier for the select menu.
     * @param options The options that can be selected in the menu.
     * @return A select menu with the given data.
     */
    public static StringSelectMenu of(String customId, List<Option> options) {
        Objects.requireNonNull(options);
        ImmutableComponentData.Builder builder = ComponentData.builder()
            .type(Type.SELECT_MENU_STRING.getValue())
            .customId(customId);

        builder.options(options.stream().map(Option::getData).collect(Collectors.toList()));

        return new StringSelectMenu(builder.build());
    }

    protected StringSelectMenu of(ComponentData data) {
        return new StringSelectMenu(data);
    }

    StringSelectMenu(ComponentData data) {
        super(data);
    }

    /**
     * Creates a new string select menu with the same data as this one, but depending on the value param it may be
     * required
     * or not.
     *
     * @param value True if the select menu should be required otherwise False.
     * @return A new possibly required select menu with the same data as this one.
     * @apiNote This value is ignored in messages
     */
    public StringSelectMenu required(boolean value) {
        return of(ComponentData.builder().from(getData()).required(value).build());
    }

    /**
     * Creates a new select menu with the same data as this one, but disabled.
     *
     * @return A new disabled select menu with the same data as this one.
     * @apiNote The disabled field on String Selects is not currently allowed in modals and will trigger an error if
     * used
     */
    public SelectMenu disabled() {
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
        return of(ComponentData.builder().from(getData()).disabled(value).build());
    }

    /**
     * Creates a new select menu with the same data as this one, but with the given placeholder text.
     *
     * @param placeholder The new placeholder text.
     * @return A new select menu with the given placeholder text.
     */
    public StringSelectMenu withPlaceholder(String placeholder) {
        return of(ComponentData.builder().from(getData()).placeholder(placeholder).build());
    }

    /**
     * Creates a new select menu with the same data as this one, but with the given minimum values.
     *
     * @param minValues The new minimum values.
     * @return A new select menu with the given minimum values.
     */
    public StringSelectMenu withMinValues(int minValues) {
        return of(ComponentData.builder().from(getData()).minValues(minValues).build());
    }

    /**
     * Creates a new select menu with the same data as this one, but with the given maximum values.
     *
     * @param maxValues The new maximum values.
     * @return A new select menu with the given maximum values.
     */
    public StringSelectMenu withMaxValues(int maxValues) {
        return of(ComponentData.builder().from(getData()).maxValues(maxValues).build());
    }
}
