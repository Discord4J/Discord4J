package discord4j.gateway.retry;

import discord4j.discordjson.json.gateway.Dispatch;

public class GatewayHardReconnect implements Dispatch {

    private GatewayHardReconnect() {
    }

    public static GatewayHardReconnect create() {
        return new GatewayHardReconnect();
    }

    @Override
    public String toString() {
        return "GatewayHardReconnect{}";
    }
}
