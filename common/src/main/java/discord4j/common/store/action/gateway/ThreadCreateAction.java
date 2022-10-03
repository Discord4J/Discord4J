package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.gateway.ThreadCreate;

public class ThreadCreateAction extends ShardAwareAction<Void> {
    private final ThreadCreate threadCreate;

    ThreadCreateAction(int shardIndex, ThreadCreate threadCreate) {
        super(shardIndex);
        this.threadCreate = threadCreate;
    }

    public ThreadCreate getThreadCreate() {
        return threadCreate;
    }
}
