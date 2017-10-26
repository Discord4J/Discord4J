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

import discord4j.common.json.request.DMCreateRequest;
import discord4j.common.json.request.GroupDMCreateRequest;
import discord4j.common.json.request.UserModifyRequest;
import discord4j.common.json.response.ChannelResponse;
import discord4j.common.json.response.ConnectionResponse;
import discord4j.common.json.response.UserGuildResponse;
import discord4j.common.json.response.UserResponse;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;

public class UserService extends RestService {

	public UserService(Router router) {
		super(router);
	}

	public Mono<UserResponse> getCurrentUser() {
		return Routes.CURRENT_USER_GET.newRequest()
				.exchange(getRouter());
	}

	public Mono<UserResponse> getUser(long userId) {
		return Routes.USER_GET.newRequest(userId)
				.exchange(getRouter());
	}

	public Mono<UserResponse> modifyCurrentUser(UserModifyRequest request) {
		return Routes.CURRENT_USER_MODIFY.newRequest()
				.body(request)
				.exchange(getRouter());
	}

	public Mono<UserGuildResponse[]> getCurrentUserGuilds() {
		return Routes.CURRENT_USER_GUILDS_GET.newRequest()
				.exchange(getRouter());
	}

	public Mono<Void> leaveGuild(long guildId) {
		return Routes.GUILD_LEAVE.newRequest(guildId)
				.exchange(getRouter());
	}

	public Mono<ChannelResponse[]> getUserDMs() {
		return Routes.USER_DMS_GET.newRequest()
				.exchange(getRouter());
	}

	public Mono<ChannelResponse> createDM(DMCreateRequest request) {
		return Routes.USER_DM_CREATE.newRequest()
				.body(request)
				.exchange(getRouter());
	}

	public Mono<ChannelResponse> createGroupDM(GroupDMCreateRequest request) {
		return Routes.GROUP_DM_CREATE.newRequest()
				.body(request)
				.exchange(getRouter());
	}

	public Mono<ConnectionResponse[]> getUserConnections() {
		return Routes.USER_CONNECTIONS_GET.newRequest()
				.exchange(getRouter());
	}
}
