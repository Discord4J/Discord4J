package discord4j.common.store;

import discord4j.common.store.layout.StoreLayout;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public final class Store {

    private final ActionMapper actionMapper;

    private Store(ActionMapper actionMapper) {
        this.actionMapper = actionMapper;
    }

    public static Store fromLayout(StoreLayout layout) {
        ActionMapper dataAccessorMapper = ActionMapper.fromDataAccessor(layout.getDataAccessor());
        ActionMapper gatewayDataUpdaterMapper = ActionMapper.fromGatewayDataUpdater(layout.getGatewayDataUpdater());
        ActionMapper customMapper = layout.getCustomActionMapper();
        return new Store(ActionMapper.aggregate(dataAccessorMapper, gatewayDataUpdaterMapper, customMapper));
    }

    @SuppressWarnings("unchecked")
    public <R> Mono<R> execute(StoreAction<R> action) {
        Function<StoreAction<?>, ? extends Mono<?>> handler = actionMapper.get(action.getClass());
        return Mono.justOrEmpty(handler).map(h -> (R) h.apply(action));
    }
}
