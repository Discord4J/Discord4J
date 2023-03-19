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

import com.fasterxml.jackson.databind.JsonNode;
import discord4j.discordjson.json.ClientCredentialsGrantRequest;
import discord4j.rest.RestClient;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class ExampleOAuth2ClientCredentials {

    private static final Logger log = Loggers.getLogger(ExampleOAuth2ClientCredentials.class);
    private static final String TOKEN = System.getenv("TOKEN");
    private static final Long CLIENT_ID = Long.parseLong(System.getenv("CLIENT_ID"));
    private static final String CLIENT_SECRET = System.getenv("CLIENT_SECRET");
    private static final String APPLICATION_ID = System.getenv("APPLICATION_ID");
    private static final String GUILD_ID = System.getenv("GUILD_ID");
    private static final String COMMAND_ID = System.getenv("COMMAND_ID");

    public static void main(String[] args) {
        RestClient restClient = RestClient.create(TOKEN);
        DiscordOAuth2Client oAuth2Client = DiscordOAuth2Client.createFromCredentials(restClient,
                CLIENT_ID,
                CLIENT_SECRET,
                ClientCredentialsGrantRequest.builder()
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .scope("identify applications.commands.permissions.update")
                        .build());
        Router router = restClient.getRestResources().getRouter();
        // fetch the current permissions for COMMAND_ID in GUILD_ID
        JsonNode permissions = oAuth2Client.withAuthorizedClient(Routes.APPLICATION_COMMAND_PERMISSIONS_GET
                        .newRequest(APPLICATION_ID, GUILD_ID, COMMAND_ID))
                .map(request -> request.exchange(router))
                .flatMap(response -> response.bodyToMono(JsonNode.class))
                // ignore 404 if no perms were set for COMMAND_ID in GUILD_ID
                .onErrorResume(ClientException.isStatusCode(404), error -> Mono.empty())
                .block();
        log.info("{}", permissions);
    }
}
