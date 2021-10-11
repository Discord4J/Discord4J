package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Webhook;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

@Value.Immutable(singleton = true)
public interface WebhookMessageDeleteSpecGenerator extends VoidSpec {

    Snowflake messageId();
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class WebhookMessageDeleteMonoGenerator extends Mono<Void> implements WebhookMessageDeleteSpecGenerator {

    abstract Webhook webhook();

    @Override
    public void subscribe( CoreSubscriber<? super Void> actual) {
        webhook().executeMessageDelete(WebhookMessageDeleteSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
