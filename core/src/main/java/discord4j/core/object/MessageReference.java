package discord4j.core.object;

import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.data.stored.MessageReferenceBean;
import discord4j.core.object.util.Snowflake;

import java.util.Objects;
import java.util.Optional;

/**
 * A Message Reference used in Webhooks from Follow channel system.
 * All data here is of the Guild Source of followed channel.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#message-reference-structure?">MessageReference Object</a>
 */
public class MessageReference implements DiscordObject {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final MessageReferenceBean data;

    /**
     * Constructs an {@code MessageReference} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public MessageReference(final ServiceMediator serviceMediator, final MessageReferenceBean data) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
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
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    @Override
    public String toString() {
        return "MessageReference{" +
            "data=" + data +
            '}';
    }
}
