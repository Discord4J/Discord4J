package discord4j.core.event.domain;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.time.Instant;

//TODO: Description
public class InviteCreateEvent extends Event {

    private final long guildId;
    private final long channelId;
    private final String code;
    private User inviter;
    private final Instant createdAt;
    private final int uses;
    private final int maxUses;
    private final int maxAge;
    private final boolean temporary;


    public InviteCreateEvent(DiscordClient client, long guildId, long channelId, String code, User inviter, Instant createdAt, int uses, int maxUses, int maxAge, boolean temporary) {
        super(client);
        this.guildId = guildId;
        this.channelId = channelId;
        this.code = code;
        this.inviter = inviter;
        this.createdAt = createdAt;
        this.uses = uses;
        this.maxAge = maxAge;
        this.maxUses = maxUses;
        this.temporary = temporary;
    }

    /**
     * Gets the {@link Snowflake} ID of the guild that had a invite deleted in this event.
     *
     * @return The ID of the guild involved.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the {@link Guild} that had a invite deleted in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved in the event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the {@link Snowflake} ID of the channel the webhook belongs to.
     *
     * @return The ID of the channel involved.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Gets the invite code (unique ID).
     *
     * @return The invite code (unique ID).
     */
    public final String getCode() {
        return code;
    }

    /**
     * Gets the ID of the target user this invite is associated to, if present.
     *
     * @return The ID of the target user this invite is associated to, if present.
     */
    public final Snowflake getInviterId() {
        return inviter.getId();
    }

    /**
     * Retrieve the inviter user associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User inviter user}
     * associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    public final User getInviter() {
        return inviter;
    }

    public Integer getUses() {
        return uses;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public Boolean isTemporary() {
        return temporary;
    }

    @Override
    public String toString() {
        return "InviteCreateEvent{" +
            "code='" + code + '\'' +
            ", guildId=" + guildId +
            ", channelId=" + channelId +
            ", inviter=" + inviter +
            ", uses=" + uses +
            ", maxUses=" + maxUses +
            ", maxAge=" + maxAge +
            ", temporary=" + temporary +
            ", createdAt='" + createdAt + '\'' +
            '}';
    }
}
