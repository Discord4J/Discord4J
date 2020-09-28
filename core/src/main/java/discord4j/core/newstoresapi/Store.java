package discord4j.core.newstoresapi;

import reactor.core.publisher.Mono;

public interface Store {

    Mono<Void> save(EntityMetadata meta, Object toSave);

    Mono<Object> find(EntityMetadata meta);

    Mono<Void> delete(EntityMetadata meta);

    Mono<Long> count(EntityMetadata meta);

    EntityPatcher getEntityPatcher();
}
