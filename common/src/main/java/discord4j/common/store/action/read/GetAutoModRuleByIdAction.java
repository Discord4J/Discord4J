package discord4j.common.store.action.read;

import discord4j.common.store.api.StoreAction;
import discord4j.discordjson.json.AutoModRuleData;

public class GetAutoModRuleByIdAction implements StoreAction<AutoModRuleData> {
    private final long guildId;
    private final long autoModRuleId;

    GetAutoModRuleByIdAction(long guildId, long autoModRuleId) {
        this.guildId = guildId;
        this.autoModRuleId = autoModRuleId;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getAutoModRuleId() {
        return autoModRuleId;
    }
}
