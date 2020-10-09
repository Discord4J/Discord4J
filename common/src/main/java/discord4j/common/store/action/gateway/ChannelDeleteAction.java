package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.gateway.ChannelDelete;

public class ChannelDeleteAction extends AbstractGatewayAction<ChannelData> {

    private final ChannelDelete channelCreate;

    public ChannelDeleteAction(int shardIndex, ChannelDelete channelCreate) {
        super(shardIndex);
        this.channelCreate = channelCreate;
    }

    public ChannelDelete getChannelDelete() {
        return channelCreate;
    }
}
