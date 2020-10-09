package discord4j.common.store.action.read;

import discord4j.discordjson.json.RoleData;

public class GetRoleByIdAction implements ReadAction<RoleData> {

    private final long guildId;
    private final long roleId;

    public GetRoleByIdAction(long guildId, long roleId) {
        this.guildId = guildId;
        this.roleId = roleId;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getRoleId() {
        return roleId;
    }
}
