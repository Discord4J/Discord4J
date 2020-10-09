package discord4j.common.store;

import discord4j.common.store.layout.ActionMapper;
import discord4j.common.store.layout.StoreLayout;
import reactor.core.publisher.Mono;

public final class Store {

    private final ActionMapper actionMapper;

    private Store(ActionMapper actionMapper) {
        this.actionMapper = actionMapper;
    }

    public static Store noOp() {
        return new Store(ActionMapper.create());
    }

    public static Store fromLayout(StoreLayout layout) {
        return new Store(layoutToMapper(layout));
    }

    private static ActionMapper layoutToMapper(StoreLayout layout) {
        ActionMapper dataAccessorMapper = ActionMapper.fromDataAccessor(layout.getDataAccessor());
        ActionMapper gatewayDataUpdaterMapper = ActionMapper.fromGatewayDataUpdater(layout.getGatewayDataUpdater());
        ActionMapper customMapper = layout.getCustomActionMapper();
        return ActionMapper.aggregate(dataAccessorMapper, gatewayDataUpdaterMapper, customMapper);
    }

    /**
     * Executes the given action. The action will be routed based on the concrete type of the action, and handled
     * according to the layout given when creating this {@link Store}. If the concrete type of the action is unknown
     * and no custom mapping was defined for it, it will return empty.
     *
     * @param action the action to execute
     * @param <R>    the return type of the action
     * @return a {@link Mono} where, upon successful completion, emits the result produced by the execution of the
     * action, if any. If an error is received, it is emitted through the {@link Mono}.
     */
    public <R> Mono<R> execute(StoreAction<R> action) {
        return Mono.justOrEmpty(actionMapper.findHandlerForAction(action))
                .flatMap(h -> h.apply(action));
    }
}
