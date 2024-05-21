package discord4j.core.object.reaction;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.discordjson.json.DefaultReactionData;

import java.util.Optional;

/**
 * DefaultReaction is used by {@link discord4j.core.object.entity.channel.ForumChannel} as an emoji shown as a quick reaction button on the thread in a forum channel.
 * When selecting an emoji, the user can use it from the forum channel page.
 */
public final class DefaultReaction implements DiscordObject {

    private final GatewayDiscordClient gateway;
    private final DefaultReactionData data;

    public DefaultReaction(final GatewayDiscordClient gateway, final DefaultReactionData data) {
        this.gateway = gateway;
        this.data = data;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the emoji id if this tag is represented as a custom guild emoji
     * At least this field or {@link #getEmojiName} is present and filled.
     *
     * @return An {@link Optional} which may contain a custom guild emoji id.
     */
    public Optional<Snowflake> getEmojiId() {
        return data.emojiId().map(Snowflake::of);
    }

    /**
     * Gets the unicode emoji if this tag is represented as a unicode emoji
     * At least this field or {@link #getEmojiId} is present and filled.
     *
     * @return An {@link Optional} which may contain a custom guild emoji id.
     */
    public Optional<String> getEmojiName() {
        return data.emojiName();
    }

    /**
     * Gets the {@link DefaultReactionData} associated to this wrapper
     *
     * @return the {@link DefaultReactionData} object wrapped by this object
     */
    public DefaultReactionData getData() {
        return data;
    }

}
