package discord4j.common.store.layout.action.read;

import discord4j.discordjson.json.MemberData;

public class GetMemberByIdAction implements ReadAction<MemberData> {

    private final long guildId;
    private final long userId;

    public GetMemberByIdAction(long guildId, long userId) {
        this.guildId = guildId;
        this.userId = userId;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getUserId() {
        return userId;
    }
}
