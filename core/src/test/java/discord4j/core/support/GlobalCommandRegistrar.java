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

public class GlobalCommandRegistrar {

    private static final Logger log = Loggers.getLogger(GlobalCommandRegistrar.class);

    private final RestClient restClient;
    private final /*~~>*/List<ApplicationCommandRequest> commandRequests;
    private final Mono<Long> applicationId;

    private GlobalCommandRegistrar(RestClient restClient, /*~~>*/List<ApplicationCommandRequest> commandRequests) {
        this.restClient = restClient;
        /*~~>*/this.commandRequests = commandRequests;
        this.applicationId = restClient.getApplicationId().cache();
    }

    public static GlobalCommandRegistrar create(RestClient restClient, /*~~>*/List<ApplicationCommandRequest> commandRequests) {
        return new GlobalCommandRegistrar(restClient, commandRequests);
    }

    public Flux<ApplicationCommandData> registerCommands() {
        return bulkOverwriteCommands(commandRequests);
    }

    private Flux<ApplicationCommandData> bulkOverwriteCommands(/*~~>*/List<ApplicationCommandRequest> requests) {
        return applicationId.flatMapMany(id -> restClient.getApplicationService()
                .bulkOverwriteGlobalApplicationCommand(id, requests)
                .doOnNext(it -> log.info("Registered command {} globally", it.name())));
    }
}
