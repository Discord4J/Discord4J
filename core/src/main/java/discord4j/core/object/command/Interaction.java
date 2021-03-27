package discord4j.core.object.command;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.InteractionData;
import discord4j.discordjson.json.UserData;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

/**
 * A Discord interaction.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/slash-commands#interaction">Interaction Object</a>
 */
@Experimental
public class Interaction implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final InteractionData data;

    /**
     * Constructs an {@code Interaction} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Interaction(final GatewayDiscordClient gateway, final InteractionData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    public InteractionData getData() {
        return data;
    }

    /**
     * Gets the id of the interaction.
     *
     * @return The id of the interaction.
     */
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the type of interaction.
     *
     * @return The type of interaction.
     */
    public Type getType() {
        return Type.of(data.type());
    }

    /**
     * Gets the command data payload.
     *
     * @return The command data payload.
     */
    public ApplicationCommandInteraction getCommandInteraction() {
        return new ApplicationCommandInteraction(getClient(), data.data().get(),
                getGuildId().map(Snowflake::asLong).orElse(null));
    }

    /**
     * Gets the guild id it was sent from, if invoked in a guild.
     *
     * @return The guild id it was sent from, if invoked in a guild.
     */
    public Optional<Snowflake> getGuildId() {
        return data.guildId().toOptional().map(Snowflake::of);
    }

    /**
     * Gets the guild it was sent from, if invoked in a guild.
     *
     * @return The guild it was sent from, if invoked in a guild.
     */
    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(gateway::getGuildById);
    }

    /**
     * Gets the channel id it was sent from.
     *
     * @return The channel id it was sent from.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(data.channelId().get());
    }

    /**
     * Gets the channel it was sent from.
     *
     * @return The channel it was sent from.
     */
    public Mono<TextChannel> getChannel() {
        return gateway.getChannelById(getChannelId()).cast(TextChannel.class);
    }

    /**
     * Gets the invoking member, if invoked in a guild.
     *
     * @return The invoking member, if invoked in a guild.
     */
    public Optional<Member> getMember() {
        return data.member().toOptional()
                .map(data -> new Member(gateway, data, getGuildId().get().asLong()));
    }

    /**
     * Gets the invoking user. The user data is extracted from the member if invoked in a guild.
     *
     * @return The invoking user. The user data is extracted from the member if invoked in a guild.
     */
    public User getUser() {
        UserData userData = data.member().isAbsent() ? data.user().get() : data.member().get().user();
        return new User(getClient(), userData);
    }

    /**
     * Gets the continuation token for responding to the interaction.
     *
     * @return The continuation token for responding to the interaction.
     */
    public String getToken() {
        return data.token();
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /** Represents the various types of interaction. */
    public enum Type {

        UNKNOWN(-1),
        PING(1),
        APPLICATION_COMMAND(2);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs an {@code Interaction.Type}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Type(final int value) {
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
         * Gets the type of interaction. It is guaranteed that invoking {@link #getValue()} from the returned enum
         * will equal ({@link #equals(Object)}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of interaction.
         */
        public static Interaction.Type of(final int value) {
            switch (value) {
                case 1: return PING;
                case 2: return APPLICATION_COMMAND;
                default: return UNKNOWN;
            }
        }
    }
}
