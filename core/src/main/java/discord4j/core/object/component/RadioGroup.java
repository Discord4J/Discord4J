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

import discord4j.discordjson.json.component.ImmutableRadioGroupComponentData;
import discord4j.discordjson.json.component.RadioGroupComponentData;
import discord4j.discordjson.possible.Possible;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class RadioGroup extends MessageComponent<RadioGroupComponentData> implements ICanBeUsedInLabelComponent {

    /**
     * Creates a radio group.
     *
     * @param customId A developer-defined identifier for the radio group.
     * @param options  The options that can be selected in the radio group.
     * @return A radio group with the given data.
     */
    public static RadioGroup of(String customId, List<RadioGroup.Option> options) {
        Objects.requireNonNull(options);
        ImmutableRadioGroupComponentData.Builder builder = RadioGroupComponentData.builder()
                .type(Type.RADIO_GROUP.getValue())
                .customId(customId);

        builder.options(options.stream().map(RadioGroup.Option::getData).collect(Collectors.toList()));

        return new RadioGroup(builder.build());
    }

    /**
     * Creates a radio group.
     *
     * @param customId A developer-defined identifier for the radio group.
     * @param options  The options that can be selected in the radio group.
     * @return A radio group with the given data.
     */
    public static RadioGroup of(int id, String customId, List<RadioGroup.Option> options) {
        Objects.requireNonNull(options);
        ImmutableRadioGroupComponentData.Builder builder = RadioGroupComponentData.builder()
                .type(Type.RADIO_GROUP.getValue())
                .id(id)
                .customId(customId);

        builder.options(options.stream().map(RadioGroup.Option::getData).collect(Collectors.toList()));

        return new RadioGroup(builder.build());
    }

    RadioGroup(RadioGroupComponentData data) {
        super(data);
    }

    protected RadioGroup of(RadioGroupComponentData data) {
        return new RadioGroup(data);
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
     * required or not.
     *
     * @param value True if the radio group should be required otherwise False.
     * @return A new possibly required radio group with the same data as this one.
     */
    public RadioGroup required(boolean value) {
        return of(RadioGroupComponentData.builder().from(getData()).required(value).build());
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
        public static RadioGroup.Option of(String label, String value) {
            return of(label, value, null, false);
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
        public static RadioGroup.Option ofSelectedByDefault(String label, String value) {
            return of(label, value, null, true);
        }

        /**
         * Creates a radio group option.
         *
         * @param label       The user-facing name of the option
         * @param value       A developer-defined identifier for the option.
         * @param description An optional description for the option
         * @return A radio group option with the given data.
         */
        public static RadioGroup.Option of(String label, String value, @Nullable String description) {
            return of(label, value, description, false);
        }

        /**
         * Creates a default radio group option.
         * <p>
         * Default options are selected by default.
         *
         * @param label       The user-facing name of the option
         * @param value       A developer-defined identifier for the option.
         * @param description An optional description for the option
         * @return A default radio group option with the given data.
         */
        public static RadioGroup.Option ofSelectedByDefault(String label, String value, @Nullable String description) {
            return of(label, value, description, true);
        }

        private static RadioGroup.Option of(String label, String value, @Nullable String description,
                                            boolean isSelectedByDefault) {
            return new RadioGroup.Option(RadioGroupComponentData.RadioGroupOptionData.builder()
                    .label(label)
                    .value(value)
                    .description(Possible.ofNullable(description))
                    .isDefault(isSelectedByDefault)
                    .build());
        }

        private final RadioGroupComponentData.RadioGroupOptionData data;

        Option(RadioGroupComponentData.RadioGroupOptionData data) {
            this.data = data;
        }

        public RadioGroupComponentData.RadioGroupOptionData getData() {
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
         * Creates a new option with the same data as this one, but with the given label.
         *
         * @param label The new label for the option.
         * @return A new option with the given label.
         */
        public RadioGroup.Option withLabel(String label) {
            return new RadioGroup.Option(RadioGroupComponentData.RadioGroupOptionData.builder().from(data).label(label).build());
        }

        /**
         * Creates a new option with the same data as this one, but with the given value.
         *
         * @param value The new value for the option.
         * @return A new option with the given value.
         */
        public RadioGroup.Option withValue(String value) {
            return new RadioGroup.Option(RadioGroupComponentData.RadioGroupOptionData.builder().from(data).value(value).build());
        }

        /**
         * Creates a new option with the same data as this one, but with the given description.
         *
         * @param description The additional description of the option.
         * @return A new option with the given description.
         */
        public RadioGroup.Option withDescription(String description) {
            return new RadioGroup.Option(RadioGroupComponentData.RadioGroupOptionData.builder().from(data).description(description).build());
        }

        /**
         * Creates a new possibly selected-by-default option with the same data as this one.
         *
         * @param isSelectedByDefault Whether the option should be selected by default.
         * @return A new option with the given selected-by-default state.
         */
        public RadioGroup.Option withSelectedByDefault(boolean isSelectedByDefault) {
            return new RadioGroup.Option(RadioGroupComponentData.RadioGroupOptionData.builder().from(data).isDefault(isSelectedByDefault).build());
        }

    }
}
