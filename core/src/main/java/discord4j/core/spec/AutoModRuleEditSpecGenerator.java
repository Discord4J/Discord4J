package discord4j.core.spec;

import discord4j.core.object.automod.AutoModRule;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.AutoModActionData;
import discord4j.discordjson.json.AutoModRuleModifyRequest;
import discord4j.discordjson.json.AutoModTriggerMetaData;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.List;

@Value.Immutable
public interface AutoModRuleEditSpecGenerator extends AuditSpec<AutoModRuleModifyRequest> {

    String name();

    int eventType();

    Possible<AutoModTriggerMetaData> triggerMetaData();

    List<AutoModActionData> actions();

    boolean enabled();

    List<Id> exemptRoles();

    List<Id> exemptChannels();

    @Override
    default AutoModRuleModifyRequest asRequest() {
        return AutoModRuleModifyRequest.builder()
                .name(name())
                .eventType(eventType())
                .triggerMetadata(triggerMetaData())
                .actions(actions())
                .enabled(enabled())
                .exemptRoles(exemptRoles())
                .exemptChannels(exemptChannels())
                .build();
    }

}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class AutoModRuleEditMonoGenerator extends Mono<AutoModRule> implements AutoModRuleEditSpecGenerator {

    abstract AutoModRule autoModRule();

    @Override
    public void subscribe(CoreSubscriber<? super AutoModRule> actual) {
        autoModRule().edit(AutoModRuleEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
