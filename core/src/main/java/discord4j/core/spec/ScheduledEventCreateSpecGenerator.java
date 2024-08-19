package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.ScheduledEvent;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.GuildScheduledEventCreateRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable
public interface ScheduledEventCreateSpecGenerator extends AuditSpec<GuildScheduledEventCreateRequest> {

    /* Possible for events with entity type external */
    Possible<Snowflake> channelId();

    Possible<ScheduledEventEntityMetadataSpec> entityMetadata();

    String name();

    ScheduledEvent.PrivacyLevel privacyLevel();

    Instant scheduledStartTime();

    Possible<Instant> scheduledEndTime();

    Possible<String> description();

    ScheduledEvent.EntityType entityType();

    Possible<Image> image();

    Possible<RecurrenceRuleSpec> recurrenceRule();

    @Override
    default GuildScheduledEventCreateRequest asRequest() {
        return GuildScheduledEventCreateRequest.builder()
            .channelId(mapPossible(channelId(), snowflake -> Id.of(snowflake.asLong())))
            .entityMetadata(mapPossible(entityMetadata(), ScheduledEventEntityMetadataSpecGenerator::asRequest))
            .name(name())
            .privacyLevel(privacyLevel().getValue())
            .scheduledStartTime(scheduledStartTime())
            .scheduledEndTime(scheduledEndTime())
            .description(description())
            .entityType(entityType().getValue())
            .image(mapPossible(image(), Image::getDataUri))
            .recurrenceRule(mapPossible(recurrenceRule(), RecurrenceRuleSpecGenerator::asRequest))
            .build();
    }
}

@Value.Immutable(builder = false)
abstract class ScheduledEventCreateMonoGenerator extends Mono<ScheduledEvent> implements ScheduledEventCreateSpecGenerator {

    abstract Guild guild();

    @Override
    public void subscribe(CoreSubscriber<? super ScheduledEvent> actual) {
        guild().createScheduledEvent(ScheduledEventCreateSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
