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

package discord4j.rest.interaction;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.List;

/**
 * A simple way to register a set of application command definitions to guilds. Use {@link #create(RestClient, List)}
 * to build one.
 */
public class GuildCommandRegistrar {

    private static final Logger log = Loggers.getLogger(GuildCommandRegistrar.class);

    private final RestClient restClient;
    private final List<ApplicationCommandRequest> commandRequests;
    private final Mono<Long> applicationId;

    private GuildCommandRegistrar(RestClient restClient, List<ApplicationCommandRequest> commandRequests) {
        this.restClient = restClient;
        this.commandRequests = commandRequests;
        this.applicationId = restClient.getApplicationId().cache();
    }

    /**
     * Create a registrar using a list of {@link ApplicationCommandRequest} entries. Run the command registration
     * process by subscribing to {@link #registerCommands(Snowflake)}.
     *
     * @param restClient a Discord web client to perform API requests
     * @param commandRequests a list of command definitions
     * @return a registrar that can register application commands by subscribing to {@link #registerCommands(Snowflake)}
     */
    public static GuildCommandRegistrar create(RestClient restClient, List<ApplicationCommandRequest> commandRequests) {
        return new GuildCommandRegistrar(restClient, commandRequests);
    }

    /**
     * Submit the command definitions to Discord to register each application command in the given guild.
     *
     * @param guildId the guild chosen for command registration
     * @return a Flux with each command registration response from Discord if successful
     */
    public Flux<ApplicationCommandData> registerCommands(Snowflake guildId) {
        return bulkOverwriteCommands(guildId, commandRequests);
    }

    private Flux<ApplicationCommandData> bulkOverwriteCommands(Snowflake guildId,
                                                               List<ApplicationCommandRequest> requests) {
        return applicationId.flatMapMany(id -> restClient.getApplicationService()
            .bulkOverwriteGuildApplicationCommand(id, guildId.asLong(), requests)
            .doOnNext(it -> log.debug("Registered command {} at guild {}", it.name(), guildId.asLong())));
    }
}
