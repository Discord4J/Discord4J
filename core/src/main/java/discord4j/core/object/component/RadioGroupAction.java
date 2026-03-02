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

public class RadioGroupAction extends ActionComponent implements ICanBeUsedInLabelComponent {

    /**
     * Creates a radio group.
     *
     * @param customId A developer-defined identifier for the radio group.
     * @param options The options that can be selected in the menu.
     * @return A radio group with the given data.
     */
    public static RadioGroupAction of(String customId, List<RadioGroupAction.Option> options) {
        Objects.requireNonNull(options);
        ImmutableComponentData.Builder builder = ComponentData.builder()
            .type(Type.RADIO_GROUP.getValue())
            .customId(customId);

        builder.options(options.stream().map(RadioGroupAction.Option::getData).collect(Collectors.toList()));

        return new RadioGroupAction(builder.build());
    }

    /**
     * Creates a radio group.
     *
     * @param customId A developer-defined identifier for the radio group.
     * @param options The options that can be selected in the menu.
     * @return A radio group with the given data.
     */
    public static RadioGroupAction of(int id, String customId, List<RadioGroupAction.Option> options) {
        Objects.requireNonNull(options);
        ImmutableComponentData.Builder builder = ComponentData.builder()
            .type(Type.RADIO_GROUP.getValue())
            .id(id)
            .customId(customId);

        builder.options(options.stream().map(RadioGroupAction.Option::getData).collect(Collectors.toList()));

        return new RadioGroupAction(builder.build());
    }

    RadioGroupAction(ComponentData data) {
        super(data);
    }

    protected RadioGroupAction of(ComponentData data) {
        return new RadioGroupAction(data);
    }

    /**
     * Gets the component value, if any. Can be present with an empty list if no value was selected.
     *
     * @return the component's value
     */
    public Optional<String> getValue() {
        return getData().value().toOptional();
    }

    /**
     * Creates a new radio group with the same data as this one, but depending on the value param it may be
     * required
     * or not.
     *
     * @param value True if the radio group should be required otherwise False.
     * @return A new possibly required radio group with the same data as this one.
     */
    public RadioGroupAction required(boolean value) {
        return of(ComponentData.builder().from(getData()).required(value).build());
    }

    /**
     * Creates a new radio group with the same data as this one, but disabled.
     *
     * @return A new disabled radio group with the same data as this one.
     */
    public RadioGroupAction disabled() {
        return this.disabled(true);
    }

    /**
     * Creates a new radio group menu with the same data as this one, but depending on the value param, it may be disabled or
     * not.
     *
     * @param value True if the radio group should be disabled otherwise False.
     * @return A new possibly disabled radio group with the same data as this one.
     */
    public RadioGroupAction disabled(boolean value) {
        return of(ComponentData.builder().from(getData()).disabled(value).build());
    }

    /**
     * An option displayed in a radio group.
     */
    public static class Option {

        /**
         * Creates a radio group option.
         *
         * @param label The user-facing name of the option
         * @param value A developer-defined identifier for the option.
         * @return A radio group option with the given data.
         */
        public static RadioGroupAction.Option of(String label, String value) {
            return of(label, value, false);
        }

        /**
         * Creates a default radio group option.
         * <p>
         * Default options are selected by default.
         *
         * @param label The user-facing name of the option
         * @param value A developer-defined identifier for the option.
         * @return A default radio group option with the given data.
         */
        public static RadioGroupAction.Option ofDefault(String label, String value) {
            return of(label, value, true);
        }

        private static RadioGroupAction.Option of(String label, String value, boolean isDefault) {
            return new RadioGroupAction.Option(SelectOptionData.builder()
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
        public RadioGroupAction.Option withDescription(String description) {
            return new RadioGroupAction.Option(SelectOptionData.builder().from(data).description(description).build());
        }

        /**
         * Creates a new possibly-default option with the same data as this one.
         *
         * @param isDefault Whether the option should be default.
         * @return A new option with the given default state.
         */
        public RadioGroupAction.Option withDefault(boolean isDefault) {
            return new RadioGroupAction.Option(SelectOptionData.builder().from(data).isDefault(isDefault).build());
        }

    }
}
