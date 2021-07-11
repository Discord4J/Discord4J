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

import discord4j.core.object.entity.Webhook;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.TopLevelGuildMessageChannel;
import discord4j.discordjson.json.WebhookCreateRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static discord4j.core.spec.InternalSpecUtils.mapPossibleOptional;

@Value.Immutable
interface WebhookCreateSpecGenerator extends AuditSpec<WebhookCreateRequest> {

    String name();

    Possible<Optional<Image>> avatar();

    @Override
    default WebhookCreateRequest asRequest() {
        return WebhookCreateRequest.builder()
                .name(name())
                .avatar(mapPossibleOptional(avatar(), Image::getDataUri))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class WebhookCreateMonoGenerator extends Mono<Webhook> implements WebhookCreateSpecGenerator {

    abstract TopLevelGuildMessageChannel channel();

    @Override
    public void subscribe(CoreSubscriber<? super Webhook> actual) {
        channel().createWebhook(WebhookCreateSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
