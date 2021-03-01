package discord4j.core.object.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.*;
import reactor.util.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A Discord application command interaction.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/slash-commands#interaction-applicationcommandinteractiondata">
 *     Application Command Interaction Object</a>
 */
public class ApplicationCommandInteractionOption implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ApplicationCommandInteractionOptionData data;

    @Nullable
    private final String guildId;

    /**
     * Constructs an {@code ApplicationCommandInteractionOption} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ApplicationCommandInteractionOption(final GatewayDiscordClient gateway, final ApplicationCommandInteractionOptionData data,
                                               @Nullable final String guildId) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
        this.guildId = guildId;
    }

    /**
     * Gets the name of the option.
     *
     * @return The name of the option.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the value of this option, present if this option is not a group or subcommand.
     *
     * @return The value of this option, present if this option is not a group or subcommand.
     */
    public Optional<String> getValue() {
        return data.value().toOptional();
    }

    public Optional<ApplicationCommand> asSubCommand() {
        final ObjectMapper objectmapper = getClient().getCoreResources().getJacksonResources().getObjectMapper();
        return data.value().toOptional()
            .map(json -> {
                try {
                    return objectmapper.readValue(json, ApplicationCommandData.class);
                } catch (JsonProcessingException e) {
                    // TODO
                    throw new RuntimeException(e);
                }
            })
            .map(applicationData -> new ApplicationCommand(gateway, applicationData));
    }

    public Optional<String> asString() {
        return data.value().toOptional();
    }

    public Optional<Long> asLong() {
        return data.value().toOptional().map(Long::parseLong);
    }

    public Optional<Boolean> asBoolean() {
        return data.value().toOptional().map(Boolean::parseBoolean);
    }

    public Optional<User> asUser() {
        final ObjectMapper objectmapper = getClient().getCoreResources().getJacksonResources().getObjectMapper();
        return data.value().toOptional()
            .map(json -> {
                try {
                    return objectmapper.readValue(json, UserData.class);
                } catch (JsonProcessingException e) {
                    // TODO
                    throw new RuntimeException(e);
                }
            })
            .map(userData -> new User(gateway, userData));
    }

    public Optional<TextChannel> asChannel() {
        final ObjectMapper objectmapper = getClient().getCoreResources().getJacksonResources().getObjectMapper();
        return data.value().toOptional()
            .map(json -> {
                try {
                    return objectmapper.readValue(json, ChannelData.class);
                } catch (JsonProcessingException e) {
                    // TODO
                    throw new RuntimeException(e);
                }
            })
            .map(channelData -> new TextChannel(gateway, channelData));
    }

    public Optional<Role> asRole() {
        final ObjectMapper objectmapper = getClient().getCoreResources().getJacksonResources().getObjectMapper();
        return data.value().toOptional()
            .map(json -> {
                try {
                    return objectmapper.readValue(json, RoleData.class);
                } catch (JsonProcessingException e) {
                    // TODO
                    throw new RuntimeException(e);
                }
            })
            .map(roleData -> new Role(gateway, roleData, Long.parseLong(guildId)));
    }

    /**
     * Gets the options of this option, present if this option is a group or subcommand.
     *
     * @return The options of this option, present if this option is a group or subcommand.
     */
    public List<ApplicationCommandInteractionOption> getOptions() {
        return data.options().toOptional().orElse(Collections.emptyList()).stream()
            .map(data -> new ApplicationCommandInteractionOption(gateway, data, guildId))
            .collect(Collectors.toList());
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }
}
