package discord4j.core.event.domain;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.StageInstance;
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
    private final StageInstance stageInstance;
    private final Member member;

    public StageRequestToSpeakEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, final StageInstance stageInstance, final Member member) {
        super(gateway, shardInfo);
        this.gateway = gateway;
        this.stageInstance = stageInstance;
        this.member = member;
    }

    /**
     * Requests to invite the {@code member} who made the initial request to join stage speakers.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the member
     *         has been invited to the speakers. If an error is received, it is emitted through the
     *         {@code Mono}.
     */
    public Mono<Void> acceptRequest() {
        return stageInstance.inviteMemberToStageSpeakers(member);
    }

    /**
     * Requests to deny the initial speak request.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the member
     *         request to speak has been denied. If an error is received, it is emitted through the
     *         {@code Mono}.
     */
    public Mono<Void> denyRequest() {
        return stageInstance.moveMemberToStageAudience(member);
    }

    public GatewayDiscordClient getGateway() {
        return gateway;
    }

    /**
     * Get the stage instance for this request.
     *
     * @return The {@link StageInstance} in which the request has been made
     */
    public StageInstance getStageInstance() {
        return stageInstance;
    }

    /**
     * Get the requesting member.
     *
     * @return The {@link Member} who made this request
     */
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
