package discord4j.core.spec;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

abstract class ConfigurableRequest<T, B, SELF extends ConfigurableRequest<T, B, SELF>> extends Mono<T> {

    final Supplier<B> requestBuilder;

    ConfigurableRequest(Supplier<B> requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    abstract SELF withBuilder(UnaryOperator<B> f);

    abstract Mono<T> getRequest();

    Supplier<B> apply(UnaryOperator<B> f) {
        return () -> f.apply(requestBuilder.get());
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        getRequest().subscribe(actual);
    }
}
