package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.gateway.ThreadUpdate;

public class ThreadUpdateAction extends ShardAwareAction<ChannelData> {
    private final ThreadUpdate threadUpdate;

    ThreadUpdateAction(int shardIndex, ThreadUpdate threadUpdate) {
        super(shardIndex);
        this.threadUpdate = threadUpdate;
    }

    public ThreadUpdate getThreadUpdate() {
        return threadUpdate;
    }
}
