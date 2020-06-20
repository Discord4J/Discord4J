package discord4j.gateway.retry;

import discord4j.common.close.CloseStatus;
import discord4j.common.close.DisconnectBehavior;

public class ClosingStateChange extends GatewayStateChange {
    private final DisconnectBehavior behavior;
    private final CloseStatus status;

    protected ClosingStateChange(DisconnectBehavior behavior, CloseStatus status) {
        super(State.DISCONNECTED, 0, null);
        this.behavior = behavior;
        this.status = status;
    }

    public DisconnectBehavior getBehavior() {
        return behavior;
    }

    public CloseStatus getStatus() {
        return status;
    }
}
