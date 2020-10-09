package discord4j.common.store.layout.action.gateway;

import discord4j.common.store.util.PresenceAndUserData;
import discord4j.discordjson.json.gateway.PresenceUpdate;

public class PresenceUpdateAction extends AbstractGatewayAction<PresenceAndUserData> {

    private final PresenceUpdate presenceUpdate;

    public PresenceUpdateAction(int shardIndex, PresenceUpdate presenceUpdate) {
        super(shardIndex);
        this.presenceUpdate = presenceUpdate;
    }

    public PresenceUpdate getPresenceUpdate() {
        return presenceUpdate;
    }
}
