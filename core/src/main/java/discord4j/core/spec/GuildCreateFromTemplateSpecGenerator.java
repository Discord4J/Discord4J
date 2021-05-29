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

import discord4j.core.object.GuildTemplate;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.TemplateCreateGuildRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import static discord4j.core.spec.InternalSpecUtils.*;

@SpecStyle
@Value.Immutable
interface GuildCreateFromTemplateSpecGenerator extends Spec<TemplateCreateGuildRequest> {

    String name();

    Possible<Image> icon();

    @Override
    default TemplateCreateGuildRequest asRequest() {
        return TemplateCreateGuildRequest.builder()
                .name(name())
                .icon(mapPossible(icon(), Image::getDataUri))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@SpecStyle
@Value.Immutable(builder = false)
abstract class GuildCreateFromTemplateMonoGenerator extends Mono<Guild>
        implements GuildCreateFromTemplateSpecGenerator {

    abstract GuildTemplate guildTemplate();

    @Override
    public void subscribe(CoreSubscriber<? super Guild> actual) {
        guildTemplate().createGuild(GuildCreateFromTemplateSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}