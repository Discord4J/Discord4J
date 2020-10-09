package discord4j.common.store.layout.action.gateway;

import discord4j.discordjson.json.gateway.ChannelCreate;

public class ChannelCreateAction extends AbstractGatewayAction<Void> {

    private final ChannelCreate channelCreate;

    public ChannelCreateAction(int shardIndex, ChannelCreate channelCreate) {
        super(shardIndex);
        this.channelCreate = channelCreate;
    }

    public ChannelCreate getChannelCreate() {
        return channelCreate;
    }
}
