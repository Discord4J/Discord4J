package discord4j.common.store.action.read;

import discord4j.discordjson.json.MemberData;

public class GetGuildMembersAction implements ReadAction<MemberData> {

    private final long guildId;
    private final boolean requireComplete;

    public GetGuildMembersAction(long guildId, boolean requireComplete) {
        this.guildId = guildId;
        this.requireComplete = requireComplete;
    }

    public long getGuildId() {
        return guildId;
    }

    public boolean requireComplete() {
        return requireComplete;
    }
}
