package discord4j.core.object.onboarding;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.discordjson.json.OnboardingData;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the onboarding configuration for a guild.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#guild-onboarding-object-guild-onboarding-structure">
 *     Guild Onboarding Structure</a>
 */
public class Onboarding implements Entity {

    private final GatewayDiscordClient client;
    private final OnboardingData data;
    private final Snowflake guildId;
    private final List<OnboardingPrompt> prompts;
    private final List<Snowflake> defaultChannelIds;

    public Onboarding(GatewayDiscordClient client, OnboardingData data) {
        this.client = client;
        this.data = data;

        this.guildId = Snowflake.of(data.guildId());
        this.prompts = data.prompts().stream()
                .map(promptData -> new OnboardingPrompt(client, promptData))
                .collect(Collectors.toList());
        this.defaultChannelIds = data.defaultChannelIds().stream()
                .map(Snowflake::of)
                .collect(Collectors.toList());
    }

    /**
     * Gets the raw data for this onboarding configuration.
     *
     * @return The raw data for this onboarding configuration.
     */
    public OnboardingData getData() {
        return this.data;
    }

    /**
     * Gets the guild ID for this onboarding configuration.
     *
     * @return The guild ID for this onboarding configuration.
     */
    public Snowflake getGuildId() {
        return this.guildId;
    }

    /**
     * Gets whether the onboarding configuration is enabled.
     *
     * @return Whether the onboarding configuration is enabled.
     */
    public boolean isEnabled() {
        return this.data.enabled();
    }

    /**
     * Gets the onboarding mode.
     *
     * @return The onboarding mode.
     */
    public Mode getMode() {
        return Mode.from(this.data.mode());
    }

    /**
     * Gets the prompts for the onboarding configuration.
     *
     * @return The prompts for the onboarding configuration.
     */
    public List<OnboardingPrompt> getPrompts() {
        return this.prompts;
    }

    /**
     * Gets the default channel IDs for the onboarding configuration.
     *
     * @return The default channel IDs for the onboarding configuration.
     */
    public List<Snowflake> getDefaultChannelIds() {
        return this.defaultChannelIds;
    }

    @Override
    public Snowflake getId() {
        return this.guildId;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.client;
    }

    @Override
    public String toString() {
        return "Onboarding{" +
            "guildId=" + guildId +
            ", prompts=" + prompts +
            ", defaultChannelIds=" + defaultChannelIds +
            '}';
    }

    /**
     * The onboarding mode.
     */
    public enum Mode {

        /** Unknown mode. */
        UNKNOWN(-1),

        /** Counts only Default Channels towards constraints */
        ONBOARDING_DEFAULT(0),

        /** Counts Default Channels and Questions towards constraints */
        ONBOARDING_ADVANCED(1);

        /** The underlying value of the mode as represented by Discord. */
        private final int value;

        /**
         * Constructs a new mode with the given value.
         *
         * @param value The underlying value of the mode as represented by Discord.
         */
        Mode(int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value of the mode as represented by Discord.
         *
         * @return The underlying value of the mode as represented by Discord.
         */
        public int getValue() {
            return this.value;
        }

        /**
         * Gets the onboarding mode. It is guaranteed that invoking {@link #getValue()} from the returned enum will equal
         * ({@link #equals(Object)}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The onboarding mode.
         */
        public static Mode from(int value) {
            switch (value) {
                case 0: return Mode.ONBOARDING_DEFAULT;
                case 1: return Mode.ONBOARDING_ADVANCED;
                default: return Mode.UNKNOWN;
            }
        }
    }
}
