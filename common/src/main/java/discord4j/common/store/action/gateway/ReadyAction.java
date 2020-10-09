package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.gateway.Ready;

public class ReadyAction extends AbstractGatewayAction<Void> {

    private final Ready ready;

    public ReadyAction(int shardIndex, Ready ready) {
        super(shardIndex);
        this.ready = ready;
    }

    public Ready getReady() {
        return ready;
    }
}
