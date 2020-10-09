package discord4j.common.store.layout.action.gateway;

import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.gateway.ChannelUpdate;

public class ChannelUpdateAction extends AbstractGatewayAction<ChannelData> {

    private final ChannelUpdate channelUpdate;

    public ChannelUpdateAction(int shardIndex, ChannelUpdate channelUpdate) {
        super(shardIndex);
        this.channelUpdate = channelUpdate;
    }

    public ChannelUpdate getChannelUpdate() {
        return channelUpdate;
    }
}
