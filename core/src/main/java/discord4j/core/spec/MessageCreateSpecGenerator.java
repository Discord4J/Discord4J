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
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.discordjson.json.MessageReferenceData;
import discord4j.discordjson.json.PollCreateData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.MultipartRequest;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable(singleton = true)
interface MessageCreateSpecGenerator extends Spec<MultipartRequest<MessageCreateRequest>> {

    Possible<String> content();

    Possible<String> nonce();

    Possible<Boolean> enforceNonce();

    Possible<Boolean> tts();

    Possible<List<EmbedCreateSpec>> embeds();

    @Value.Default
    default List<MessageCreateFields.File> files() {
        return Collections.emptyList();
    }

    @Value.Default
    default List<MessageCreateFields.FileSpoiler> fileSpoilers() {
        return Collections.emptyList();
    }

    Possible<AllowedMentions> allowedMentions();

    Possible<Snowflake> messageReference();

    Possible<List<LayoutComponent>> components();

    Possible<PollCreateData> poll();

    @Override
    default MultipartRequest<MessageCreateRequest> asRequest() {
        MessageCreateRequest json = MessageCreateRequest.builder()
                .content(content())
                .nonce(nonce())
                .enforceNonce(enforceNonce())
                .tts(tts())
                .embeds(mapPossible(embeds(), embeds -> embeds.stream()
                        .map(EmbedCreateSpec::asRequest)
                        .collect(Collectors.toList())))
                .allowedMentions(mapPossible(allowedMentions(), AllowedMentions::toData))
                .messageReference(mapPossible(messageReference(),
                        ref -> MessageReferenceData.builder()
                                .messageId(ref.asString())
                                .build()))
                .components(mapPossible(components(), components -> components.stream()
                        .map(LayoutComponent::getData)
                        .collect(Collectors.toList())))
                .poll(poll())
                .build();
        return MultipartRequest.ofRequestAndFiles(json, Stream.concat(files().stream(), fileSpoilers().stream())
                .map(MessageCreateFields.File::asRequest)
                .collect(Collectors.toList()));
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class MessageCreateMonoGenerator extends Mono<Message> implements MessageCreateSpecGenerator {

    abstract MessageChannel channel();

    @Override
    public void subscribe(CoreSubscriber<? super Message> actual) {
        channel().createMessage(MessageCreateSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
