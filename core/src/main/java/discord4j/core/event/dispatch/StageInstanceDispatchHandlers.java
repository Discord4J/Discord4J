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
import discord4j.core.event.domain.StageInstanceCreateEvent;
import discord4j.core.event.domain.StageInstanceDeleteEvent;
import discord4j.core.event.domain.StageInstanceUpdateEvent;
import discord4j.core.event.domain.StageRequestToSpeakEvent;
import discord4j.core.object.entity.StageInstance;
import discord4j.discordjson.json.StageInstanceData;
import discord4j.discordjson.json.VoiceStateData;
import discord4j.discordjson.json.gateway.StageInstanceCreate;
import discord4j.discordjson.json.gateway.StageInstanceDelete;
import discord4j.discordjson.json.gateway.StageInstanceUpdate;
import discord4j.discordjson.json.gateway.VoiceStateUpdateDispatch;
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

    static Mono<StageRequestToSpeakEvent> stageRequestToSpeak(DispatchContext<VoiceStateUpdateDispatch, VoiceStateData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        Optional<VoiceStateData> oldVoiceStateData = context.getOldState();
        VoiceStateUpdateDispatch voiceStateUpdate = context.getDispatch();
        VoiceStateData voiceStateData = voiceStateUpdate.voiceState();

        if (oldVoiceStateData.isPresent()
            && voiceStateData.channelId().isPresent()
            && !voiceStateData.guildId().isAbsent()
            && voiceStateData.suppress()
            && voiceStateData.requestToSpeakTimestamp().isPresent()
            && !oldVoiceStateData.flatMap(VoiceStateData::requestToSpeakTimestamp).isPresent()) {
            return Mono.just(new StageRequestToSpeakEvent(gateway, context.getShardInfo(), voiceStateData));
        } else {
            return Mono.empty();
        }
    }

}
