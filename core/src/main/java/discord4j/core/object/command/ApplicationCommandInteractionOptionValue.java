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
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.rest.util.ApplicationCommandOptionType;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

@Experimental
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
        if (type != ApplicationCommandOptionType.STRING.getValue()) {
            throw new IllegalArgumentException("Option value cannot be converted as string");
        }
        return value;
    }

    public boolean asBoolean() {
        if (type != ApplicationCommandOptionType.BOOLEAN.getValue()) {
            throw new IllegalArgumentException("Option value cannot be converted as boolean");
        }
        return Boolean.parseBoolean(value);
    }

    public long asLong() {
        if (type != ApplicationCommandOptionType.INTEGER.getValue()) {
            throw new IllegalArgumentException("Option value cannot be converted as long");
        }
        return Long.parseLong(value);
    }

    public double asDouble() {
        if (type != ApplicationCommandOptionType.NUMBER.getValue()) {
            throw new IllegalArgumentException("Option value cannot be converted as double");
        }
        return Double.parseDouble(value);
    }

    public Snowflake asSnowflake() {
        if (type != ApplicationCommandOptionType.USER.getValue()
                && type != ApplicationCommandOptionType.ROLE.getValue()
                && type != ApplicationCommandOptionType.CHANNEL.getValue()) {
            throw new IllegalArgumentException("Option value cannot be converted as snowflake");
        }

        return Snowflake.of(value);
    }

    public Mono<User> asUser() {
        if (type != ApplicationCommandOptionType.USER.getValue()) {
            return Mono.error(new IllegalArgumentException("Option value cannot be converted as user"));
        }

        return getClient().getUserById(asSnowflake());
    }

    public Mono<Role> asRole() {
        if (type != ApplicationCommandOptionType.ROLE.getValue()) {
            return Mono.error(new IllegalArgumentException("Option value cannot be converted as role"));
        }

        return getClient().getRoleById(Snowflake.of(guildId), asSnowflake());
    }

    public Mono<Channel> asChannel() {
        if (type != ApplicationCommandOptionType.CHANNEL.getValue()) {
            return Mono.error(new IllegalArgumentException("Option value cannot be converted as channel"));
        }

        return getClient().getChannelById(asSnowflake());
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }
}
