package discord4j.core.object.command;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.InteractionData;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

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
     * Gets the command data payload, if present.
     * Note: This is always present on ApplicationCommand interaction types. It is optional for future-proofing
     * against new interaction types.
     *
     * @return The command data payload, if present.
     */
    public Optional<ApplicationCommandInteraction> getApplicationCommandInteraction() {
        return data.data().toOptional()
            .map(interactionData -> new ApplicationCommandInteraction(gateway, interactionData,
                    getGuildId().map(Snowflake::asLong).orElse(null)));
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
     * Gets the channel id it was sent from, if invoked in a guild.
     *
     * @return The channel id it was sent from, if invoked in a guild.
     */
    public Optional<Snowflake> getChannelId() {
        return data.channelId().toOptional().map(Snowflake::of);
    }

    /**
     * Gets the channel it was sent from, if invoked in a guild.
     *
     * @return The channel it was sent from, if invoked in a guild.
     */
    public Mono<TextChannel> getChannel() {
        return Mono.justOrEmpty(getChannelId()).map(gateway::getChannelById).cast(TextChannel.class);
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
     * Gets the invoking user, if invoked in a DM.
     *
     * @return The invoking user, if invoked in a DM.
    public Optional<User> getUser() {
        return Optional.of(data.user())
            .map(data -> new User(gateway, data));
    }
     */

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
         * Gets the type of interaction. It is guaranteed that invoking {@link #getValue()} from the returned enum will equal
         * ({@link #equals(Object)}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of interaction.
         */
        public static Interaction.Type of(final int value) {
            return Arrays.stream(values()).filter(type -> type.getValue() == value).findFirst().orElse(UNKNOWN);
        }
    }
}
