package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.ForumTagData;
import discord4j.discordjson.json.ForumTagParamsData;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;

import java.util.Optional;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable
public interface ForumTagCreateSpecGenerator extends Spec<ForumTagParamsData> {

    String name();

    Possible<Boolean> moderated();

    Possible<Optional<String>> emojiName();

    Possible<Optional<Snowflake>> emojiId();


    @Override
    default ForumTagParamsData asRequest() {
        return ForumTagParamsData.builder()
            .name(name())
            .moderated(moderated())
            .emojiName(emojiName())
            .emojiId(mapPossible(emojiId(), opt -> opt.map(snowflake -> Id.of(snowflake.asLong()))))
            .build();
    }

}
