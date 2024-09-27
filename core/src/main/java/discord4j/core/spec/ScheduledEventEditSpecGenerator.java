package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.ScheduledEvent;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.GuildScheduledEventModifyRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable
public interface ScheduledEventEditSpecGenerator extends AuditSpec<GuildScheduledEventModifyRequest> {

    /* Possible for events with entity type external */
    Possible<Optional<Snowflake>> channelId();

    Possible<ScheduledEventEntityMetadataSpec> entityMetadata();

    Possible<String> name();

    Possible<ScheduledEvent.PrivacyLevel> privacyLevel();

    Possible<Instant> scheduledStartTime();

    Possible<Instant> scheduledEndTime();

    Possible<String> description();

    Possible<ScheduledEvent.EntityType> entityType();

    Possible<ScheduledEvent.Status> status();

    Possible<Image> image();

    Possible<Optional<RecurrenceRuleSpec>> recurrenceRule();

    @Override
    default GuildScheduledEventModifyRequest asRequest() {
        return GuildScheduledEventModifyRequest.builder()
            .channelId(mapPossible(channelId(), optional -> optional.map(snowflake -> Id.of(snowflake.asLong()))))
            .entityMetadata(mapPossible(entityMetadata(), ScheduledEventEntityMetadataSpecGenerator::asRequest))
            .name(name())
            .privacyLevel(mapPossible(privacyLevel(), ScheduledEvent.PrivacyLevel::getValue))
            .scheduledStartTime(scheduledStartTime())
            .scheduledEndTime(scheduledEndTime())
            .description(description())
            .entityType(mapPossible(entityType(), ScheduledEvent.EntityType::getValue))
            .status(mapPossible(status(), ScheduledEvent.Status::getValue))
            .image(mapPossible(image(), Image::getDataUri))
            .recurrenceRule(mapPossible(recurrenceRule(), optional -> optional.map(RecurrenceRuleSpecGenerator::asRequest)))
            .build();
    }
}

@Value.Immutable(builder = false)
abstract class ScheduledEventEditMonoGenerator extends Mono<ScheduledEvent> implements ScheduledEventEditSpecGenerator {

    abstract ScheduledEvent event();

    @Override
    public void subscribe(CoreSubscriber<? super ScheduledEvent> actual) {
        event().edit(ScheduledEventEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
