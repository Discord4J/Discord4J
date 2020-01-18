package discord4j.core.event.domain;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

//TODO: Description
public class InviteDeleteEvent extends Event {

    private final long guildId;
    private final long channelId;
    private final String code;

    public InviteDeleteEvent(DiscordClient client, long guildId, long channelId, String code) {
        super(client);
        this.guildId = guildId;
        this.channelId = channelId;
        this.code = code;
    }

    /**
     * Gets the {@link Snowflake} ID of the guild that had a invite created in this event.
     *
     * @return The ID of the guild involved.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the {@link Guild} that had a invite created in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved in the event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the {@link Snowflake} ID of the channel invite belongs to.
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

    @Override
    public String toString() {
        return "InviteDeleteEvent{" +
            "guildId=" + guildId +
            ", channelId=" + channelId +
            ", code=" + code +
            '}';
    }
}
