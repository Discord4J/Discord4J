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
import discord4j.core.object.entity.GuildEmoji;
import discord4j.discordjson.json.GuildEmojiModifyRequest;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapNullable;
import static discord4j.core.spec.InternalSpecUtils.toPossible;

@SpecStyle
@Value.Immutable(singleton = true)
interface GuildEmojiEditSpecGenerator extends AuditSpec<GuildEmojiModifyRequest> {

    @Nullable
    String name();

    @Nullable
    Set<Snowflake> roles();

    @Override
    default GuildEmojiModifyRequest asRequest() {
        return GuildEmojiModifyRequest.builder()
                .name(toPossible(name()))
                .roles(toPossible(mapNullable(roles(), r -> r.stream()
                        .map(Snowflake::asString)
                        .collect(Collectors.toList()))))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@SpecStyle
@Value.Immutable(builder = false)
abstract class GuildEmojiEditMonoGenerator extends Mono<GuildEmoji> implements GuildEmojiEditSpecGenerator {

    abstract GuildEmoji guildEmoji();

    @Override
    public void subscribe(CoreSubscriber<? super GuildEmoji> actual) {
        guildEmoji().edit(GuildEmojiEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}