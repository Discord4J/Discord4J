package discord4j.core.event.dispatch;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.StageInstanceCreateEvent;
import discord4j.core.event.domain.StageInstanceDeleteEvent;
import discord4j.core.event.domain.StageInstanceUpdateEvent;
import discord4j.core.event.domain.StageRequestToSpeakEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.StageInstance;
import discord4j.discordjson.json.StageInstanceData;
import discord4j.discordjson.json.VoiceStateData;
import discord4j.discordjson.json.gateway.StageInstanceCreate;
import discord4j.discordjson.json.gateway.StageInstanceDelete;
import discord4j.discordjson.json.gateway.StageInstanceUpdate;
import discord4j.discordjson.json.gateway.VoiceStateUpdate;
import reactor.core.publisher.Mono;

import java.util.Optional;

class StageInstanceDispatchHandlers {

    static Mono<StageInstanceCreateEvent> stageInstanceCreate(DispatchContext<StageInstanceCreate, StageInstanceData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        StageInstanceData stageInstanceData = context.getDispatch().stageInstance();
        StageInstance stageInstance = new StageInstance(gateway, stageInstanceData);

        return Mono.just(new StageInstanceCreateEvent(gateway, context.getShardInfo(), stageInstance));
    }

    static Mono<StageInstanceUpdateEvent> stageInstanceUpdate(DispatchContext<StageInstanceUpdate, StageInstanceData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        StageInstanceData currentData = context.getDispatch().stageInstance();
        StageInstance current = new StageInstance(gateway, currentData);

        return Mono.just(new StageInstanceUpdateEvent(gateway, context.getShardInfo(), current, context.getOldState()
            .map(old -> new StageInstance(gateway, old)).orElse(null)));
    }

    static Mono<StageInstanceDeleteEvent> stageInstanceDelete(DispatchContext<StageInstanceDelete, StageInstanceData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        StageInstanceData stageInstanceData = context.getDispatch().stageInstance();
        StageInstance stageInstance = new StageInstance(gateway, stageInstanceData);

        return Mono.just(new StageInstanceDeleteEvent(gateway, context.getShardInfo(), stageInstance));
    }

    static Mono<StageRequestToSpeakEvent> stageRequestToSpeak(DispatchContext<VoiceStateUpdate, VoiceStateData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        Optional<VoiceStateData> oldVoiceStateData = context.getOldState();
        VoiceStateUpdate voiceStateUpdate = context.getDispatch();
        VoiceStateData voiceStateData = voiceStateUpdate.voiceStateData();

        if (oldVoiceStateData.isPresent()
            && voiceStateData.channelId().isPresent()
            && !voiceStateData.guildId().isAbsent()
            && voiceStateData.suppress()
            && voiceStateData.requestToSpeakTimestamp().isPresent()
            && !oldVoiceStateData.flatMap(VoiceStateData::requestToSpeakTimestamp).isPresent()) {

            Snowflake channelId = voiceStateData.channelId().map(Snowflake::of).get();
            Snowflake guildId = Snowflake.of(voiceStateData.guildId().get());
            Mono<Member> memberMono = voiceStateData.member().toOptional().map(data -> Mono.just(new Member(gateway, data, guildId.asLong()))).orElse(gateway.getMemberById(guildId, Snowflake.of(voiceStateData.userId())));

            return gateway.getStageInstanceByChannelId(channelId).flatMap(stageInstance -> memberMono.flatMap(member -> Mono.just(new StageRequestToSpeakEvent(gateway, context.getShardInfo(), stageInstance, member))));
        } else {
            return Mono.empty();
        }
    }

}
