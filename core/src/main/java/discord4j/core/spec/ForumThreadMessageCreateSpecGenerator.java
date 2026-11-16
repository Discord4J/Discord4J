package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.object.component.TopLevelMessageComponent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.ForumThreadMessageParamsData;
import discord4j.discordjson.json.ImmutableForumThreadMessageParamsData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import org.immutables.value.Value;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable
public interface ForumThreadMessageCreateSpecGenerator extends Spec<ForumThreadMessageParamsData> {

    Possible<String> content();

    Possible<List<EmbedCreateSpec>> embeds();

    Possible<AllowedMentions> allowedMentions();

    Possible<List<TopLevelMessageComponent>> components();

    Possible<List<Snowflake>> stickerIds();

    @Override
    default ForumThreadMessageParamsData asRequest() {
        ImmutableForumThreadMessageParamsData.Builder builder = ForumThreadMessageParamsData.builder();

        builder.content(content());
        builder.embeds(mapPossible(
                embeds(),
                list -> list.stream().map(EmbedCreateSpec::asRequest).collect(Collectors.toList())
        ));
        builder.allowedMentions(mapPossible(allowedMentions(), AllowedMentions::toData));
        builder.components(mapPossible(
                components(),
                list -> list.stream().map(TopLevelMessageComponent::getData).collect(Collectors.toList())
        ));
        builder.stickerIds(mapPossible(
                stickerIds(),
                list -> list.stream().map(snowflake -> Id.of(snowflake.asLong())).collect(Collectors.toList())
        ));

        final Set<Message.Flag> flagsToApply = InternalMessageSpecUtils.decorateFlags(null, Possible.absent(),
                Possible.absent(), components());
        builder.flags(mapPossible(Possible.ofNullable(flagsToApply), f -> f.stream()
                .mapToInt(Message.Flag::getFlag)
                .reduce(0, (left, right) -> left | right)));

        return builder.build();
    }
}
