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
package discord4j.core.event.domain;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.StageInstance;
import discord4j.discordjson.json.VoiceStateData;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

/**
 * Dispatched when a user connected to a stage channel makes a request to speak.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#voice-state-update">Voice State Update</a>
 */
public class StageRequestToSpeakEvent extends Event {

    private final GatewayDiscordClient gateway;
    private final VoiceStateData voiceStateData;

    public StageRequestToSpeakEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, VoiceStateData voiceStateData) {
        super(gateway, shardInfo);
        this.gateway = gateway;
        this.voiceStateData = voiceStateData;
    }

    /**
     * Requests to invite the {@code member} who made the initial request to join stage speakers.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the member
     *         has been invited to the speakers. If an error is received, it is emitted through the
     *         {@code Mono}.
     */
    public Mono<Void> acceptRequest() {
        return getMember().flatMap(member -> getStageInstance().flatMap(stageInstance -> stageInstance.inviteMemberToStageSpeakers(member)));
    }

    /**
     * Requests to deny the initial speak request.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the member
     *         request to speak has been denied. If an error is received, it is emitted through the
     *         {@code Mono}.
     */
    public Mono<Void> denyRequest() {
        return getMember().flatMap(member -> getStageInstance().flatMap(stageInstance -> stageInstance.moveMemberToStageAudience(member)));
    }

    /**
     * Get the stage instance for this request.
     *
     * @return A {@link Mono} where, upon successful completion, emits the
     *         {@link StageInstance} in which the request has been made
     */
    public Mono<StageInstance> getStageInstance() {
        return gateway.getStageInstanceByChannelId(Snowflake.of(voiceStateData.channelId().get()));
    }

    /**
     * Get the requesting member.
     *
     * @return A {@link Mono} where, upon successful completion, emits the
     *         {@link Member} who made this request
     */
    public Mono<Member> getMember() {
        return voiceStateData.member().toOptional().map(data -> Mono.just(new Member(gateway, data, voiceStateData.guildId().get().asLong()))).orElse(gateway.getMemberById(Snowflake.of(voiceStateData.guildId().get()), Snowflake.of(voiceStateData.userId())));
    }

    @Override
    public String toString() {
        return "StageRequestToSpeakEvent{" +
            "gateway=" + gateway +
            ", voiceStateData=" + voiceStateData +
            '}';
    }
}
