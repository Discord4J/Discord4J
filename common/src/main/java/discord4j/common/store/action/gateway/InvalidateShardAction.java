package discord4j.common.store.action.gateway;

import discord4j.common.store.layout.InvalidationCause;

public class InvalidateShardAction extends AbstractGatewayAction<Void> {

    private final InvalidationCause cause;

    public InvalidateShardAction(int shardIndex, InvalidationCause cause) {
        super(shardIndex);
        this.cause = cause;
    }

    public InvalidationCause getCause() {
        return cause;
    }
}
