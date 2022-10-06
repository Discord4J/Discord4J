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
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.discordjson.json.ApplicationCommandInteractionResolvedData;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

@Experimental
public class ApplicationCommandInteractionOptionValue implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    @Nullable
    private final Long guildId;
    private final int type;
    private final String value;
    @Nullable
    private final ApplicationCommandInteractionResolvedData resolved;

    public ApplicationCommandInteractionOptionValue(final GatewayDiscordClient gateway, @Nullable final Long guildId,
                                                    final int type, final String value,
                                                    @Nullable final ApplicationCommandInteractionResolvedData resolved) {
        this.gateway = gateway;
        this.guildId = guildId;
        this.value = value;
        this.type = type;
        this.resolved = resolved;
    }

    public String getRaw() {
        return value;
    }

    public String asString() {
        return getValueAs("string", Function.identity(), ApplicationCommandOption.Type.STRING);
    }

    public boolean asBoolean() {
        return getValueAs("boolean", Boolean::parseBoolean, ApplicationCommandOption.Type.BOOLEAN);
    }

    public long asLong() {
        return getValueAs("long", Long::parseLong, ApplicationCommandOption.Type.INTEGER);
    }

    public double asDouble() {
        return getValueAs("double", Double::parseDouble, ApplicationCommandOption.Type.NUMBER);
    }

    public Snowflake asSnowflake() {
        return getValueAs("snowflake", Snowflake::of,
                ApplicationCommandOption.Type.USER,
                ApplicationCommandOption.Type.ROLE,
                ApplicationCommandOption.Type.CHANNEL,
                ApplicationCommandOption.Type.MENTIONABLE);
    }

    public Mono<User> asUser() {
        return getValueAs("user",
                value -> getClient().getUserById(Snowflake.of(value)),
                ApplicationCommandOption.Type.USER);
    }

    public Mono<Role> asRole() {
        return getValueAs("role",
                value -> getClient().getRoleById(Snowflake.of(Objects.requireNonNull(guildId)), Snowflake.of(value)),
                ApplicationCommandOption.Type.ROLE);
    }

    public Mono<Channel> asChannel() {
        return getValueAs("channel",
                value -> getClient().getChannelById(Snowflake.of(value)),
                ApplicationCommandOption.Type.CHANNEL);
    }

    public Attachment asAttachment() {
        return getValueAs("attachment",
                value -> new Attachment(getClient(), Objects.requireNonNull(resolved)
                        .attachments().get().get(value)),
                ApplicationCommandOption.Type.ATTACHMENT);
    }

    private <T> T getValueAs(String parsedTypeName, Function<String, T> parser,
                             ApplicationCommandOption.Type... allowedTypes) {
        if (!Arrays.asList(allowedTypes).contains(ApplicationCommandOption.Type.of(type))) {
            throw new IllegalArgumentException("Option value cannot be converted as " + parsedTypeName);
        }

        return parser.apply(value);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }
}
