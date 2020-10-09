package discord4j.common.store.layout;

import discord4j.common.store.ActionMapper;

public interface StoreLayout {

    DataAccessor getDataAccessor();

    GatewayDataUpdater getGatewayDataUpdater();

    ActionMapper getCustomActionMapper();
}
