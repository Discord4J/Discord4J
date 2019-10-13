package discord4j.core.object;

import discord4j.common.annotations.Experimental;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.data.stored.MessageReferenceBean;
import discord4j.core.object.util.Snowflake;

import java.util.Objects;
import java.util.Optional;

/**
 * A Message Reference used by the Server Following feature.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#message-object-message-structure">
 * MessageReference Object</a>
 */
@Experimental
public class MessageReference implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final MessageReferenceBean data;

    /**
     * Constructs a {@code MessageReference} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public MessageReference(final GatewayDiscordClient gateway, final MessageReferenceBean data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    public Snowflake getChannelId() {
        return Snowflake.of(data.getChannelId());
    }

    public Optional<Snowflake> getGuildId() {
        return Optional.ofNullable(data.getGuildId())
                .map(Snowflake::of);
    }

    public Optional<Snowflake> getMessageId() {
        return Optional.ofNullable(data.getMessageId())
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
