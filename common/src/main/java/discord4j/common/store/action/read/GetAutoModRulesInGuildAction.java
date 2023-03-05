package discord4j.common.store.action.read;

import discord4j.common.store.api.StoreAction;
import discord4j.discordjson.json.AutoModRuleData;

public class GetAutoModRulesInGuildAction implements StoreAction<AutoModRuleData> {

    private final long guildId;

    GetAutoModRulesInGuildAction(long guildId) {
        this.guildId = guildId;
    }

    public long getGuildId() {
        return guildId;
    }

}
