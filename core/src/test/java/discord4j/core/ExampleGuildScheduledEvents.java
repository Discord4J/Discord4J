package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.guild.*;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.RecurrenceRule;
import discord4j.core.object.entity.ScheduledEvent;
import discord4j.core.spec.RecurrenceRuleNWeekdaySpec;
import discord4j.core.spec.RecurrenceRuleSpec;
import discord4j.core.spec.ScheduledEventCreateSpec;
import discord4j.core.spec.ScheduledEventEditSpec;
import discord4j.core.spec.ScheduledEventEntityMetadataSpec;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Example bot creating and updating a guild scheduled event
 * Requires token and guildId environment variables to be set to work properly
 */
public class ExampleGuildScheduledEvents {

    private static final Logger log = Loggers.getLogger(ExampleGuildScheduledEvents.class);

    private static final String token = System.getenv("token");
    private static final long guildId = Long.parseLong(System.getenv("guildId"));

    public static final String EVENT_NAME = "Guild event test";
    public static final String EVENT_DESCRIPTION = "This is a test event from a bot using Discord4J.";
    public static final String EVENT_LOCATION = "Somewhere, probably still on Earth";

    public static void main(String[] args) {
        DiscordClient.create(token)
            .withGateway(client -> {

                // Logic to create a scheduled event on ready event
                Publisher<?> createOnReady = client.on(ReadyEvent.class)
                    // Fetch the given guild
                    .flatMap(ignored -> client.getGuildById(Snowflake.of(guildId)))
                    // Create the event
                    .flatMap(guild -> {
                        // Recurrence rule for Monthly on the fourth Wednesday
                        List<RecurrenceRuleNWeekdaySpec> rrNWeekday = new ArrayList<>();
                        rrNWeekday.add(RecurrenceRuleNWeekdaySpec.builder().n(4).day(DayOfWeek.WEDNESDAY).build());
                        RecurrenceRuleSpec recurrenceRuleSpec = RecurrenceRuleSpec.builder()
                            .start(Instant.now().plusSeconds(10))
                            .frequency(RecurrenceRule.Frequency.MONTHLY)
                            .interval(1)
                            .byNWeekday(rrNWeekday)
                            .build();

                        // Create the ScheduledEvent
                        return guild.createScheduledEvent(ScheduledEventCreateSpec.builder()
                            .name(EVENT_NAME)
                            .privacyLevel(ScheduledEvent.PrivacyLevel.GUILD_ONLY)
                            .entityType(ScheduledEvent.EntityType.EXTERNAL)
                            .entityMetadata(ScheduledEventEntityMetadataSpec.builder()
                                .location(EVENT_LOCATION)
                                .build())
                            .scheduledStartTime(Instant.now().plusSeconds(3600)) // Start in one hour
                            .scheduledEndTime(Instant.now().plusSeconds(7200)) // End in two hours (required for external events)
                            .recurrenceRule(recurrenceRuleSpec)
                            .build()
                        );
                    })
                    // Advertise our event
                    .doOnNext(event -> System.out.format("The created event ID is %d\n", event.getId().asLong()))
                    // Wait 10 seconds, then update the event
                    .delayElements(Duration.ofSeconds(10))
                    .flatMap(event -> event.edit(ScheduledEventEditSpec.builder()
                        .name("Edited " + EVENT_NAME)
                        .description(EVENT_DESCRIPTION)
                        .status(ScheduledEvent.Status.ACTIVE)
                        .recurrenceRuleOrNull(null)
                        .build()))
                    // Wait a minute before deleting it
                    .delayElements(Duration.ofMinutes(3))
                    // Delete it
                    .flatMap(ScheduledEvent::delete);

                // New listeners
                Publisher<?> eventListeners = client.on(new ReactiveEventAdapter() {
                    @Override
                    public Publisher<?> onScheduledEventCreate(ScheduledEventCreateEvent event) {
                        return Mono.fromRunnable(() -> log.info("Created event: {}",
                            event.getScheduledEvent().getData()));
                    }

                    @Override
                    public Publisher<?> onScheduledEventUpdate(ScheduledEventUpdateEvent event) {
                        return Mono.fromRunnable(() -> {
                            log.info("Old event: {}", event.getOld().map(ScheduledEvent::getData).orElse(null));
                            log.info("Updated event: {}", event.getCurrent().getData());
                        });
                    }

                    @Override
                    public Publisher<?> onScheduledEventDelete(ScheduledEventDeleteEvent event) {
                        return Mono.fromRunnable(() -> log.info("Deleted event: {}",
                            event.getScheduledEvent().getData()));
                    }

                    @Override
                    public Publisher<?> onScheduledEventUserAdd(ScheduledEventUserAddEvent event) {
                        return event.getMember().doOnNext(member -> log.info("Subscribed: {}", member));
                    }

                    @Override
                    public Publisher<?> onScheduledEventUserRemove(ScheduledEventUserRemoveEvent event) {
                        return event.getMember().doOnNext(member -> log.info("Unsubscribed: {}", member));
                    }
                });

                // Join both publishers and return them to start a subscription on startup
                // To avoid event race conditions ReadyEvent listeners must go first
                return Mono.when(createOnReady, eventListeners);
            })
            .block();
    }

}
