package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.ThreadMemberData;
import discord4j.discordjson.json.gateway.ThreadMemberUpdate;

public class ThreadMemberUpdateAction extends ShardAwareAction<ThreadMemberData> {
    private final ThreadMemberUpdate threadMemberUpdate;

    ThreadMemberUpdateAction(int shardIndex, ThreadMemberUpdate threadMemberUpdate) {
        super(shardIndex);
        this.threadMemberUpdate = threadMemberUpdate;
    }

    public ThreadMemberUpdate getThreadMemberUpdate() {
        return threadMemberUpdate;
    }
}
