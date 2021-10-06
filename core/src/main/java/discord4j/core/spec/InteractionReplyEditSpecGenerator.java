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
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.WebhookMessageEditRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.MultipartRequest;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;
import static discord4j.core.spec.InternalSpecUtils.mapPossibleOptional;

@Value.Immutable(singleton = true)
interface InteractionReplyEditSpecGenerator extends FilesMessageRequestSpec<WebhookMessageEditRequest> {

    @Override
    default WebhookMessageEditRequest getMessageRequest() {
        return WebhookMessageEditRequest.builder()
                .from(getBaseRequest())
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class InteractionReplyEditMonoGenerator extends Mono<Message> implements InteractionReplyEditSpecGenerator {

    abstract InteractionCreateEvent event();

    @Override
    public void subscribe(CoreSubscriber<? super Message> actual) {
        event().editReply(InteractionReplyEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class InteractionFollowupEditMonoGenerator extends Mono<Message> implements InteractionReplyEditSpecGenerator {

    abstract Snowflake messageId();

    abstract InteractionCreateEvent event();

    @Override
    public void subscribe(CoreSubscriber<? super Message> actual) {
        event().editFollowup(messageId(), InteractionReplyEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}