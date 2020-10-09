package discord4j.common.store.action.read;

import discord4j.common.store.util.PossiblyIncompleteList;
import discord4j.discordjson.json.RoleData;

public class GetGuildRolesAction implements ReadAction<PossiblyIncompleteList<RoleData>> {

    private final long guildId;

    public GetGuildRolesAction(long guildId) {
        this.guildId = guildId;
    }

    public long getGuildId() {
        return guildId;
    }
}
