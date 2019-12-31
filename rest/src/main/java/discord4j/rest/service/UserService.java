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
package discord4j.rest.service;

import discord4j.common.json.UserResponse;
import discord4j.rest.json.request.DMCreateRequest;
import discord4j.rest.json.request.GroupDMCreateRequest;
import discord4j.rest.json.request.UserModifyRequest;
import discord4j.rest.json.response.ChannelResponse;
import discord4j.rest.json.response.ConnectionResponse;
import discord4j.rest.json.response.UserGuildResponse;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class UserService extends RestService {

    public UserService(Router router) {
        super(router);
    }

    public Mono<UserResponse> getCurrentUser() {
        return Routes.CURRENT_USER_GET.newRequest()
                .exchange(getRouter())
                .bodyToMono(UserResponse.class);
    }

    public Mono<UserResponse> getUser(long userId) {
        return Routes.USER_GET.newRequest(userId)
                .exchange(getRouter())
                .bodyToMono(UserResponse.class);
    }

    public Mono<UserResponse> modifyCurrentUser(UserModifyRequest request) {
        return Routes.CURRENT_USER_MODIFY.newRequest()
                .body(request)
                .exchange(getRouter())
                .bodyToMono(UserResponse.class);
    }

    public Flux<UserGuildResponse> getCurrentUserGuilds(Map<String, Object> queryParams) {
        return Routes.CURRENT_USER_GUILDS_GET.newRequest()
                .query(queryParams)
                .exchange(getRouter())
                .bodyToMono(UserGuildResponse[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Mono<Void> leaveGuild(long guildId) {
        return Routes.GUILD_LEAVE.newRequest(guildId)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Flux<ChannelResponse> getUserDMs() {
        return Routes.USER_DMS_GET.newRequest()
                .exchange(getRouter())
                .bodyToMono(ChannelResponse[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Mono<ChannelResponse> createDM(DMCreateRequest request) {
        return Routes.USER_DM_CREATE.newRequest()
                .body(request)
                .exchange(getRouter())
                .bodyToMono(ChannelResponse.class);
    }

    public Mono<ChannelResponse> createGroupDM(GroupDMCreateRequest request) {
        return Routes.GROUP_DM_CREATE.newRequest()
                .body(request)
                .exchange(getRouter())
                .bodyToMono(ChannelResponse.class);
    }

    public Flux<ConnectionResponse> getUserConnections() {
        return Routes.USER_CONNECTIONS_GET.newRequest()
                .exchange(getRouter())
                .bodyToMono(ConnectionResponse[].class)
                .flatMapMany(Flux::fromArray);
    }
}
