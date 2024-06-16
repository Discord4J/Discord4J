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
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.PermissionSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A Discord application command.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/slash-commands#applicationcommand">
 * Application Command Object</a>
 */
@Experimental
public class ApplicationCommand implements DiscordObject {

    /** The maximum amount of characters that can be in an application command name. */
    public static final int MAX_NAME_LENGTH = 32;
    /** The maximum amount of characters that can be in an application command description. */
    public static final int MAX_DESCRIPTION_LENGTH = 100;

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ApplicationCommandData data;

    /**
     * Constructs an {@code ApplicationCommand} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ApplicationCommand(final GatewayDiscordClient gateway, final ApplicationCommandData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    /**
     * Gets unique id of the command.
     *
     * @return The unique id of the command.
     */
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the version of the command.
     *
     * @return The version of the command
     */
    public Snowflake getVersion() {
        return Snowflake.of(data.version());
    }

    /**
     * Gets the id of the guild if the command is guild scoped.
     *
     * @return The id of the guild
     */
    public Optional<Snowflake> getGuildId() {
        return data.guildId().toOptional().map(Snowflake::of);
    }

    /**
     * Gets the type of the command.
     *
     * @return The type of the command.
     */
    public Type getType() {
        // Discord defaults to treating the type as a CHAT_INPUT command if type is not present.
        return data.type().toOptional()
            .map(Type::of)
            .orElse(Type.CHAT_INPUT);
    }

    /**
     * Gets the unique id of the parent application.
     *
     * @return The unique id of the parent application.
     */
    public Snowflake getApplicationId() {
        return Snowflake.of(data.applicationId());
    }

    /**
     * Gets the name of the command.
     *
     * @return The name of the command.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the Locale and name of the command.
     *
     * @return The locales and names of the command.
     */
    public Map<Locale, String> getLocalizedNames() {
        return Possible.flatOpt(data.nameLocalizations())
                .orElse(Collections.emptyMap())
                .entrySet().stream()
                .collect(Collectors.toMap(entry -> new Locale.Builder().setLanguageTag(entry.getKey()).build(),
                        Map.Entry::getValue));
    }

    /**
     * Gets description of the command.
     *
     * @return The description of the command.
     */
    public String getDescription() {
        return data.description();
    }

    /**
     * Gets the Locale and description of the command.
     *
     * @return The locales and descriptions of the command.
     */
    public Map<Locale, String> getLocalizedDescriptions() {
        return Possible.flatOpt(data.descriptionLocalizations())
                .orElse(Collections.emptyMap())
                .entrySet().stream()
                .collect(Collectors.toMap(entry -> new Locale.Builder().setLanguageTag(entry.getKey()).build(),
                        Map.Entry::getValue));
    }

    /**
     * Gets the default permissions assigned to this Application Command for member.
     *
     * @return The permissions assigned to this Application Command.
     */
    public PermissionSet getDefaultMemberPermissions() {
        return data.defaultMemberPermissions().map(Long::parseLong).map(PermissionSet::of).orElse(PermissionSet.none());
    }

    /**
     * Gets the options of the command.
     *
     * @return The options of the command.
     */
    public List<ApplicationCommandOption> getOptions() {
        return data.options().toOptional().orElse(Collections.emptyList()).stream()
                .map(data -> new ApplicationCommandOption(gateway, data))
                .collect(Collectors.toList());
    }

    /**
     * Gets the option corresponding to the provided name, if present.
     *
     * @param name The name of the option.
     * @return The option corresponding to the provided name, if present.
     */
    public Optional<ApplicationCommandOption> getOption(final String name) {
        return data.options().toOptional().orElse(Collections.emptyList()).stream()
                .filter(option -> option.name().equals(name))
                .findFirst()
                .map(data -> new ApplicationCommandOption(gateway, data));
    }

    /**
     * Gets if the command is available in DM, this only for global commands.
     *
     * @return {@code true} if the command is available in DM, {@code false} otherwise.
     */
    public boolean isAvailableInDM() {
        return data.dmPermission().toOptional().orElse(!this.getGuildId().isPresent());
    }

    /**
     * Gets if the command is flagged as NSFW.
     *
     * @return {@code true} if the command is flagged as NSFW, {@code false} otherwise.
     */
    public boolean isNsfw() {
        return data.nsfw().toOptional().orElse(false);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * @see <a href="https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-types">
     * Application Command Type</a>
     */
    public enum Type {
        UNKNOWN(-1),
        CHAT_INPUT(1),
        USER(2),
        MESSAGE(3);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * Constructs an {@code ApplicationCommandType}.
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
         * Gets the type of application command. It is guaranteed that invoking {@link #getValue()} from the
         * returned enum will
         * equal ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of command.
         */
        public static Type of(final int value) {
            switch (value) {
                case 1: return CHAT_INPUT;
                case 2: return USER;
                case 3: return MESSAGE;
                default: return UNKNOWN;
            }
        }
    }
}
