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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.discordjson.json.ClientCredentialsGrantRequest;
import discord4j.rest.RestClient;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * Showcase a way to work with your own developer credentials to test fetching data that requires OAuth2.
 * <p>
 * Supply the following environment variables:
 * <ul>
 *     <li>{@code TOKEN} - your bot token</li>
 *     <li>{@code CLIENT_ID} - your app client id</li>
 *     <li>{@code CLIENT_SECRET} - your app client secret</li>
 *     <li>{@code APPLICATION_ID} - an application id to retrieve command permissions</li>
 *     <li>{@code GUILD_ID} - a guild where command permissions will be retrieved</li>
 *     <li>{@code COMMAND_ID} - the command to retrieve permissions</li>
 * </ul>
 */
public class ExampleOAuth2ClientCredentials {

    private static final Logger log = Loggers.getLogger(ExampleOAuth2ClientCredentials.class);
    private static final String TOKEN = System.getenv("TOKEN");
    private static final Long CLIENT_ID = Long.parseLong(System.getenv("CLIENT_ID"));
    private static final String CLIENT_SECRET = System.getenv("CLIENT_SECRET");
    private static final Long APPLICATION_ID = Long.parseLong(System.getenv("APPLICATION_ID"));
    private static final Long GUILD_ID = Long.parseLong(System.getenv("GUILD_ID"));
    private static final Long COMMAND_ID = Long.parseLong(System.getenv("COMMAND_ID"));

    public static void main(String[] args) {
        RestClient restClient = RestClient.create(TOKEN);
        DiscordOAuth2Client oAuth2Client = DiscordOAuth2Client.createFromCredentials(restClient,
                ClientCredentialsGrantRequest.builder()
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .scope(Scope.asString(Scope.IDENTIFY, Scope.APPLICATIONS_COMMANDS_PERMISSIONS_UPDATE, Scope.CONNECTIONS))
                        .build());

        // fetch the current permissions for COMMAND_ID in GUILD_ID
        // ignore 404 if no perms were set for COMMAND_ID in GUILD_ID
        oAuth2Client.getApplicationCommandPermissions(APPLICATION_ID, GUILD_ID, COMMAND_ID)
                .onErrorResume(ClientException.isStatusCode(404), error -> Mono.empty())
                .doOnSuccess(data -> {
                    if (data == null) {
                        log.info("No permissions set");
                    } else {
                        log.info("{}", data);
                    }
                })
                .block();

        ObjectMapper mapper = restClient.getRestResources().getJacksonResources().getObjectMapper();

        // print my own connections
        oAuth2Client.getUserConnections()
                .doOnNext(it -> {
                    try {
                        log.info("{}", mapper.writeValueAsString(it));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .blockLast();
    }
}
