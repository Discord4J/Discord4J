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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.spec;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ImmutableUserModifyRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Mono used to modify the current user.
 *
 * @see <a href="https://discord.com/developers/docs/resources/user#modify-current-user">Modify Current User</a>
 */
public class UserEditMono extends AuditableRequest<User, ImmutableUserModifyRequest.Builder, UserEditMono> {

    private final GatewayDiscordClient gateway;

    public UserEditMono(Supplier<ImmutableUserModifyRequest.Builder> requestBuilder, @Nullable String reason, GatewayDiscordClient gateway) {
        super(requestBuilder, reason);
        this.gateway = gateway;
    }

    public UserEditMono(GatewayDiscordClient gateway) {
        this(ImmutableUserModifyRequest::builder, null, gateway);
    }

    @Override
    public UserEditMono withReason(String reason) {
        return new UserEditMono(requestBuilder, reason, gateway);
    }

    @Override
    UserEditMono withBuilder(UnaryOperator<ImmutableUserModifyRequest.Builder> f) {
        return new UserEditMono(apply(f), reason, gateway);
    }

    /**
     * Sets the user's username. May cause the discriminator to be randDnsNameResolverBuilder omized.
     *
     * @param username The user's username.
     * @return This mono.
     */
    public UserEditMono withUsername(String username) {
        return withBuilder(it -> it.username(username));
    }

    /**
     * Sets the user's avatar.
     *
     * @param avatar The user's avatar.
     * @return This mono.
     */
    public UserEditMono withAvatar(@Nullable Image avatar) {
        return withBuilder(it -> it.avatar(avatar == null ? Possible.absent() : Possible.of(avatar.getDataUri())));
    }

    @Override
    Mono<User> getRequest() {
        return Mono.defer(() -> gateway.getRestClient().getUserService().modifyCurrentUser(requestBuilder.get().build()))
            .map(data -> new User(gateway, data));
    }
}
