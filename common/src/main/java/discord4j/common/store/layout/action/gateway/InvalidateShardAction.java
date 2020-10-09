package discord4j.common.store.layout.action.gateway;

public class InvalidateShardAction extends AbstractGatewayAction<Void> {

    public enum Cause {
        HARD_RECONNECT, LOGOUT;
    }

    private final Cause cause;

    public InvalidateShardAction(int shardIndex, Cause cause) {
        super(shardIndex);
        this.cause = cause;
    }

    public Cause getCause() {
        return cause;
    }
}
