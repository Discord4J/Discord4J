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

import discord4j.discordjson.json.component.CheckboxGroupComponentData;
import discord4j.discordjson.json.component.ImmutableCheckboxGroupComponentData;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CheckboxGroup extends MessageComponent<CheckboxGroupComponentData> implements ICanBeUsedInLabelComponent {

    /**
     * Creates a checkbox group.
     *
     * @param customId A developer-defined identifier for the checkbox group.
     * @param options  The options that can be selected in the checkbox group.
     * @return A checkbox group with the given data.
     */
    public static CheckboxGroup of(String customId, List<CheckboxGroup.Option> options) {
        Objects.requireNonNull(options);
        ImmutableCheckboxGroupComponentData.Builder builder = CheckboxGroupComponentData.builder()
                .type(Type.CHECKBOX_GROUP.getValue())
                .customId(customId);

        builder.options(options.stream().map(CheckboxGroup.Option::getData).collect(Collectors.toList()));

        return new CheckboxGroup(builder.build());
    }

    /**
     * Creates a checkbox group.
     *
     * @param customId A developer-defined identifier for the checkbox group.
     * @param options  The options that can be selected in the checkbox group.
     * @return A checkbox group with the given data.
     */
    public static CheckboxGroup of(int id, String customId, List<CheckboxGroup.Option> options) {
        Objects.requireNonNull(options);
        ImmutableCheckboxGroupComponentData.Builder builder = CheckboxGroupComponentData.builder()
                .type(Type.CHECKBOX_GROUP.getValue())
                .id(id)
                .customId(customId);

        builder.options(options.stream().map(CheckboxGroup.Option::getData).collect(Collectors.toList()));

        return new CheckboxGroup(builder.build());
    }

    CheckboxGroup(CheckboxGroupComponentData data) {
        super(data);
    }

    protected CheckboxGroup of(CheckboxGroupComponentData data) {
        return new CheckboxGroup(data);
    }

    /**
     * Gets the component values, if any. Can be present with an empty list if no value was selected.
     *
     * @return the component's value
     */
    public List<String> getValues() {
        return getData().values().toOptional().orElse(Collections.emptyList());
    }

    /**
     * Creates a new checkbox group with the same data as this one, but depending on the value param it may be
     * required or not.
     *
     * @param value True if the checkbox group should be required otherwise False.
     * @return A new possibly required checkbox group with the same data as this one.
     */
    public CheckboxGroup required(boolean value) {
        return of(CheckboxGroupComponentData.builder().from(getData()).required(value).build());
    }

    /**
     * Creates a new checkbox group with the same data as this one, but with the given minimum values.
     *
     * @param minValues The new minimum values (0-10)
     * @return A new checkbox group with the given minimum values.
     */
    public CheckboxGroup withMinValues(int minValues) {
        return of(CheckboxGroupComponentData.builder().from(this.getData()).minValues(minValues).build());
    }

    /**
     * Creates a new checkbox group with the same data as this one, but with the given maximum values.
     *
     * @param maxValues The new maximum values (1-10)
     * @return A new checkbox group with the given maximum values.
     */
    public CheckboxGroup withMaxValues(int maxValues) {
        return of(CheckboxGroupComponentData.builder().from(this.getData()).maxValues(maxValues).build());
    }

    /**
     * An option displayed in a checkbox group.
     */
    public static class Option {

        /**
         * Creates a checkbox group option.
         *
         * @param label The user-facing name of the option
         * @param value A developer-defined identifier for the option.
         * @return A checkbox group option with the given data.
         */
        public static CheckboxGroup.Option of(String label, String value) {
            return of(label, value, false);
        }

        /**
         * Creates a selected-by-default checkbox group option.
         * <p>
         * Default options are selected by default.
         *
         * @param label The user-facing name of the option
         * @param value A developer-defined identifier for the option.
         * @return A selected-by-default checkbox group option with the given data.
         */
        public static CheckboxGroup.Option ofSelectedByDefault(String label, String value) {
            return of(label, value, true);
        }

        private static CheckboxGroup.Option of(String label, String value, boolean isSelectedByDefault) {
            return new CheckboxGroup.Option(CheckboxGroupComponentData.CheckboxGroupOptionData.builder()
                    .label(label)
                    .value(value)
                    .isDefault(isSelectedByDefault)
                    .build());
        }

        private final CheckboxGroupComponentData.CheckboxGroupOptionData data;

        Option(CheckboxGroupComponentData.CheckboxGroupOptionData data) {
            this.data = data;
        }

        public CheckboxGroupComponentData.CheckboxGroupOptionData getData() {
            return data;
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
         * Gets whether the option is selected by default.
         *
         * @return Whether the option is selected by default.
         */
        public boolean isSelectedByDefault() {
            return data.isDefault().toOptional().orElse(false);
        }

        /**
         * Creates a new option with the same data as this one, but with the given description.
         *
         * @param description The additional description of the option.
         * @return A new option with the given description.
         */
        public CheckboxGroup.Option withDescription(String description) {
            return new CheckboxGroup.Option(CheckboxGroupComponentData.CheckboxGroupOptionData.builder().from(data).description(description).build());
        }

        /**
         * Creates a new possibly-selected-by-default option with the same data as this one.
         *
         * @param isSelectedByDefault Whether the option should be selected by default.
         * @return A new option with the given default state.
         */
        public CheckboxGroup.Option withSelectedByDefault(boolean isSelectedByDefault) {
            return new CheckboxGroup.Option(CheckboxGroupComponentData.CheckboxGroupOptionData.builder().from(data).isDefault(isSelectedByDefault).build());
        }

    }
}
