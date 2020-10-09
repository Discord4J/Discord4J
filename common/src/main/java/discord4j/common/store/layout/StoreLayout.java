package discord4j.common.store.layout;

public interface StoreLayout {

    DataAccessor getDataAccessor();

    GatewayDataUpdater getGatewayDataUpdater();

    default ActionMapper getCustomActionMapper() {
        return ActionMapper.create();
    }
}
