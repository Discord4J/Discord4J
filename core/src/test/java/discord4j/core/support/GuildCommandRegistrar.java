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

package discord4j.core.support;

import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.List;

public class GuildCommandRegistrar {

    private static final Logger log = Loggers.getLogger(GuildCommandRegistrar.class);

    private final RestClient restClient;
    private final long guildId;
    private final /*~~>*/List<ApplicationCommandRequest> commandRequests;
    private final Mono<Long> applicationId;

    private GuildCommandRegistrar(RestClient restClient, long guildId,
                                  /*~~>*/List<ApplicationCommandRequest> commandRequests) {
        this.restClient = restClient;
        this.guildId = guildId;
        /*~~>*/this.commandRequests = commandRequests;
        this.applicationId = restClient.getApplicationId().cache();
    }

    public static GuildCommandRegistrar create(RestClient restClient, long guildId,
                                               /*~~>*/List<ApplicationCommandRequest> commandRequests) {
        return new GuildCommandRegistrar(restClient, guildId, commandRequests);
    }

    public Flux<ApplicationCommandData> registerCommands() {
        return bulkOverwriteCommands(commandRequests);
    }

    private Flux<ApplicationCommandData> bulkOverwriteCommands(/*~~>*/List<ApplicationCommandRequest> requests) {
        return applicationId.flatMapMany(id -> restClient.getApplicationService()
                .bulkOverwriteGuildApplicationCommand(id, guildId, requests)
                .doOnNext(it -> log.info("Registered command {} at guild {}", it.name(), guildId)));
    }
}
