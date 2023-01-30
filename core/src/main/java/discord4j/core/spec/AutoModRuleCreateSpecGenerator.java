package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.object.automod.AutoModRule;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.AutoModActionData;
import discord4j.discordjson.json.AutoModRuleCreateRequest;
import discord4j.discordjson.json.AutoModTriggerMetaData;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable(singleton = true)
interface AutoModRuleCreateSpecGenerator extends AuditSpec<AutoModRuleCreateRequest> {

    String name();

    int eventType();

    int triggerType();

    Possible<AutoModTriggerMetaData> triggerMetaData();

    List<AutoModActionData> actions();

    Possible<Boolean> enabled();

    Possible<List<Snowflake>> exemptRoles();

    Possible<List<Snowflake>> exemptChannels();

    @Override
    default AutoModRuleCreateRequest asRequest() {
        return AutoModRuleCreateRequest.builder()
                .name(name())
                .eventType(eventType())
                .triggerType(triggerType())
                .triggerMetadata(triggerMetaData())
                .actions(actions())
                .enabled(enabled())
                .exemptRoles(mapPossible(exemptRoles(), r -> r.stream().map(Snowflake::asLong).map(Id::of).collect(Collectors.toList())))
                .exemptChannels(mapPossible(exemptChannels(), r -> r.stream().map(Snowflake::asLong).map(Id::of).collect(Collectors.toList())))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class AutoModRuleCreateMonoGenerator extends Mono<AutoModRule> implements AutoModRuleCreateSpecGenerator {

    abstract Guild guild();

    @Override
    public void subscribe(CoreSubscriber<? super AutoModRule> actual) {
        guild().createAutoModRule(AutoModRuleCreateSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
