/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core.object.command;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.monetization.Entitlement;
import discord4j.discordjson.json.InteractionData;
import discord4j.discordjson.json.UserData;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * Gets the id of the application this interaction is for.
     *
     * @return The id of the application this interaction is for.
     */
    public Snowflake getApplicationId() {
        return Snowflake.of(data.applicationId());
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
    public Optional<ApplicationCommandInteraction> getCommandInteraction() {
        return data.data().toOptional().map(data -> new ApplicationCommandInteraction(getClient(), data,
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
    public Mono<MessageChannel> getChannel() {
        return gateway.getChannelById(getChannelId()).cast(MessageChannel.class);
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

    /**
     * Gets the message associated with the interaction.
     *
     * @return The message associated with the interaction.
     */
    public Optional<Message> getMessage() {
        return data.message().toOptional()
                .map(data -> new Message(gateway, data));
    }

    /**
     * Gets the ID of the message associated with the interaction.
     *
     * @return The message associated with the interaction.
     */
    public Optional<Snowflake> getMessageId() {
        return data.message().toOptional()
                .map(data -> Snowflake.of(data.id()));
    }

    /**
     * Gets the invoking user's client locale.
     * <br>
     * This is not present on {@code PING} interactions and will therefore default to {@code en-US}
     *
     * @see <a href="https://discord.com/developers/docs/reference#locales">Discord Locales</a>
     * @return The invoking user's client locale.
     */
    public String getUserLocale() {
        return data.locale().toOptional().orElse("en-US");
    }

    /**
     * Gets the guild's locale if the interaction was invoked from a guild.
     * Defaults to {@code en-US} for non-community guilds.
     * <br>
     * This is not present on {@code PING} interactions
     *
     * @see <a href="https://discord.com/developers/docs/reference#locales">Discord Locales</a>
     * @return The locale of the guild where the interaction was invoked, otherwise {@link Optional#empty()}
     */
    public Optional<String> getGuildLocale() {
        return data.guildLocale().toOptional();
    }

    /**
     * Get the id of the authorizing integration owner for the given integration type, if present.
     *
     * @param type The type of integration to get the owner for.
     * @return An {@link Optional} containing the id of the authorizing integration owner if present.
     */
    public Optional<Snowflake> getAuthorizingIntegrationOwner(ApplicationIntegrationType type) {
        return Optional.ofNullable(data.authorizingIntegrationOwners().get(type.getValue()))
            .map(Snowflake::of);
    }

    /**
     * Get the authorizing integration owners for the interaction.
     *
     * @return A {@link Map} containing the authorizing integration owners for the interaction.
     */
    public Map<ApplicationIntegrationType, Snowflake> getAuthorizingIntegrationOwners() {
        return data.authorizingIntegrationOwners().entrySet().stream()
            .collect(Collectors.toMap(entry -> ApplicationIntegrationType.of(entry.getKey()), entry -> Snowflake.of(entry.getValue())));
    }

    /**
     * Get the context of the interaction.
     *
     * @return An {@link Optional} containing the context of the interaction if present.
     */
    public Optional<ApplicationCommandContexts> getContext() {
        return data.context().toOptional().map(ApplicationCommandContexts::of);
    }

    /**
     * Gets the entitlements attached to the interaction.
     *
     * @return The list of {@link Entitlement} attached to the interaction.
     */
    @Experimental // This method could not be tested due to the lack of a Discord verified application
    public List<Entitlement> getEntitlements() {
        return data.entitlements()
            .stream()
            .map(entitlementData -> new Entitlement(gateway, entitlementData))
            .collect(Collectors.toList());
    }

    /**
     * Checks if the user has an entitlement for this interaction.
     *
     * @return {@code true} if the user has an entitlement for this interaction, {@code false} otherwise.
     */
    @Experimental // This method could not be tested due to the lack of a Discord verified application
    public boolean hasUserEntitlement() {
        User user = getUser();
        return data.entitlements().stream().anyMatch(entitlementData -> !entitlementData.userId().isAbsent() && entitlementData.userId().get().asLong() == user.getId().asLong());
    }

    /**
     * Checks if the guild has an entitlement for this interaction.
     *
     * @return {@code true} if the guild has an entitlement for this interaction, {@code false} otherwise.
     */
    @Experimental // This method could not be tested due to the lack of a Discord verified application
    public boolean hasGuildEntitlement() {
        if (data.guildId().isAbsent())
            return false;

        return data.entitlements().stream().anyMatch(entitlementData -> !entitlementData.guildId().isAbsent() && entitlementData.guildId().get().asLong() == data.guildId().get().asLong());
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /** Represents the various types of interaction. */
    public enum Type {

        UNKNOWN(-1),
        PING(1),
        APPLICATION_COMMAND(2),
        MESSAGE_COMPONENT(3),
        APPLICATION_COMMAND_AUTOCOMPLETE(4),
        MODAL_SUBMIT(5);

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
                case 3: return MESSAGE_COMPONENT;
                case 4: return APPLICATION_COMMAND_AUTOCOMPLETE;
                case 5: return MODAL_SUBMIT;
                default: return UNKNOWN;
            }
        }
    }
}
