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

import com.darichey.discordjson.json.AuditLogData;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;

import java.util.Map;

public class AuditLogService extends RestService {

    public AuditLogService(Router router) {
        super(router);
    }

    public Mono<AuditLogData> getAuditLog(long guildId, Map<String, Object> queryParams) {
        return Routes.AUDIT_LOG_GET.newRequest(guildId)
                .query(queryParams)
                .exchange(getRouter())
                .bodyToMono(AuditLogData.class);
    }
}
