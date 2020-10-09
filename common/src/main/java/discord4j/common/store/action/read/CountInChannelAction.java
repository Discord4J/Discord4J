package discord4j.common.store.action.read;

public class CountInChannelAction implements ReadAction<Long> {

    public static final long NO_CONTAINER = -1L;

    public enum InChannelEntity {
        MESSAGES,
        VOICE_STATES
    }

    private final InChannelEntity entity;
    private final long channelId;

    public CountInChannelAction(InChannelEntity entity, long channelId) {
        this.entity = entity;
        this.channelId = channelId;
    }

    public InChannelEntity getEntity() {
        return entity;
    }

    public long getChannelId() {
        return channelId;
    }
}
