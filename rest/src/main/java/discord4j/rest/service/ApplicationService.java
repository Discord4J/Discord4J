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

import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ApplicationInfoData;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ApplicationService extends RestService {

    public ApplicationService(Router router) {
        super(router);
    }

    public Mono<ApplicationInfoData> getCurrentApplicationInfo() {
        return Routes.APPLICATION_INFO_GET.newRequest()
                .exchange(getRouter())
                .bodyToMono(ApplicationInfoData.class);
    }

    public Flux<ApplicationCommandData> getGlobalApplicationCommands(long applicationId) {
        return Routes.GLOBAL_APPLICATION_COMMANDS_GET.newRequest(applicationId)
            .exchange(getRouter())
            .bodyToMono(ApplicationCommandData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<ApplicationCommandData> createGlobalApplicationCommand(long applicationId, ApplicationCommandRequest request) {
        return Routes.GLOBAL_APPLICATION_COMMANDS_CREATE.newRequest(applicationId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(ApplicationCommandData.class);
    }

    public Mono<ApplicationCommandData> getGlobalApplicationCommand(long applicationId, long commandId) {
        return Routes.GLOBAL_APPLICATION_COMMAND_GET.newRequest(applicationId, commandId)
            .exchange(getRouter())
            .bodyToMono(ApplicationCommandData.class);
    }

    public Mono<ApplicationCommandData> modifyGlobalApplicationCommand(long applicationId, long commandId, ApplicationCommandRequest request) {
        return Routes.GLOBAL_APPLICATION_COMMAND_MODIFY.newRequest(applicationId, commandId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(ApplicationCommandData.class);
    }

    public Mono<Void> deleteGlobalApplicationCommand(long applicationId, long commandId) {
        return Routes.GLOBAL_APPLICATION_COMMAND_DELETE.newRequest(applicationId, commandId)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Flux<ApplicationCommandData> getGuildApplicationCommands(long applicationId, long guildId) {
        return Routes.GUILD_APPLICATION_COMMANDS_GET.newRequest(applicationId, guildId)
            .exchange(getRouter())
            .bodyToMono(ApplicationCommandData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<ApplicationCommandData> createGuildApplicationCommand(long applicationId, long guildId, ApplicationCommandRequest request) {
        return Routes.GUILD_APPLICATION_COMMANDS_CREATE.newRequest(applicationId, guildId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(ApplicationCommandData.class);
    }

    public Mono<ApplicationCommandData> getGuildApplicationCommand(long applicationId, long guildId, long commandId) {
        return Routes.GUILD_APPLICATION_COMMAND_GET.newRequest(applicationId, guildId, commandId)
            .exchange(getRouter())
            .bodyToMono(ApplicationCommandData.class);
    }

    public Mono<ApplicationCommandData> modifyGuildApplicationCommand(long applicationId, long guildId, long commandId, ApplicationCommandRequest request) {
        return Routes.GUILD_APPLICATION_COMMAND_MODIFY.newRequest(applicationId, guildId, commandId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(ApplicationCommandData.class);
    }

    public Mono<Void> deleteGuildApplicationCommand(long applicationId, long guildId, long commandId) {
        return Routes.GUILD_APPLICATION_COMMAND_DELETE.newRequest(applicationId, guildId, commandId)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }
}
