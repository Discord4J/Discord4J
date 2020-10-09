package discord4j.common.store;

import discord4j.common.store.layout.StoreLayout;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class Store {

    private final List<ConditionMapper> conditionMappers;
    private final ActionMapper fallbackMapper;

    private Store(List<ConditionMapper> conditionMappers, ActionMapper fallbackMapper) {
        this.conditionMappers = conditionMappers;
        this.fallbackMapper = fallbackMapper;
    }

    public static Store noOp() {
        return new Store(Collections.emptyList(), ActionMapper.create());
    }

    public static Store fromLayout(StoreLayout layout) {
        return new Store(Collections.emptyList(), layoutToMapper(layout));
    }

    public static Store fromLayoutSwitcher(LayoutSwitcher layoutSwitcher) {
        List<ConditionMapper> conditionMappers = layoutSwitcher.conditionLayouts.stream()
                .map(ConditionLayout::toConditionMapper)
                .collect(Collectors.toList());
        ActionMapper fallbackMapper = layoutSwitcher.fallback == null
                ? ActionMapper.create() : layoutToMapper(layoutSwitcher.fallback);
        return new Store(conditionMappers, fallbackMapper);
    }

    private static ActionMapper layoutToMapper(StoreLayout layout) {
        ActionMapper dataAccessorMapper = ActionMapper.fromDataAccessor(layout.getDataAccessor());
        ActionMapper gatewayDataUpdaterMapper = ActionMapper.fromGatewayDataUpdater(layout.getGatewayDataUpdater());
        ActionMapper customMapper = layout.getCustomActionMapper();
        return ActionMapper.aggregate(dataAccessorMapper, gatewayDataUpdaterMapper, customMapper);
    }

    @SuppressWarnings("unchecked")
    public <R> Mono<R> execute(StoreAction<R> action) {
        ActionMapper mapper = fallbackMapper;
        if (!conditionMappers.isEmpty()) {
            mapper = conditionMappers.stream()
                    .filter(conditionMapper -> conditionMapper.condition.test(action))
                    .map(conditionMapper -> conditionMapper.actionMapper)
                    .findAny()
                    .orElse(fallbackMapper);
        }
        Function<StoreAction<?>, ? extends Mono<?>> handler = mapper.get(action.getClass());
        return Mono.justOrEmpty(handler).map(h -> (R) h.apply(action));
    }

    public static class LayoutSwitcher {

        private final List<ConditionLayout> conditionLayouts = new ArrayList<>();
        private StoreLayout fallback = null;

        private LayoutSwitcher() {
        }

        public static LayoutSwitcher create() {
            return new LayoutSwitcher();
        }

        public LayoutSwitcher useIfActionMatches(StoreLayout layout, Predicate<? super StoreAction<?>> condition) {
            Objects.requireNonNull(layout);
            Objects.requireNonNull(condition);
            conditionLayouts.add(new ConditionLayout(layout, condition));
            return this;
        }

        public LayoutSwitcher useIfActionOfType(StoreLayout layout, Class<? extends StoreAction<?>> actionType,
                                                Class<? extends StoreAction<?>>... moreTypes) {
            Objects.requireNonNull(moreTypes);
            return useIfActionMatches(layout, action -> actionType.isInstance(action)
                    || Arrays.stream(moreTypes).anyMatch(t -> t.isInstance(action)));
        }

        public LayoutSwitcher setFallback(@Nullable StoreLayout fallback) {
            this.fallback = fallback;
            return this;
        }
    }

    private static class ConditionMapper {

        final ActionMapper actionMapper;
        final Predicate<? super StoreAction<?>> condition;

        ConditionMapper(ActionMapper actionMapper, Predicate<? super StoreAction<?>> condition) {
            this.actionMapper = actionMapper;
            this.condition = condition;
        }
    }

    private static class ConditionLayout {

        final StoreLayout layout;
        final Predicate<? super StoreAction<?>> condition;

        ConditionLayout(StoreLayout layout, Predicate<? super StoreAction<?>> condition) {
            this.layout = layout;
            this.condition = condition;
        }

        ConditionMapper toConditionMapper() {
            return new ConditionMapper(layoutToMapper(layout), condition);
        }
    }
}
