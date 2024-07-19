package discord4j.core.object.onboarding;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.OnboardingPromptOptionData;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents an option for an onboarding prompt.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#guild-onboarding-object-prompt-option-structure">
 *     Guild Onboarding Prompt Option Structure</a>
 */
public class OnboardingPromptOption implements Entity {

    private final GatewayDiscordClient client;
    private final OnboardingPromptOptionData data;
    private final Snowflake id;
    private final List<Snowflake> addedChannelIds;
    private final List<Snowflake> roleIds;
    private final ReactionEmoji emoji;

    public OnboardingPromptOption(GatewayDiscordClient client, OnboardingPromptOptionData data) {
        this.client = client;
        this.data = data;

        this.id = Snowflake.of(data.id());
        this.addedChannelIds = data.channelsIds().stream()
                .map(Snowflake::of)
                .collect(Collectors.toList());
        this.roleIds = data.rolesIds().stream()
                .map(Snowflake::of)
                .collect(Collectors.toList());

        this.emoji = data.emoji().toOptional().map(ReactionEmoji::of).orElse(null);
    }

    /**
     * Gets the raw data for this option.
     *
     * @return The raw data for this option.
     */
    public OnboardingPromptOptionData getData() {
        return this.data;
    }

    /**
     * Gets the channel IDs in which the option will add the user.
     *
     * @return The channel IDs in which the option will add the user.
     */
    public List<Snowflake> getAddedChannelIds() {
        return this.addedChannelIds;
    }

    /**
     * Gets the role IDs in which the option will add the user.
     *
     * @return The role IDs in which the option will add the user.
     */
    public List<Snowflake> getRoleIds() {
        return this.roleIds;
    }

    /**
     * Gets the title of the option.
     *
     * @return The title of the option.
     */
    public String getTitle() {
        return this.data.title();
    }

    /**
     * Gets the description of the option.
     *
     * @return An {@link Optional} containing the description of the option or {@link Optional#empty()} if not present.
     */
    public Optional<String> getDescription() {
        return this.data.description();
    }

    /**
     * Gets the emoji of the option.
     *
     * @return An {@link Optional} containing the emoji of the option or {@link Optional#empty()} if not present.
     */
    public Optional<ReactionEmoji> getEmoji() {
        return Optional.ofNullable(this.emoji);
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
        return "OnboardingPromptOption{" +
            "id=" + id +
            ", addedChannelIds=" + addedChannelIds +
            ", roleIds=" + roleIds +
            ", emoji=" + emoji +
            '}';
    }
}
