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

public class GuildCommandRegistrar {

    private static final Logger log = Loggers.getLogger(GuildCommandRegistrar.class);

    private final RestClient restClient;
    private final long guildId;
    private final List<ApplicationCommandRequest> commandRequests;
    private final Mono<Long> applicationId;

    private GuildCommandRegistrar(RestClient restClient, long guildId,
                                  List<ApplicationCommandRequest> commandRequests) {
        this.restClient = restClient;
        this.guildId = guildId;
        this.commandRequests = commandRequests;
        this.applicationId = restClient.getApplicationId().cache();
    }

    public static GuildCommandRegistrar create(RestClient restClient, long guildId,
                                               List<ApplicationCommandRequest> commandRequests) {
        return new GuildCommandRegistrar(restClient, guildId, commandRequests);
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
                .createGuildApplicationCommand(id, guildId, request)
                .doOnNext(it -> log.info("Created command {} at guild {}", request.name(), guildId)));
    }

    private Mono<ApplicationCommandData> modifyCommand(long commandId, ApplicationCommandRequest request) {
        return applicationId.flatMap(id -> restClient.getApplicationService()
                .modifyGuildApplicationCommand(id, guildId, commandId, request)
                .doOnNext(it -> log.info("Updated command {} at guild {}", request.name(), guildId)));
    }

    private Mono<Void> deleteCommand(long commandId, ApplicationCommandData request) {
        return applicationId.flatMap(id -> restClient.getApplicationService()
                .deleteGuildApplicationCommand(id, guildId, commandId)
                .doOnTerminate(() -> log.info("Deleted command {} from guild {}", request.name(), guildId)));
    }

    private boolean isChanged(ApplicationCommandData existingCommand, ApplicationCommandRequest command) {
        return command.description().toOptional().map(value -> !existingCommand.description().equals(value)).orElse(false)
                || !existingCommand.options().equals(command.options())
                || existingCommand.defaultPermission().toOptional().orElse(true) != command.defaultPermission().toOptional().orElse(true);
    }

    private Mono<Map<String, ApplicationCommandData>> getExistingCommands() {
        return applicationId.flatMap(id -> restClient.getApplicationService()
                .getGuildApplicationCommands(id, guildId)
                .collectMap(ApplicationCommandData::name));
    }
}
