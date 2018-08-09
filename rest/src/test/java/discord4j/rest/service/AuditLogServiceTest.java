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

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.rest.RestTests;
import discord4j.rest.request.Router;
import org.junit.Test;

import java.util.Collections;

public class AuditLogServiceTest {

    private static final long guild = Long.parseUnsignedLong(System.getenv("guild"));

    private AuditLogService auditLogService = null;

    private AuditLogService getAuditLogService() {

        if (auditLogService != null) {
            return auditLogService;
        }

        String token = System.getenv("token");
        boolean ignoreUnknown = !Boolean.parseBoolean(System.getenv("failUnknown"));
        ObjectMapper mapper = RestTests.getMapper(ignoreUnknown);
        Router router = RestTests.getRouter(token, mapper);

        return auditLogService = new AuditLogService(router);
    }

    @Test
    public void testGetAuditLog() {
        getAuditLogService().getAuditLog(guild, Collections.emptyMap()).block();
    }
}
