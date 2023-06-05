/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.ForumTag;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.ForumChannel;
import discord4j.core.object.reaction.DefaultReaction;
import discord4j.discordjson.json.ChannelCreateRequest;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable
public interface ForumChannelCreateSpecGenerator extends AuditSpec<ChannelCreateRequest> {

    String name();

    Possible<String> topic();

    Possible<Integer> rateLimitPerUser();

    Possible<Integer> position();

    Possible<List<PermissionOverwrite>> permissionOverwrites();

    Possible<Snowflake> parentId();

    Possible<Boolean> nsfw();

    Possible<Optional<Integer>> defaultAutoArchiveDuration();

    Possible<EnumSet<Channel.Flag>> flags();

    Possible<Optional<DefaultReaction>> defaultReactionEmoji();

    List<ForumTagCreateSpec> availableTags();

    Possible<Optional<Integer>> defaultSortOrder();

    Possible<Optional<Integer>> defaultForumLayout();

    @Override
    default ChannelCreateRequest asRequest() {
        return ChannelCreateRequest.builder()
            .type(Channel.Type.GUILD_FORUM.getValue())
            .name(name())
            .topic(topic())
            .rateLimitPerUser(rateLimitPerUser())
            .permissionOverwrites(mapPossible(permissionOverwrites(), po -> po.stream()
                .map(PermissionOverwrite::getData)
                .collect(Collectors.toList())))
            .parentId(mapPossible(parentId(), Snowflake::asString))
            .nsfw(nsfw())
            .defaultAutoArchiveDuration(defaultAutoArchiveDuration())
            .flags(mapPossible(flags(), Channel.Flag::toBitfield))
            .defaultReactionEmoji(mapPossible(defaultReactionEmoji(), opt -> opt.map(DefaultReaction::getData)))
            .defaultForumLayout(defaultForumLayout())
            .availableTags(availableTags().stream().map(ForumTagCreateSpec::asRequest).collect(Collectors.toList()))
            .defaultSortOrder(defaultSortOrder())
            .build();
    }
}

@Value.Immutable(builder = false)
abstract class ForumChannelCreateMonoGenerator extends Mono<ForumChannel> implements ForumChannelCreateSpecGenerator {

    abstract Guild guild();

    @Override
    public void subscribe(CoreSubscriber<? super ForumChannel> actual) {
        guild().createForumChannel(ForumChannelCreateSpec.copyOf(this)).subscribe(actual);
    }
}
