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
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Webhook;
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
public interface WebhookMessageEditSpecGenerator extends Spec<MultipartRequest<WebhookMessageEditRequest>> {

    Possible<Optional<String>> content();

    @Value.Default
    default List<MessageCreateFields.File> files() {
        return Collections.emptyList();
    }

    @Value.Default
    default List<MessageCreateFields.FileSpoiler> fileSpoilers() {
        return Collections.emptyList();
    }

    @Value.Default
    default List<EmbedCreateSpec> embeds() {
        return Collections.emptyList();
    }

    Possible<Optional<AllowedMentions>> allowedMentions();

    Possible<List<LayoutComponent>> components();

    Possible<Snowflake> threadId();

    @Override
    default MultipartRequest<WebhookMessageEditRequest> asRequest() {
        WebhookMessageEditRequest request = WebhookMessageEditRequest.builder()
            .content(content())
            .embeds(embeds().stream().map(EmbedCreateSpec::asRequest).collect(Collectors.toList()))
            .allowedMentions(mapPossibleOptional(allowedMentions(), AllowedMentions::toData))
            .components(mapPossible(components(), components -> components.stream()
                .map(LayoutComponent::getData)
                .collect(Collectors.toList())))
            .build();
        return MultipartRequest.ofRequestAndFiles(request, Stream.concat(files().stream(), fileSpoilers().stream())
            .map(MessageCreateFields.File::asRequest)
            .collect(Collectors.toList()));
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class WebhookMessageEditMonoGenerator extends Mono<Message> implements WebhookMessageEditSpecGenerator {

    abstract Snowflake messageId();

    abstract Webhook webhook();

    @Override
    public void subscribe(CoreSubscriber<? super Message> actual) {
        if (this.threadId().isAbsent()) {
            webhook().editMessage(messageId(), WebhookMessageEditSpec.copyOf(this)).subscribe(actual);
        } else {
            webhook().editMessage(messageId(), this.threadId().get(), WebhookMessageEditSpec.copyOf(this)).subscribe(actual);
        }
    }

    @Override
    public abstract String toString();
}
