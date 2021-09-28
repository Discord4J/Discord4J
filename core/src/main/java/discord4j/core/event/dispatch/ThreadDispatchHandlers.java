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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class ThreadDispatchHandlers {

    static Mono<? extends Event> threadCreate(DispatchContext<ThreadCreate, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().thread();

        return Mono.just(new ThreadChannelCreateEvent(gateway, context.getShardInfo(),
                new ThreadChannel(gateway, channel)));
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
        List<ThreadMemberData> addedMembers = context.getDispatch().addedMembers();
        Optional<List<ThreadMemberData>> oldData = context.getOldState();

        return Mono.just(new ThreadMembersUpdateEvent(gateway, context.getShardInfo(), context.getDispatch(),
                addedMembers.stream().map(data -> new ThreadMember(gateway, data)).collect(Collectors.toList()),
                oldData.map(list -> list.stream().map(data -> new ThreadMember(gateway, data)).collect(Collectors.toList())).orElse(null)));
    }
}
