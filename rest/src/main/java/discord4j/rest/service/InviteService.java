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

import discord4j.discordjson.json.InviteData;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

public class InviteService extends RestService {

    public InviteService(Router router) {
        super(router);
    }

    public Mono<InviteData> getInvite(String inviteCode) {
        return getInvite(inviteCode, Collections.emptyMap());
    }

    public Mono<InviteData> getInvite(String inviteCode, Map<String, Object> queryParams) {
        return Routes.INVITE_GET.newRequest(inviteCode)
                .query(queryParams)
                .exchange(getRouter())
                .bodyToMono(InviteData.class);
    }

    public Mono<InviteData> deleteInvite(String inviteCode, @Nullable String reason) {
        return Routes.INVITE_DELETE.newRequest(inviteCode)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(InviteData.class);
    }
}
