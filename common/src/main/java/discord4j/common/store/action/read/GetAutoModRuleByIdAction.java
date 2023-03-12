package discord4j.common.store.action.read;

import discord4j.common.store.api.StoreAction;
import discord4j.discordjson.json.AutoModRuleData;

public class GetAutoModRuleByIdAction implements StoreAction<AutoModRuleData> {
    private final long guildId;
    private final long stickerId;

    GetAutoModRuleByIdAction(long guildId, long stickerId) {
        this.guildId = guildId;
        this.stickerId = stickerId;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getStickerId() {
        return stickerId;
    }
}
