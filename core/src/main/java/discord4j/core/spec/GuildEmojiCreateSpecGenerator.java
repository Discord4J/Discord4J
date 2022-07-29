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
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.discordjson.json.GuildEmojiCreateRequest;
import discord4j.rest.util.Image;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Value.Immutable
interface GuildEmojiCreateSpecGenerator extends AuditSpec<GuildEmojiCreateRequest> {

    String name();

    Image image();

    @Value.Default
    default /*~~>*/List<Snowflake> roles() {
        return Collections.emptyList();
    }

    @Override
    default GuildEmojiCreateRequest asRequest() {
        return GuildEmojiCreateRequest.builder()
                .name(name())
                .image(image().getDataUri())
                .roles(roles().stream().map(Snowflake::asString).collect(Collectors.toList()))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class GuildEmojiCreateMonoGenerator extends Mono<GuildEmoji> implements GuildEmojiCreateSpecGenerator {

    abstract Guild guild();

    @Override
    public void subscribe(CoreSubscriber<? super GuildEmoji> actual) {
        guild().createEmoji(GuildEmojiCreateSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
