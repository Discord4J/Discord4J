package discord4j.common.store.layout.action.read;

import java.util.OptionalLong;

public class CountAction implements ReadAction<Long> {

    public static final long NO_CONTAINER = -1L;

    public enum CountableEntity {
        CHANNELS,
        EMOJIS,
        GUILDS,
        MEMBERS,
        MESSAGES,
        PRESENCES,
        ROLES,
        USERS;
    }

    private final CountableEntity entity;
    private final long containerId;

    public CountAction(CountableEntity entity) {
        this(entity, NO_CONTAINER);
    }

    public CountAction(CountableEntity entity, long containerId) {
        this.entity = entity;
        this.containerId = containerId;
    }

    public CountableEntity getEntity() {
        return entity;
    }

    public OptionalLong getContainerId() {
        return containerId == NO_CONTAINER ? OptionalLong.empty() : OptionalLong.of(containerId);
    }
}
