package discord4j.core.object.automod;

import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.AutoModTriggerMetaData;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An Auto Moderation Trigger MetaData
 *
 * @see <a href="https://discord.com/developers/docs/resources/auto-moderation#auto-moderation-rule-object-trigger-metadata">Auto Moderation Trigger MetaData</a>
 */
public class AutoModRuleTriggerMetaData {

    /**
     * The gateway associated to this object.
     */
    private final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final AutoModTriggerMetaData data;

    public AutoModRuleTriggerMetaData(final GatewayDiscordClient gateway, final AutoModTriggerMetaData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    public GatewayDiscordClient getClient() {
        return gateway;
    }

    public AutoModTriggerMetaData getData() {
        return data;
    }

    /**
     * Gets substrings which will be searched for in content.
     *
     * @return a list of words.
     */
    public List<String> getKeywordFilter() {
        return data.keywordFilter().toOptional()
                .orElse(Collections.emptyList());
    }

    /**
     * Gets substrings which will be exempt from triggering the preset trigger type.
     *
     * @return a list of words.
     */
    public List<String> getAllowedFilter() {
        return data.allowList().toOptional()
                .orElse(Collections.emptyList());
    }

    /**
     * Gets Regular expression patterns which will be matched against content.
     *
     * @return a list of regex.
     */
    public List<Pattern> getRegexPatterns() {
        return data.regexPatterns().toOptional()
                .map(listRegexPatterns -> listRegexPatterns.stream().map(Pattern::compile).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * Gets the total number of unique role and user mentions allowed per message if set.
     *
     * @return the mention limit if available.
     */
    public Optional<Integer> getMentionLimit() {
        return data.mentionTotalLimit().toOptional();
    }

    /**
     * Gets the internally pre-defined wordsets which will be searched for in content.
     *
     * @return a EnumSet with all the presets in the class.
     */
    public EnumSet<Preset> getPresets() {
        EnumSet<Preset> presets = EnumSet.noneOf(Preset.class);
        if (data.presets().isAbsent()) {
            return presets;
        }
        presets.addAll(data.presets().toOptional().map(presetValues -> presetValues.stream().map(Preset::of)).orElse(Stream.empty()).collect(Collectors.toList()));
        return presets;
    }

    /**
     * Represents a preset in the AutoModMetaData.
     */
    public enum Preset {
        /**
         * Unknown type.
         */
        UNKNOWN(-1),

        /**
         * Words that may be considered forms of swearing or cursing.
         */
        PROFANITY(1),

        /**
         * Words that refer to sexually explicit behavior or activity.
         */
        SEXUAL_CONTENT(2),

        /**
         * Personal insults or words that may be considered hate speech.
         */
        SLURS(3);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * Constructs a {@code AutoModRule.TriggerType}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Preset(final int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the type of message. It is guaranteed that invoking {@link #getValue()} from the returned enum will be
         * equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of message.
         */
        public static AutoModRuleTriggerMetaData.Preset of(final int value) {
            for (Preset preset : values()) {
                if (preset.getValue() == value) {
                    return preset;
                }
            }
            return UNKNOWN;
        }
    }

}
