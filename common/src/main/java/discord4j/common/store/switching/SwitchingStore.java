package discord4j.common.store.switching;

import discord4j.store.api.wip.Store;
import discord4j.store.api.wip.StoreAction;
import discord4j.store.api.wip.noop.NoOpStore;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class SwitchingStore implements Store {

    private final List<ConditionStore> conditionStores;
    private final Store fallbackStore;

    private SwitchingStore(List<ConditionStore> conditionStores, Store fallbackStore) {
        this.conditionStores = Collections.unmodifiableList(conditionStores);
        this.fallbackStore = fallbackStore == null ? NoOpStore.create() : fallbackStore;
    }

    @Override
    public <R> Mono<R> execute(StoreAction<R> action) {
        return Flux.fromIterable(conditionStores)
                .filter(conditionStore -> conditionStore.condition.test(action))
                .map(conditionStore -> conditionStore.store)
                .next()
                .defaultIfEmpty(fallbackStore)
                .flatMap(store -> store.execute(action));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final List<ConditionStore> conditionStores = new LinkedList<>();
        private Store fallbackStore;

        public final Builder useIfActionMatches(Store store, Predicate<StoreAction<?>> condition) {
            Objects.requireNonNull(store);
            Objects.requireNonNull(condition);
            conditionStores.add(new ConditionStore(store, condition));
            return this;
        }

        @SafeVarargs
        public final Builder useIfActionOfType(Store store, Class<? extends StoreAction<?>> actionType,
                                         Class<? extends StoreAction<?>>... moreTypes) {
            Objects.requireNonNull(store);
            Objects.requireNonNull(actionType);
            Objects.requireNonNull(moreTypes);
            conditionStores.add(new ConditionStore(store, action -> actionType.isInstance(action)
                || Arrays.stream(moreTypes).anyMatch(type -> type.isInstance(action))));
            return this;
        }

        /**
         * Sets the Store to use when no other store matches the action.
         *
         * @param fallbackStore the store to use as fallback. A null value is equivalent to a {@link NoOpStore}
         * @return this builder
         */
        public final Builder setFallback(@Nullable Store fallbackStore) {
            this.fallbackStore = fallbackStore;
            return this;
        }

        public final SwitchingStore build() {
            return new SwitchingStore(conditionStores, fallbackStore);
        }
    }

    private static class ConditionStore {

        private final Store store;
        private final Predicate<StoreAction<?>> condition;

        ConditionStore(Store store, Predicate<StoreAction<?>> condition) {
            this.store = store;
            this.condition = condition;
        }

        @Override
        public String toString() {
            return "ConditionStore{" +
                "store=" + store +
                ", condition=" + condition +
                '}';
        }
    }
}
