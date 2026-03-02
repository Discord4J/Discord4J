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
import discord4j.discordjson.json.SelectOptionData;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CheckboxGroupAction extends ActionComponent implements ICanBeUsedInLabelComponent {

    /**
     * Creates a checkbox group.
     *
     * @param customId A developer-defined identifier for the checkbox group.
     * @param options The options that can be selected in the menu.
     * @return A checkbox group with the given data.
     */
    public static CheckboxGroupAction of(String customId, List<CheckboxGroupAction.Option> options) {
        Objects.requireNonNull(options);
        ImmutableComponentData.Builder builder = ComponentData.builder()
            .type(Type.CHECKBOX_GROUP.getValue())
            .customId(customId);

        builder.options(options.stream().map(CheckboxGroupAction.Option::getData).collect(Collectors.toList()));

        return new CheckboxGroupAction(builder.build());
    }

    /**
     * Creates a checkbox group.
     *
     * @param customId A developer-defined identifier for the checkbox group.
     * @param options The options that can be selected in the menu.
     * @return A checkbox group with the given data.
     */
    public static CheckboxGroupAction of(int id, String customId, List<CheckboxGroupAction.Option> options) {
        Objects.requireNonNull(options);
        ImmutableComponentData.Builder builder = ComponentData.builder()
            .type(Type.CHECKBOX_GROUP.getValue())
            .id(id)
            .customId(customId);

        builder.options(options.stream().map(CheckboxGroupAction.Option::getData).collect(Collectors.toList()));

        return new CheckboxGroupAction(builder.build());
    }

    CheckboxGroupAction(ComponentData data) {
        super(data);
    }

    protected CheckboxGroupAction of(ComponentData data) {
        return new CheckboxGroupAction(data);
    }

    /**
     * Gets the component values, if any. Can be present with an empty list if no value was selected.
     *
     * @return the component's value
     */
    public Optional<List<String>> getValues() {
        return getData().values().toOptional();
    }

    /**
     * Creates a new checkbox group with the same data as this one, but depending on the value param it may be
     * required
     * or not.
     *
     * @param value True if the checkbox group should be required otherwise False.
     * @return A new possibly required checkbox group with the same data as this one.
     */
    public CheckboxGroupAction required(boolean value) {
        return of(ComponentData.builder().from(getData()).required(value).build());
    }

    /**
     * Creates a new checkbox group with the same data as this one, but disabled.
     *
     * @return A new disabled checkbox group with the same data as this one.
     */
    public CheckboxGroupAction disabled() {
        return this.disabled(true);
    }

    /**
     * Creates a new checkbox group with the same data as this one, but depending on the value param, it may be disabled or
     * not.
     *
     * @param value True if the checkbox group should be disabled otherwise False.
     * @return A new possibly disabled checkbox group with the same data as this one.
     */
    public CheckboxGroupAction disabled(boolean value) {
        return of(ComponentData.builder().from(getData()).disabled(value).build());
    }

    /**
     * Creates a new checkbox group with the same data as this one, but with the given minimum values.
     *
     * @param minValues The new minimum values (0-10)
     * @return A new checkbox group with the given minimum values.
     */
    public CheckboxGroupAction withMinValues(int minValues) {
        return of(ComponentData.builder().from(this.getData()).minValues(minValues).build());
    }

    /**
     * Creates a new checkbox group with the same data as this one, but with the given maximum values.
     *
     * @param maxValues The new maximum values (1-10)
     * @return A new checkbox group with the given maximum values.
     */
    public CheckboxGroupAction withMaxValues(int maxValues) {
        return of(ComponentData.builder().from(this.getData()).maxValues(maxValues).build());
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
        public static CheckboxGroupAction.Option of(String label, String value) {
            return of(label, value, false);
        }

        /**
         * Creates a default checkbox group option.
         * <p>
         * Default options are selected by default.
         *
         * @param label The user-facing name of the option
         * @param value A developer-defined identifier for the option.
         * @return A default checkbox group option with the given data.
         */
        public static CheckboxGroupAction.Option ofDefault(String label, String value) {
            return of(label, value, true);
        }

        private static CheckboxGroupAction.Option of(String label, String value, boolean isDefault) {
            return new CheckboxGroupAction.Option(SelectOptionData.builder()
                .label(label)
                .value(value)
                .isDefault(isDefault)
                .build());
        }

        private final SelectOptionData data;

        Option(SelectOptionData data) {
            this.data = data;
        }

        public SelectOptionData getData() {
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
        public CheckboxGroupAction.Option withDescription(String description) {
            return new CheckboxGroupAction.Option(SelectOptionData.builder().from(data).description(description).build());
        }

        /**
         * Creates a new possibly-default option with the same data as this one.
         *
         * @param isDefault Whether the option should be default.
         * @return A new option with the given default state.
         */
        public CheckboxGroupAction.Option withDefault(boolean isDefault) {
            return new CheckboxGroupAction.Option(SelectOptionData.builder().from(data).isDefault(isDefault).build());
        }

    }
}
