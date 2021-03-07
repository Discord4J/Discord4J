package discord4j.core.object.command;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public class ApplicationCommandInteractionOptionValue implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    @Nullable
    private final Long guildId;
    private final int type;
    private final String value;

    public ApplicationCommandInteractionOptionValue(final GatewayDiscordClient gateway, @Nullable final Long guildId,
                                                    final int type, final String value) {
        this.gateway = gateway;
        this.guildId = guildId;
        this.value = value;
        this.type = type;
    }

    public String getRaw() {
        return value;
    }

    public String asString() {
        if (type != ApplicationCommandOption.Type.STRING.getValue()) {
            throw new IllegalArgumentException("Option value cannot be converted as string");
        }
        return value;
    }

    public boolean asBoolean() {
        if (type != ApplicationCommandOption.Type.BOOLEAN.getValue()) {
            throw new IllegalArgumentException("Option value cannot be converted as boolean");
        }
        return Boolean.parseBoolean(value);
    }

    public long asLong() {
        if (type != ApplicationCommandOption.Type.INTEGER.getValue()) {
            throw new IllegalArgumentException("Option value cannot be converted as long");
        }
        return Long.parseLong(value);
    }

    public Snowflake asSnowflake() {
        if (type != ApplicationCommandOption.Type.USER.getValue()
                && type != ApplicationCommandOption.Type.ROLE.getValue()
                && type != ApplicationCommandOption.Type.CHANNEL.getValue()) {
            throw new IllegalArgumentException("Option value cannot be converted as snowflake");
        }

        return Snowflake.of(value);
    }

    public Mono<User> asUser() {
        if (type != ApplicationCommandOption.Type.USER.getValue()) {
            throw new IllegalArgumentException("Option value cannot be converted as user");
        }

        return getClient().getUserById(asSnowflake());
    }

    public Mono<Role> asRole() {
        if (type != ApplicationCommandOption.Type.ROLE.getValue()) {
            throw new IllegalArgumentException("Option value cannot be converted as role");
        }

        return getClient().getRoleById(Snowflake.of(guildId), asSnowflake());
    }

    public Mono<Channel> asChannel() {
        if (type != ApplicationCommandOption.Type.ROLE.getValue()) {
            throw new IllegalArgumentException("Option value cannot be converted as channel");
        }

        return getClient().getChannelById(asSnowflake());
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }
}