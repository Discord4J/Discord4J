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
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalCommandRegistrar {

    private static final Logger log = Loggers.getLogger(GlobalCommandRegistrar.class);

    private final RestClient restClient;
    private final List<ApplicationCommandRequest> commandRequests;
    private final Mono<Long> applicationId;

    private GlobalCommandRegistrar(RestClient restClient, List<ApplicationCommandRequest> commandRequests) {
        this.restClient = restClient;
        this.commandRequests = commandRequests;
        this.applicationId = restClient.getApplicationId().cache();
    }

    public static GlobalCommandRegistrar create(RestClient restClient, List<ApplicationCommandRequest> commandRequests) {
        return new GlobalCommandRegistrar(restClient, commandRequests);
    }

    public Mono<Void> registerCommands() {
        // get already existing commands to compare
        return getExistingCommands()
                .flatMap(existing -> {
                    List<Mono<?>> actions = new ArrayList<>();

                    // get commands from this source
                    Map<String, ApplicationCommandRequest> commands = new HashMap<>();
                    for (ApplicationCommandRequest request : commandRequests) {
                        commands.put(request.name(), request);

                        // prepare to register new commands from source
                        if (!existing.containsKey(request.name())) {
                            actions.add(createCommand(request));
                        }
                    }

                    // check if any commands have been deleted or changed
                    for (ApplicationCommandData existingCommand : existing.values()) {
                        long existingCommandId = Long.parseLong(existingCommand.id());
                        if (commands.containsKey(existingCommand.name())) {
                            ApplicationCommandRequest command = commands.get(existingCommand.name());
                            if (isChanged(existingCommand, command)) {
                                actions.add(modifyCommand(existingCommandId, command));
                            }
                        } else {
                            // removed source command, delete remote command
                            actions.add(deleteCommand(existingCommandId, existingCommand));
                        }
                    }

                    return Mono.when(actions);
                });
    }

    private Mono<ApplicationCommandData> createCommand(ApplicationCommandRequest request) {
        return applicationId.flatMap(id -> restClient.getApplicationService()
                .createGlobalApplicationCommand(id, request)
                .doOnNext(it -> log.info("Created global command {}", request.name())));
    }

    private Mono<ApplicationCommandData> modifyCommand(long commandId, ApplicationCommandRequest request) {
        return applicationId.flatMap(id -> restClient.getApplicationService()
                .modifyGlobalApplicationCommand(id, commandId, request)
                .doOnNext(it -> log.info("Updated global command {}", request.name())));
    }

    private Mono<Void> deleteCommand(long commandId, ApplicationCommandData request) {
        return applicationId.flatMap(id -> restClient.getApplicationService()
                .deleteGlobalApplicationCommand(id, commandId)
                .doOnTerminate(() -> log.info("Deleted global command {}", request.name())));
    }

    private boolean isChanged(ApplicationCommandData existingCommand, ApplicationCommandRequest command) {
        return command.description().toOptional().map(value -> !existingCommand.description().equals(value)).orElse(false)
                || !existingCommand.options().equals(command.options())
                || existingCommand.defaultPermission().toOptional().orElse(true) != command.defaultPermission().toOptional().orElse(true);
    }

    private Mono<Map<String, ApplicationCommandData>> getExistingCommands() {
        return applicationId.flatMap(id -> restClient.getApplicationService()
                .getGlobalApplicationCommands(id)
                .collectMap(ApplicationCommandData::name));
    }
}
