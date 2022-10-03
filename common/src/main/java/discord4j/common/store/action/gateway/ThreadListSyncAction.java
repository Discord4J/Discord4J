package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.gateway.ThreadListSync;

public class ThreadListSyncAction extends ShardAwareAction<Void> {
    private final ThreadListSync threadListSync;

    ThreadListSyncAction(int shardIndex, ThreadListSync threadListSync) {
        super(shardIndex);
        this.threadListSync = threadListSync;
    }

    public ThreadListSync getThreadListSync() {
        return threadListSync;
    }
}
