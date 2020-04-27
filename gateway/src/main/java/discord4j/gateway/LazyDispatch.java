package discord4j.gateway;

import discord4j.discordjson.json.gateway.PayloadData;
import discord4j.gateway.json.GatewayPayload;
import reactor.util.annotation.Nullable;

public class LazyDispatch<T extends PayloadData> {

    @Nullable
    private GatewayPayload<T> source;
    @Nullable
    private T data;

    private boolean shardAware;

    public LazyDispatch(@Nullable GatewayPayload<T> source, @Nullable T data, boolean shardAware) {
        this.source = source;
        this.data = data;
        this.shardAware = shardAware;
    }

    public LazyDispatch(@Nullable GatewayPayload<T> source, @Nullable T data) {
        this(source, data, false);
    }

    public boolean isShardAware() {
        return shardAware;
    }

    public boolean isFromGateway() {
        return source != null;
    }

    @Nullable
    public T getData() {
        if (isFromGateway()) {
            return source.getData();
        } else {
            return data;
        }
    }

    @Nullable
    public GatewayPayload<T> getSource() {
        return source;
    }
}
