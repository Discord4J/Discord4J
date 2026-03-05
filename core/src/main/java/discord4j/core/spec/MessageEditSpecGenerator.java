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

import discord4j.core.object.component.BaseMessageComponent;
import discord4j.core.object.component.TopLevelMessageComponent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.ImmutableMessageEditRequest;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.MultipartRequest;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static discord4j.core.spec.InternalSpecUtils.mapPossibleOptional;

@Value.Immutable(singleton = true)
interface MessageEditSpecGenerator extends Spec<MultipartRequest<MessageEditRequest>> {

    Possible<Optional<String>> content();

    Possible<Optional<List<EmbedCreateSpec>>> embeds();

    @Value.Default
    default List<MessageCreateFields.File> files() {
        return Collections.emptyList();
    }

    @Value.Default
    default List<MessageCreateFields.FileSpoiler> fileSpoilers() {
        return Collections.emptyList();
    }

    Possible<Optional<AllowedMentions>> allowedMentions();

    Possible<Optional<List<Message.Flag>>> flags();

    Possible<Optional<List<TopLevelMessageComponent>>> components();

    Possible<Optional<List<Attachment>>> attachments();

    @Override
    default MultipartRequest<MessageEditRequest> asRequest() {
        final Set<Message.Flag> flagsToApply = InternalMessageSpecUtils.decorateFlags(
                Possible.flatOpt(this.flags()).orElse(null),
                Possible.absent(),
                Possible.absent(),
                Possible.ofNullable(Possible.flatOpt(this.components()).orElse(null))
        );

        ImmutableMessageEditRequest.Builder messageEditRequestBuilder = MessageEditRequest.builder()
                .content(content())
                .embeds(mapPossibleOptional(embeds(), embeds -> embeds.stream()
                        .map(EmbedCreateSpec::asRequest)
                        .collect(Collectors.toList())))
                .allowedMentions(mapPossibleOptional(allowedMentions(), AllowedMentions::toData))
                .flags(mapPossibleOptional(Possible.of(Optional.ofNullable(flagsToApply)), f -> f.stream()
                        .mapToInt(Message.Flag::getFlag)
                        .reduce(0, (left, right) -> left | right)))
                .components(mapPossibleOptional(components(), components -> components.stream()
                        .map(BaseMessageComponent::getData)
                        .collect(Collectors.toList())))
                .attachments(mapPossibleOptional(attachments(), attachments -> attachments.stream()
                        .map(Attachment::getData)
                        .collect(Collectors.toList())));

        final List<MessageCreateFields.File> filesCollected = Stream.concat(files().stream(), fileSpoilers().stream()).collect(Collectors.toList());
        final List<Tuple2<String, InputStream>> filesTuples = filesCollected.stream()
                .map(MessageCreateFields.File::asRequest)
                .collect(Collectors.toList());

        if (!filesCollected.isEmpty()) {
            for (int x = 0; x < filesCollected.size(); x++) {
                messageEditRequestBuilder.addAttachment(filesCollected.get(x).asPartialAttachmentData(Id.of(x)));
            }
        }

        MessageEditRequest json = messageEditRequestBuilder.build();
        return MultipartRequest.ofRequestAndFiles(json, filesTuples);
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class MessageEditMonoGenerator extends Mono<Message> implements MessageEditSpecGenerator {

    abstract Message message();

    @Override
    public void subscribe(CoreSubscriber<? super Message> actual) {
        message().edit(MessageEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
