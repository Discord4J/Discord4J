package discord4j.core.spec;

import reactor.util.annotation.Nullable;

import java.util.function.Supplier;

abstract class AuditableRequest<T, B, SELF extends AuditableRequest<T, B, SELF>> extends ConfigurableRequest<T, B, SELF> {

    @Nullable
    final String reason;

    public AuditableRequest(Supplier<B> requestBuilder, @Nullable String reason) {
        super(requestBuilder);
        this.reason = reason;
    }

    abstract SELF withReason(String reason);
}
