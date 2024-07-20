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

import discord4j.discordjson.json.*;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class ApplicationService extends RestService {

    public ApplicationService(Router router) {
        super(router);
    }

    public Mono<ApplicationInfoData> getCurrentApplicationInfo() {
        return Routes.APPLICATION_INFO_GET.newRequest()
                .exchange(getRouter())
                .bodyToMono(ApplicationInfoData.class);
    }

    public Flux<ApplicationCommandData> getGlobalApplicationCommands(long applicationId, boolean withLocalizations) {
        return Routes.GLOBAL_APPLICATION_COMMANDS_GET.newRequest(applicationId)
                .query("with_localizations", withLocalizations)
                .exchange(getRouter())
                .bodyToMono(ApplicationCommandData[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Mono<ApplicationInfoData> modifyCurrentApplicationInfo(ApplicationInfoRequest request) {
        return Routes.APPLICATION_INFO_MODIFY.newRequest()
            .body(request)
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

    public Flux<ApplicationCommandData> bulkOverwriteGlobalApplicationCommand(long applicationId, List<ApplicationCommandRequest> requests) {
        return Routes.GLOBAL_APPLICATION_COMMANDS_BULK_OVERWRITE.newRequest(applicationId)
                .body(requests)
                .exchange(getRouter())
                .bodyToMono(ApplicationCommandData[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Mono<ApplicationCommandData> getGlobalApplicationCommand(long applicationId, long commandId) {
        return Routes.GLOBAL_APPLICATION_COMMAND_GET.newRequest(applicationId, commandId)
            .exchange(getRouter())
            .bodyToMono(ApplicationCommandData.class);
    }

    public Mono<ApplicationCommandData> modifyGlobalApplicationCommand(long applicationId, long commandId,
                                                                       ApplicationCommandRequest request) {
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

    public Mono<ApplicationCommandData> createGuildApplicationCommand(long applicationId, long guildId,
                                                                      ApplicationCommandRequest request) {
        return Routes.GUILD_APPLICATION_COMMANDS_CREATE.newRequest(applicationId, guildId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(ApplicationCommandData.class);
    }

    public Flux<ApplicationCommandData> bulkOverwriteGuildApplicationCommand(long applicationId, long guildId,
                                                                             List<ApplicationCommandRequest> requests) {
        return Routes.GUILD_APPLICATION_COMMANDS_BULK_OVERWRITE.newRequest(applicationId, guildId)
                .body(requests)
                .exchange(getRouter())
                .bodyToMono(ApplicationCommandData[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Mono<ApplicationCommandData> getGuildApplicationCommand(long applicationId, long guildId, long commandId) {
        return Routes.GUILD_APPLICATION_COMMAND_GET.newRequest(applicationId, guildId, commandId)
            .exchange(getRouter())
            .bodyToMono(ApplicationCommandData.class);
    }

    public Mono<ApplicationCommandData> modifyGuildApplicationCommand(long applicationId, long guildId, long commandId,
                                                                      ApplicationCommandRequest request) {
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

    public Flux<GuildApplicationCommandPermissionsData> getGuildApplicationCommandPermissions(long applicationId,
                                                                                              long guildId) {
        return Routes.GUILD_APPLICATION_COMMAND_PERMISSIONS_GET.newRequest(applicationId, guildId)
                .exchange(getRouter())
                .bodyToMono(GuildApplicationCommandPermissionsData[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Mono<GuildApplicationCommandPermissionsData> getApplicationCommandPermissions(long applicationId, long guildId,
                                                                                         long commandId) {
        return Routes.APPLICATION_COMMAND_PERMISSIONS_GET.newRequest(applicationId, guildId, commandId)
                .exchange(getRouter())
                .bodyToMono(GuildApplicationCommandPermissionsData.class);
    }

    public Mono<GuildApplicationCommandPermissionsData> modifyApplicationCommandPermissions(long applicationId, long guildId,
                                                          long commandId,
                                                          ApplicationCommandPermissionsRequest request) {
        return Routes.APPLICATION_COMMAND_PERMISSIONS_MODIFY.newRequest(applicationId, guildId, commandId)
                .body(request)
                .exchange(getRouter())
                .bodyToMono(GuildApplicationCommandPermissionsData.class);
    }

    public Flux<GuildApplicationCommandPermissionsData> bulkModifyApplicationCommandPermissions(long applicationId, long guildId,
                                                              List<PartialGuildApplicationCommandPermissionsData> permissions) {
        return Routes.APPLICATION_COMMAND_PERMISSIONS_BULK_MODIFY.newRequest(applicationId, guildId)
            .body(permissions)
            .exchange(getRouter())
            .bodyToMono(GuildApplicationCommandPermissionsData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Flux<ApplicationRoleConnectionMetadataData> getApplicationRoleConnectionMetadata(long applicationId) {
        return Routes.APPLICATION_ROLE_CONNECTION_METADATA_GET.newRequest(applicationId)
                .exchange(getRouter())
                .bodyToMono(ApplicationRoleConnectionMetadataData[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Flux<ApplicationRoleConnectionMetadataData> modifyApplicationRoleConnectionMetadata(long applicationId, List<ApplicationRoleConnectionMetadataData> request) {
        return Routes.APPLICATION_ROLE_CONNECTION_METADATA_MODIFY.newRequest(applicationId)
                .body(request)
                .exchange(getRouter())
                .bodyToMono(ApplicationRoleConnectionMetadataData[].class)
                .flatMapMany(Flux::fromArray);
    }

}
