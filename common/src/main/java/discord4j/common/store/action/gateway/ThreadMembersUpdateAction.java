package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.ThreadMemberData;
import discord4j.discordjson.json.gateway.ThreadMembersUpdate;

import java.util.List;

public class ThreadMembersUpdateAction extends ShardAwareAction<List<ThreadMemberData>> {
    private final ThreadMembersUpdate threadMembersUpdate;

    ThreadMembersUpdateAction(int shardIndex, ThreadMembersUpdate threadMembersUpdate) {
        super(shardIndex);
        this.threadMembersUpdate = threadMembersUpdate;
    }

    public ThreadMembersUpdate getThreadMembersUpdate() {
        return threadMembersUpdate;
    }
}
