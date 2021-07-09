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
import discord4j.core.object.entity.Webhook;
import discord4j.discordjson.json.WebhookModifyRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable(singleton = true)
interface WebhookEditSpecGenerator extends AuditSpec<WebhookModifyRequest> {

    Possible<String> name();

    Possible<Image> avatar();

    Possible<Snowflake> channelId();

    @Override
    default WebhookModifyRequest asRequest() {
        return WebhookModifyRequest.builder()
                .name(name())
                .avatar(mapPossible(avatar(), Image::getDataUri))
                .channelId(mapPossible(channelId(), Snowflake::asString))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class WebhookEditMonoGenerator extends Mono<Webhook> implements WebhookEditSpecGenerator {

    abstract Webhook webhook();

    @Override
    public void subscribe(CoreSubscriber<? super Webhook> actual) {
        webhook().edit(WebhookEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}