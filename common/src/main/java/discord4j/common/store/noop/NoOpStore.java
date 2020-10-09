package discord4j.common.store.noop;

import discord4j.common.store.Store;
import discord4j.common.store.StoreAction;
import reactor.core.publisher.Mono;

/**
 * Implementation of {@link Store} that does nothing.
 */
public class NoOpStore implements Store {

    private NoOpStore() {
    }

    public static NoOpStore create() {
        return new NoOpStore();
    }

    @Override
    public <R> Mono<R> execute(StoreAction<R> action) {
        return Mono.empty();
    }
}
