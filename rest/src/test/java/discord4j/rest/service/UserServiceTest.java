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
import discord4j.discordjson.json.DMCreateRequest;
import discord4j.discordjson.json.UserModifyRequest;
import discord4j.rest.RestTests;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.request.Router;
import discord4j.rest.util.Snowflake;
import org.junit.Test;

import java.util.Collections;

public class UserServiceTest {

    private static final long user = Long.parseUnsignedLong(System.getenv("member"));

    private UserService userService = null;

    private UserService getUserService() {

        if (userService != null) {
            return userService;
        }

        String token = System.getenv("token");
        boolean ignoreUnknown = !Boolean.parseBoolean(System.getenv("failUnknown"));
        ObjectMapper mapper = RestTests.getMapper(ignoreUnknown);
        Router router = RestTests.getRouter(token, mapper);

        return userService = new UserService(router);
    }

    @Test
    public void testGetCurrentUser() {
        getUserService().getCurrentUser().block();
    }

    @Test
    public void testGetUser() {
        getUserService().getUser(user).block();
    }

    @Test
    public void testGetInvalidUser() {
        try {
            getUserService().getUser(1111222).block(); // should throw ClientException
        } catch (ClientException e) {
            System.out.println("Error: " + e.toString());
        }
    }

    @Test
    public void testModifyCurrentUser() {
        UserModifyRequest req = UserModifyRequest.builder()
            .username("Discord4J 3 Test Bot")
            .build();
        getUserService().modifyCurrentUser(req).block();
    }

    @Test
    public void testGetCurrentUserGuilds() {
        getUserService().getCurrentUserGuilds(Collections.emptyMap()).then().block();
    }

    @Test
    public void testLeaveGuild() {
        // TODO
    }

    @Test
    public void testGetUserDMs() {
        getUserService().getUserDMs().then().block();
    }

    @Test
    public void testCreateDM() {
        DMCreateRequest req = DMCreateRequest.builder().recipientId(Snowflake.asString(user)).build();
        getUserService().createDM(req).block();
    }

    @Test
    public void testCreateGroupDM() {
        // TODO
    }

    @Test
    public void testGetUserConnections() {
        // TODO
    }

}
