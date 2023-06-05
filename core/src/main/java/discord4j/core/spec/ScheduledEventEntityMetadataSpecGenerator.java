package discord4j.core.spec;

import discord4j.discordjson.json.GuildScheduledEventEntityMetadataData;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;

@Value.Immutable
public interface ScheduledEventEntityMetadataSpecGenerator extends Spec<GuildScheduledEventEntityMetadataData> {

    Possible<String> location();

    @Override
    default GuildScheduledEventEntityMetadataData asRequest() {
        return GuildScheduledEventEntityMetadataData.builder()
            .location(location())
            .build();
    }
}
