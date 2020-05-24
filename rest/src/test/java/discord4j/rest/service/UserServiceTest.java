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

import discord4j.discordjson.json.DMCreateRequest;
import discord4j.discordjson.json.UserModifyRequest;
import discord4j.rest.RestTests;
import discord4j.rest.http.client.ClientException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import discord4j.common.util.Snowflake;

import java.util.Collections;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "D4J_TEST_DISCORD", matches = "true")
public class UserServiceTest {

    private static final long user = Snowflake.asLong(System.getenv("member"));

    private UserService userService;

    @BeforeAll
    public void setup() {
        userService = new UserService(RestTests.defaultRouter());
    }

    @Test
    public void testGetCurrentUser() {
        userService.getCurrentUser().block();
    }

    @Test
    public void testGetUser() {
        userService.getUser(user).block();
    }

    @Test
    public void testGetInvalidUser() {
        try {
            userService.getUser(1111222).block(); // should throw ClientException
        } catch (ClientException e) {
            System.out.println("Error: " + e.toString());
        }
    }

    @Test
    public void testModifyCurrentUser() {
        UserModifyRequest req = UserModifyRequest.builder()
            .username("Discord4J 3 Test Bot")
            .build();
        userService.modifyCurrentUser(req).block();
    }

    @Test
    public void testGetCurrentUserGuilds() {
        userService.getCurrentUserGuilds(Collections.emptyMap()).then().block();
    }

    @Test
    public void testLeaveGuild() {
        // TODO
    }

    @Test
    public void testGetUserDMs() {
        userService.getUserDMs().then().block();
    }

    @Test
    public void testCreateDM() {
        DMCreateRequest req = DMCreateRequest.builder().recipientId(Snowflake.asString(user)).build();
        userService.createDM(req).block();
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
