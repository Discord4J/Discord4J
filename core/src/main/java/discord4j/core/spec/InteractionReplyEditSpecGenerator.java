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
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.component.BaseMessageComponent;
import discord4j.core.object.component.TopLevelMessageComponent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.PollCreateData;
import discord4j.discordjson.json.WebhookMessageEditRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.MultipartRequest;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;
import static discord4j.core.spec.InternalSpecUtils.mapPossibleOptional;

@Value.Immutable(singleton = true)
interface InteractionReplyEditSpecGenerator extends Spec<MultipartRequest<WebhookMessageEditRequest>> {

    Possible<Optional<String>> content();

    Possible<Boolean> suppressEmbeds();

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

    Possible<Optional<List<TopLevelMessageComponent>>> components();

    Possible<PollCreateData> poll();

    Possible<Optional<List<Attachment>>> attachments();

    @Override
    default MultipartRequest<WebhookMessageEditRequest> asRequest() {
        List<Message.Flag> flagsToApply = new ArrayList<>();
        if (this.suppressEmbeds().toOptional().orElse(false)) {
            flagsToApply.add(Message.Flag.SUPPRESS_EMBEDS);
        }
        if (!this.components().isAbsent() && this.components().map(Optional::get).get().stream().anyMatch(topLevelComponent -> topLevelComponent.getType().isRequiredFlag())) {
            flagsToApply.add(Message.Flag.IS_COMPONENTS_V2);
        }
        Possible<Optional<List<Message.Flag>>> pFlagsToApply = Possible.of(Optional.of(flagsToApply));
        WebhookMessageEditRequest json = WebhookMessageEditRequest.builder()
            .content(content())
            .embeds(mapPossibleOptional(embeds(), embeds -> embeds.stream()
                .map(EmbedCreateSpec::asRequest)
                .collect(Collectors.toList())))
            .allowedMentions(mapPossibleOptional(allowedMentions(), AllowedMentions::toData))
            .components(mapPossible(components(), components -> components
                .map(list -> list.stream()
                    .map(BaseMessageComponent::getData)
                    .collect(Collectors.toList()))
                .orElse(Collections.emptyList())))
            .poll(poll())
            .flags(mapPossibleOptional(pFlagsToApply, f -> f.stream()
                .mapToInt(Message.Flag::getFlag)
                .reduce(0, (left, right) -> left | right)))
            // TODO upon v10 upgrade, it is required to also include new files as attachment here
            .attachments(mapPossibleOptional(attachments(), attachments -> attachments.stream()
                .map(Attachment::getData)
                .collect(Collectors.toList())))
            .build();
        return MultipartRequest.ofRequestAndFiles(json, Stream.concat(files().stream(), fileSpoilers().stream())
            .map(MessageCreateFields.File::asRequest)
            .collect(Collectors.toList()));
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class InteractionReplyEditMonoGenerator extends Mono<Message> implements InteractionReplyEditSpecGenerator {

    abstract DeferrableInteractionEvent event();

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

    abstract DeferrableInteractionEvent event();

    @Override
    public void subscribe(CoreSubscriber<? super Message> actual) {
        event().editFollowup(messageId(), InteractionReplyEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
