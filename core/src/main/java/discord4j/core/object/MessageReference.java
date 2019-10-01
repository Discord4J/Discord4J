package discord4j.core.object;

import discord4j.common.annotations.Experimental;
import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
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

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final MessageReferenceBean data;

    /**
     * Constructs a {@code MessageReference} with an associated {@link ServiceMediator} and Discord data.
     *
     * @param serviceMediator The {@link ServiceMediator} associated to this object, must be non-null.
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
