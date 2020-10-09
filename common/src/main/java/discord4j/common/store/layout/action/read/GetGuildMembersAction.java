package discord4j.common.store.layout.action.read;

import discord4j.common.store.util.PossiblyIncompleteList;
import discord4j.discordjson.json.MemberData;

public class GetGuildMembersAction implements ReadAction<PossiblyIncompleteList<MemberData>> {

    private final long guildId;

    public GetGuildMembersAction(long guildId) {
        this.guildId = guildId;
    }

    public long getGuildId() {
        return guildId;
    }
}
