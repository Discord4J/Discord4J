package discord4j.common.store.action.read;

import discord4j.discordjson.json.RoleData;

public class GetGuildRolesAction implements ReadAction<RoleData> {

    private final long guildId;
    private final boolean requireComplete;

    public GetGuildRolesAction(long guildId, boolean requireComplete) {
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
