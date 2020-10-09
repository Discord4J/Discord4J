package discord4j.common.store.action.read;

public class CountInGuildAction implements ReadAction<Long> {

    public static final long NO_CONTAINER = -1L;

    public enum InGuildEntity {
        CHANNELS,
        EMOJIS,
        MEMBERS,
        PRESENCES,
        ROLES,
        VOICE_STATES
    }

    private final InGuildEntity entity;
    private final long guildId;

    public CountInGuildAction(InGuildEntity entity, long guildId) {
        this.entity = entity;
        this.guildId = guildId;
    }

    public InGuildEntity getEntity() {
        return entity;
    }

    public long getGuildId() {
        return guildId;
    }
}
