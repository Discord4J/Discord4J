package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.object.component.MessageComponent;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.ForumThreadMessageParamsData;
import discord4j.discordjson.json.ImmutableForumThreadMessageParamsData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import org.immutables.value.Value;

import java.util.List;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable
public interface ForumThreadMessageCreateSpecGenerator extends Spec<ForumThreadMessageParamsData> {

    Possible<String> content();

    Possible<List<EmbedCreateSpec>> embeds();

    Possible<List<AllowedMentions>> allowedMentions();

    Possible<List<MessageComponent>> components();

    Possible<List<Snowflake>> stickerIds();

    @Override
    default ForumThreadMessageParamsData asRequest() {
        ImmutableForumThreadMessageParamsData.Builder builder = ForumThreadMessageParamsData.builder();

        return builder.content(content())
            .embeds(mapPossible(embeds(), list -> list.stream().map(EmbedCreateSpec::asRequest).collect(Collectors.toList())))
            .allowedMentions(mapPossible(allowedMentions(), list -> list.stream().map(AllowedMentions::toData).collect(Collectors.toList())))
            .components(mapPossible(components(), list -> list.stream().map(MessageComponent::getData).collect(Collectors.toList())))
            .stickerIds(mapPossible(stickerIds(), list -> list.stream().map(snowflake -> Id.of(snowflake.asLong())).collect(Collectors.toList())))
            .build();
    }
}
