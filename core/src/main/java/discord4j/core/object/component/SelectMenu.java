package discord4j.core.object.component;

import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.ImmutableComponentData;
import discord4j.discordjson.json.SelectOptionData;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public class SelectMenu extends ActionComponent {

    public static SelectMenu of(String customId, Option... options) {
        return of(customId, Arrays.asList(options));
    }

    public static SelectMenu of(String customId, List<Option> options) {
        return new SelectMenu(ComponentData.builder()
                .type(Type.SELECT_MENU.getValue())
                .customId(customId)
                .options(options.stream().map(opt -> opt.data).collect(Collectors.toList()))
                .build());
    }

    public SelectMenu(ComponentData data) {
        super(data);
    }

    public String getCustomId() {
        return getData().customId().toOptional().orElseThrow(IllegalStateException::new);
    }

    public Optional<String> getPlaceholder() {
        return getData().customId().toOptional();
    }

    public OptionalInt getMinValues() {
        return getData().minValues().toOptional()
                .map(OptionalInt::of)
                .orElse(OptionalInt.empty());
    }

    public OptionalInt getMaxValues() {
        return getData().maxValues().toOptional()
                .map(OptionalInt::of)
                .orElse(OptionalInt.empty());
    }

    public List<Option> getOptions() {
        // should always be present for select menus
        List<SelectOptionData> options = getData().options().toOptional().orElseThrow(IllegalStateException::new);

        return options.stream()
                .map(Option::new)
                .collect(Collectors.toList());
    }

    public SelectMenu withPlaceholder(String placeholder) {
        return new SelectMenu(ComponentData.builder().from(getData()).placeholder(placeholder).build());
    }

    public SelectMenu withMinValues(int minValues) {
        return new SelectMenu(ComponentData.builder().from(getData()).minValues(minValues).build());
    }

    public SelectMenu withMaxValues(int maxValues) {
        return new SelectMenu(ComponentData.builder().from(getData()).maxValues(maxValues).build());
    }

    public static class Option {

        public static Option of(String label, String value) {
            return of(label, value, false);
        }

        public static Option ofDefault(String label, String value) {
            return of(label, value, true);
        }

        private static Option of(String label, String value, boolean isDefault) {
            return new Option(SelectOptionData.builder()
                    .label(label)
                    .value(value)
                    .isDefault(isDefault)
                    .build());
        }

        private final SelectOptionData data;

        Option(SelectOptionData data) {
            this.data = data;
        }

        public String getLabel() {
            return data.label();
        }

        public String getValue() {
            return data.value();
        }

        public Optional<String> getDescription() {
            return data.description().toOptional();
        }

        public Optional<ReactionEmoji> getEmoji() {
            return data.emoji().toOptional()
                    .map(ReactionEmoji::of);
        }

        public boolean isDefault() {
            return data.isDefault().toOptional().orElse(false);
        }

        public Option withDescription(String description) {
            return new Option(SelectOptionData.builder().from(data).description(description).build());
        }

        public Option withEmoji(ReactionEmoji emoji) {
            return new Option(SelectOptionData.builder().from(data).emoji(emoji.asEmojiData()).build());
        }
    }
}
