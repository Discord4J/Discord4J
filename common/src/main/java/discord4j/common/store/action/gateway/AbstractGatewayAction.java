package discord4j.common.store.action.gateway;

abstract class AbstractGatewayAction<R> implements GatewayAction<R> {

    private final int shardIndex;

    AbstractGatewayAction(int shardIndex) {
        this.shardIndex = shardIndex;
    }

    public int getShardIndex() {
        return shardIndex;
    }
}
