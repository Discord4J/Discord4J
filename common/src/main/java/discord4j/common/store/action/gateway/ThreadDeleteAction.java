package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.gateway.ThreadDelete;

public class ThreadDeleteAction extends ShardAwareAction<ChannelData> {
    private final ThreadDelete threadDelete;

    ThreadDeleteAction(int shardIndex, ThreadDelete threadDelete) {
        super(shardIndex);
        this.threadDelete = threadDelete;
    }

    public ThreadDelete getThreadDelete() {
        return threadDelete;
    }
}
