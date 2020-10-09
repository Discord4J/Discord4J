package discord4j.common.store.action.read;

public class CountTotalAction implements ReadAction<Long> {

    public static final long NO_CONTAINER = -1L;

    public enum CountableEntity {
        CHANNELS,
        EMOJIS,
        GUILDS,
        MEMBERS,
        MESSAGES,
        PRESENCES,
        ROLES,
        USERS,
        VOICE_STATES
    }

    private final CountableEntity entity;

    public CountTotalAction(CountableEntity entity) {
        this.entity = entity;
    }
    public CountableEntity getEntity() {
        return entity;
    }
}
