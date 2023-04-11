package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.ScheduledEvent;
import discord4j.core.spec.ScheduledEventCreateSpec;
import discord4j.core.spec.ScheduledEventEntityMetadataSpec;

import java.time.Duration;
import java.time.Instant;

/**
 * Example bot creating and updating a guild scheduled event
 * Requires token and guildId environment variables to be set to work properly
 */
public class ExampleGuildScheduledEvents {

    private static final String token = System.getenv("token");
    private static final long guildId = Long.parseLong(System.getenv("guildId"));

    public static final String EVENT_NAME = "Guild event test",
        EVENT_DESCRIPTION = "This is a test event from a bot using Discord4J.",
        EVENT_LOCATION = "Somewhere, probably still on Earth";

    public static void main(String[] args) {
        DiscordClient.create(token)
            .withGateway(client -> client.on(ReadyEvent.class)
                // Fetch the given guild
                .flatMap(ignored -> client.getGuildById(Snowflake.of(guildId)))
                // Create the event
                .flatMap(guild -> guild.createScheduledEvent(ScheduledEventCreateSpec.builder()
                    .name(EVENT_NAME)
                    .description(EVENT_DESCRIPTION)
                    .privacyLevel(ScheduledEvent.PrivacyLevel.GUILD_ONLY.getValue())
                    .entityType(3)
                    .entityMetadata(ScheduledEventEntityMetadataSpec.builder()
                        .location(EVENT_LOCATION)
                        .build())
                    .scheduledStartTime(Instant.now().plusSeconds(3600)) // Start in one hour
                    .scheduledEndTime(Instant.now().plusSeconds(7200)) // End in two hours (required for external events)
                    .build()
                ))
                // Update the event
                .flatMap(event -> event.edit().withName("Edited " + EVENT_NAME))
                // Advertise our event
                .doOnNext(event -> System.out.format("The created event ID is %d\n", event.getId().asLong()))
                // Wait a minute before deleting it
                .delayElements(Duration.ofMinutes(1))
                // Delete it
                .flatMap(ScheduledEvent::delete)
            )
            .block();
    }

}
