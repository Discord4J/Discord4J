package discord4j.core.object;

import discord4j.discordjson.json.MessageReferenceData;
import discord4j.common.annotations.Experimental;
import discord4j.core.GatewayDiscordClient;
import discord4j.common.util.Snowflake;

import java.util.Objects;
import java.util.Optional;

/**
 * A Message Reference used by the Server Following feature.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#message-object-message-structure">
 * MessageReference Object</a>
 */
@Experimental
public class MessageReference implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final MessageReferenceData data;

    /**
     * Constructs a {@code MessageReference} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public MessageReference(final GatewayDiscordClient gateway, final MessageReferenceData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    /**
     * Gets the data of the message reference.
     *
     * @return The data of the message reference.
     */
    public MessageReferenceData getData() {
        return data;
    }

    public Snowflake getChannelId() {
        return Snowflake.of(data.channelId().toOptional().orElseThrow(IllegalStateException::new));
    }

    public Optional<Snowflake> getGuildId() {
        return data.guildId().toOptional()
                .map(Snowflake::of);
    }

    public Optional<Snowflake> getMessageId() {
        return data.messageId().toOptional()
                .map(Snowflake::of);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public String toString() {
        return "MessageReference{" +
                "data=" + data +
                '}';
    }
}
