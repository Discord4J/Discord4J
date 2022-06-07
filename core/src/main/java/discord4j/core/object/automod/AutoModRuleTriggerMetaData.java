package discord4j.core.object.automod;

import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.AutoModTriggerMetaData;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AutoModRuleTriggerMetaData {

    /**
     * The gateway associated to this object.
     */
    private final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final AutoModTriggerMetaData data;

    public AutoModRuleTriggerMetaData(final GatewayDiscordClient gateway, final AutoModTriggerMetaData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    public GatewayDiscordClient getClient() {
        return gateway;
    }

    public List<String> getKeywordFilter() {
        return data.keywordFilter().toOptional()
                .orElse(Collections.emptyList());
    }

    public List<String> getKeywordLists() {
        return data.keywordLists().toOptional()
                .orElse(Collections.emptyList());
    }

}
