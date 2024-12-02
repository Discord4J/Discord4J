package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.ForumTagData;

import java.util.Optional;

public class ForumTag implements Entity {

    /**
     * The gateway associated to this object.
     */
    protected final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final ForumTagData data;

    public ForumTag(final GatewayDiscordClient gateway, final ForumTagData data) {
        this.gateway = gateway;
        this.data = data;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the name for this tag
     *
     * @return The tag name
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets whether this tag is moderated, meaning only members with the MANAGE_THREADS permission can use it
     *
     * @return {@code true} if this tag is moderated
     */
    public boolean isModerated() {
        return data.moderated();
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
     * Gets the associated {@link ForumTagData} wrapped by this object
     *
     * @return the wrapped {@link ForumTagData} object
     */
    public ForumTagData getData() {
        return data;
    }

}
