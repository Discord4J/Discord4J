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

import discord4j.discordjson.json.component.CheckboxComponentData;
import discord4j.discordjson.json.component.ImmutableCheckboxComponentData;

public class Checkbox extends MessageComponent<CheckboxComponentData> implements ICanBeUsedInLabelComponent {

    /**
     * Creates a checkbox.
     *
     * @param customId A developer-defined identifier for the checkbox.
     * @return A checkbox with the given data.
     */
    public static Checkbox of(String customId) {
        ImmutableCheckboxComponentData.Builder builder = CheckboxComponentData.builder()
            .type(Type.CHECKBOX.getValue())
            .customId(customId);

        return new Checkbox(builder.build());
    }

    /**
     * Creates a checkbox.
     *
     * @param customId A developer-defined identifier for the checkbox.
     * @return A checkbox with the given data.
     */
    public static Checkbox of(int id, String customId) {
        ImmutableCheckboxComponentData.Builder builder = CheckboxComponentData.builder()
            .type(Type.CHECKBOX.getValue())
            .id(id)
            .customId(customId);

        return new Checkbox(builder.build());
    }

    /**
     * Creates a checkbox.
     *
     * @param customId A developer-defined identifier for the checkbox.
     * @param value The initial value of the checkbox.
     * @return A checkbox with the given data.
     */
    public static Checkbox of(String customId, boolean value) {
        ImmutableCheckboxComponentData.Builder builder = CheckboxComponentData.builder()
                .type(Type.CHECKBOX.getValue())
                .customId(customId)
                .isDefault(value);

        return new Checkbox(builder.build());
    }

    /**
     * Creates a checkbox.
     *
     * @param customId A developer-defined identifier for the checkbox.
     * @param value The initial value of the checkbox.
     * @return A checkbox with the given data.
     */
    public static Checkbox of(int id, String customId, boolean value) {
        ImmutableCheckboxComponentData.Builder builder = CheckboxComponentData.builder()
                .type(Type.CHECKBOX.getValue())
                .id(id)
                .customId(customId)
                .isDefault(value);

        return new Checkbox(builder.build());
    }

    Checkbox(CheckboxComponentData data) {
        super(data);
    }

    protected Checkbox of(CheckboxComponentData data) {
        return new Checkbox(data);
    }

    /**
     * Gets the checkbox's value. Defaults to false if not present.
     *
     * @return the checkbox's value
     */
    public boolean getValue() {
        return getData()
                .value()
                .toOptional()
                .map(Boolean::parseBoolean)
                .orElse(false);
    }

}
