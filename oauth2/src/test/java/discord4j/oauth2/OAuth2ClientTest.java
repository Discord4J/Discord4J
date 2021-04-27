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

package discord4j.oauth2;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OAuth2ClientTest {

    private static final long channel = Snowflake.asLong(System.getenv("channel"));
    private static final long user = Snowflake.asLong(System.getenv("member"));

    private OAuth2Client oAuth2Client;

    @BeforeAll
    public void setup() {
        oAuth2Client = OAuth2Client.createFromCredentials(spec -> spec.addScope(Scope.CONNECTIONS)
                .addScope(Scope.EMAIL)
                .addScope(Scope.IDENTIFY)
                .addScope(Scope.GUILDS)
                .addScope(Scope.GDM_JOIN))
                .setClientId(Long.parseLong(System.getenv("id")))
                .setClientSecret(System.getenv("secret"))
                .build();
    }

    @Test
    public void testConnections() {
        oAuth2Client.getUserService()
                .getUserConnections()
                .collectList()
                .block();
    }

    @Test
    public void testSelf() {
        UserData self = oAuth2Client.getSelf().block();
        assertFalse(self.email().isAbsent());
        assertTrue(self.email().get().isPresent());
    }

    @Test
    public void testGuilds() {
        oAuth2Client.getGuilds().collectList().block();
    }

    @Test
    public void testAddToGroupDM() {
        String bearerToken = oAuth2Client.getToken(spec -> spec.setCode(System.getenv("code"))
                .setRedirectUri("http://localhost"))
                .block()
                .asString();
        GroupAddRecipientRequest req = GroupAddRecipientRequest.builder()
                .accessToken(bearerToken)
                .nick("test")
                .build();
        oAuth2Client.getChannelService().addGroupDMRecipient(channel, user, req).block();
        oAuth2Client.getChannelService().deleteGroupDMRecipient(channel, user).block();
    }
}
