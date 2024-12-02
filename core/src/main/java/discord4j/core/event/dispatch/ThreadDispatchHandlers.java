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
package discord4j.core.event.dispatch;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.thread.*;
import discord4j.core.object.entity.ThreadMember;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.ThreadMemberData;
import discord4j.discordjson.json.gateway.*;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class ThreadDispatchHandlers {

    static Mono<? extends Event> threadCreate(DispatchContext<ThreadCreate, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().thread();
        boolean threadNewlyCreated = context.getDispatch().newlyCreated().toOptional().orElse(false);

        return Mono.just(new ThreadChannelCreateEvent(gateway, context.getShardInfo(),
                new ThreadChannel(gateway, channel), threadNewlyCreated));
    }

    static Mono<? extends Event> threadUpdate(DispatchContext<ThreadUpdate, ChannelData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().thread();
        Optional<ChannelData> oldData = context.getOldState();

        return Mono.just(new ThreadChannelUpdateEvent(gateway, context.getShardInfo(),
                new ThreadChannel(gateway, channel),
                oldData.map(old -> new ThreadChannel(gateway, old)).orElse(null)));
    }

    static Mono<? extends Event> threadDelete(DispatchContext<ThreadDelete, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().thread();

        return Mono.just(new ThreadChannelDeleteEvent(gateway, context.getShardInfo(),
                new ThreadChannel(gateway, channel)));
    }

    static Mono<? extends Event> threadListSync(DispatchContext<ThreadListSync, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();

        return Mono.just(new ThreadListSyncEvent(gateway, context.getShardInfo(), context.getDispatch()));
    }

    static Mono<? extends Event> threadMemberUpdate(DispatchContext<ThreadMemberUpdate, ThreadMemberData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ThreadMemberData member = context.getDispatch().member();
        Optional<ThreadMemberData> oldData = context.getOldState();

        return Mono.just(new ThreadMemberUpdateEvent(gateway, context.getShardInfo(),
                new ThreadMember(gateway, member),
                oldData.map(old -> new ThreadMember(gateway, old)).orElse(null)));
    }

    static Mono<? extends Event> threadMembersUpdate(DispatchContext<ThreadMembersUpdate, List<ThreadMemberData>> context) {
        GatewayDiscordClient gateway = context.getGateway();
        List<ThreadMemberData> addedMembers = context.getDispatch().addedMembers().toOptional().orElse(Collections.emptyList());
        Optional<List<ThreadMemberData>> oldData = context.getOldState();

        return Mono.just(new ThreadMembersUpdateEvent(gateway, context.getShardInfo(), context.getDispatch(),
                addedMembers.stream().map(data -> new ThreadMember(gateway, data)).collect(Collectors.toList()),
                oldData.map(list -> list.stream().map(data -> new ThreadMember(gateway, data)).collect(Collectors.toList())).orElse(null)));
    }
}
