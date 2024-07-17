package discord4j.core.object.onboarding;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.discordjson.json.OnboardingPromptData;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents an onboarding prompt.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#guild-onboarding-object-onboarding-prompt-structure">
 *     Guild Onboarding Prompt Structure</a>
 */
public class OnboardingPrompt implements Entity {

    private final GatewayDiscordClient client;
    private final OnboardingPromptData data;
    private final Snowflake id;
    private final Type type;
    private final List<OnboardingPromptOption> options;

    public OnboardingPrompt(GatewayDiscordClient client, OnboardingPromptData data) {
        this.client = client;
        this.data = data;

        this.id = Snowflake.of(data.id());
        this.type = Type.from(this.data.type());
        this.options = data.options()
                .stream()
                .map(promptOptionData -> new OnboardingPromptOption(client, promptOptionData))
                .collect(Collectors.toList());
    }

    /**
     * Gets the raw data for this prompt.
     *
     * @return The raw data for this prompt.
     */
    public OnboardingPromptData getData() {
        return this.data;
    }

    /**
     * Gets the type of the prompt.
     *
     * @return The type of the prompt.
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Gets the options for the prompt.
     *
     * @return The options for the prompt.
     */
    public List<OnboardingPromptOption> getOptions() {
        return this.options;
    }

    /**
     * Gets the title of the prompt.
     *
     * @return The title of the prompt.
     */
    public String getTitle() {
        return this.data.title();
    }

    /**
     * Indicates whether users are limited to selecting one option for the prompt
     *
     * @return {@code true} if users are limited to selecting one option for the prompt, {@code false} otherwise.
     */
    public boolean isSingleSelect() {
        return this.data.singleSelect();
    }

    /**
     * Indicates whether the prompt is required before a user completes the onboarding flow
     *
     * @return {@code true} if the prompt is required before a user completes the onboarding flow, {@code false} otherwise.
     */
    public boolean isRequired() {
        return this.data.required();
    }

    /**
     * Indicates whether the prompt is present in the onboarding flow. If false, the prompt will only appear
     * in the Channels and Roles tab
     *
     * @return {@code true} if the prompt is present in the onboarding flow, {@code false} otherwise.
     */
    public boolean isInOnboarding() {
        return this.data.inOnboarding();
    }

    @Override
    public Snowflake getId() {
        return this.id;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.client;
    }

    @Override
    public String toString() {
        return "OnboardingPrompt{" +
            "id=" + id +
            ", type=" + type +
            ", options=" + options +
            '}';
    }

    /**
     * Represents the type of onboarding prompt.
     */
    public enum Type {

        /** An unknown prompt type. */
        UNKNOWN(-1),

        /** A multiple choice prompt. */
        MULTIPLE_CHOICE(0),
        /** A dropdown prompt. */
        DROPDOWN(1);

        /**
         * The underlying value of the prompt type as represented by Discord.
         */
        private final int value;

        /**
         * Constructs a new prompt type with the given value.
         *
         * @param value The underlying value of the prompt type as represented by Discord.
         */
        Type(int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value of the prompt type as represented by Discord.
         *
         * @return The underlying value of the prompt type as represented by Discord.
         */
        public int getValue() {
            return this.value;
        }

        /**
         * Gets the prompt type from the given value. It is guaranteed that invoking {@link #getValue()} from the returned enum will equal
         * ({@link #equals(Object)}) the supplied {@code value}.
         *
         * @param value The underlying value of the prompt type as represented by Discord.
         * @return The prompt type from the given value.
         */
        public static Type from(int value) {
            switch (value) {
                case 0: return Type.MULTIPLE_CHOICE;
                case 1: return Type.DROPDOWN;
                default: return Type.UNKNOWN;
            }
        }
    }
}
