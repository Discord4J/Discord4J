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

import java.util.List;
import java.util.Optional;

public class CheckboxAction extends ActionComponent implements ICanBeUsedInLabelComponent {

    /**
     * Creates a checkbox.
     *
     * @param customId A developer-defined identifier for the checkbox.
     * @return A checkbox with the given data.
     */
    public static CheckboxAction of(String customId) {
        ImmutableComponentData.Builder builder = ComponentData.builder()
            .type(Type.CHECKBOX.getValue())
            .customId(customId);

        return new CheckboxAction(builder.build());
    }

    /**
     * Creates a checkbox.
     *
     * @param customId A developer-defined identifier for the checkbox.
     * @return A checkbox with the given data.
     */
    public static CheckboxAction of(int id, String customId) {
        ImmutableComponentData.Builder builder = ComponentData.builder()
            .type(Type.CHECKBOX.getValue())
            .id(id)
            .customId(customId);

        return new CheckboxAction(builder.build());
    }

    CheckboxAction(ComponentData data) {
        super(data);
    }

    protected CheckboxAction of(ComponentData data) {
        return new CheckboxAction(data);
    }

    /**
     * Gets the component value, if any. Can be present with an empty list if no value was selected.
     *
     * @return the component's value
     */
    public Optional<Boolean> getValue() {
        return getData().value().toOptional().map(Boolean::parseBoolean);
    }

}
