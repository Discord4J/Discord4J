package discord4j.core.event.dispatch;

import discord4j.core.event.domain.Event;
import discord4j.discordjson.json.gateway.*;
import reactor.core.publisher.Mono;

class ThreadDispatchHandlers {

    static Mono<? extends Event> threadCreate(DispatchContext<ThreadCreate, Void> context) {
        return Mono.error(new UnsupportedOperationException("TODO"));
    }

    static Mono<? extends Event> threadUpdate(DispatchContext<ThreadUpdate, Void> context) {
        return Mono.error(new UnsupportedOperationException("TODO"));
    }

    static Mono<? extends Event> threadDelete(DispatchContext<ThreadDelete, Void> context) {
        return Mono.error(new UnsupportedOperationException("TODO"));
    }

    static Mono<? extends Event> threadListSync(DispatchContext<ThreadListSync, Void> context) {
        return Mono.error(new UnsupportedOperationException("TODO"));
    }

    static Mono<? extends Event> threadMemberUpdate(DispatchContext<ThreadMemberUpdate, Void> context) {
        return Mono.error(new UnsupportedOperationException("TODO"));
    }

    static Mono<? extends Event> threadMembersUpdate(DispatchContext<ThreadMembersUpdate, Void> context) {
        return Mono.error(new UnsupportedOperationException("TODO"));
    }
}
