package discord4j.core.event.domain;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.StageInstance;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

public class StageRequestToSpeakEvent extends Event {

    private final GatewayDiscordClient gateway;
    private final StageInstance stageInstance;
    private final Member member;

    public StageRequestToSpeakEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, final StageInstance stageInstance, final Member member) {
        super(gateway, shardInfo);
        this.gateway = gateway;
        this.stageInstance = stageInstance;
        this.member = member;
    }

    public Mono<Void> acceptRequest() {
        return stageInstance.inviteMemberToStageSpeakers(member);
    }

    public Mono<Void> denyRequest() {
        return stageInstance.moveMemberToStageAudience(member);
    }

    public StageInstance getStageInstance() {
        return stageInstance;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public String toString() {
        return "StageRequestToSpeakEvent{" +
            "stageInstance=" + stageInstance +
            ", member=" + member +
            '}';
    }
}
