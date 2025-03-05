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

import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.component.BaseMessageComponent;
import discord4j.core.object.component.TopLevelMessageComponent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.InteractionApplicationCommandCallbackData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.MultipartRequest;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable(singleton = true)
interface InteractionApplicationCommandCallbackSpecGenerator extends Spec<MultipartRequest<InteractionApplicationCommandCallbackData>> {

    Possible<String> content();

    Possible<Boolean> tts();

    Possible<Boolean> ephemeral();

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

    Possible<List<TopLevelMessageComponent>> components();

    @Override
    default MultipartRequest<InteractionApplicationCommandCallbackData> asRequest() {
        List<Message.Flag> flagsToApply = new ArrayList<>();
        if (this.ephemeral().toOptional().orElse(false)) {
            flagsToApply.add(Message.Flag.EPHEMERAL);
        }
        if (!this.components().isAbsent() && this.components().get().stream().anyMatch(topLevelComponent -> topLevelComponent.getType().isRequiredFlag())) {
            flagsToApply.add(Message.Flag.IS_COMPONENTS_V2);
        }
        Possible<List<Message.Flag>> pFlagsToApply = Possible.of(flagsToApply);
        InteractionApplicationCommandCallbackData json = InteractionApplicationCommandCallbackData.builder()
                .content(content())
                .tts(tts())
                .flags(mapPossible(pFlagsToApply, f -> f.stream()
                    .mapToInt(Message.Flag::getFlag)
                    .reduce(0, (left, right) -> left | right)))
                .embeds(mapPossible(embeds(), embeds -> embeds.stream()
                        .map(EmbedCreateSpec::asRequest)
                        .collect(Collectors.toList())))
                .components(mapPossible(components(), components -> components.stream()
                    .map(BaseMessageComponent::getData)
                    .collect(Collectors.toList())))
                .allowedMentions(mapPossible(allowedMentions(), AllowedMentions::toData))
                .build();
        return MultipartRequest.ofRequestAndFiles(json, Stream.concat(files().stream(), fileSpoilers().stream())
                .map(MessageCreateFields.File::asRequest)
                .collect(Collectors.toList()));
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class InteractionApplicationCommandCallbackReplyMonoGenerator extends Mono<Void>
        implements InteractionApplicationCommandCallbackSpecGenerator {

    abstract DeferrableInteractionEvent event();

    @Override
    public void subscribe(CoreSubscriber<? super Void> actual) {
        event().reply(InteractionApplicationCommandCallbackSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class InteractionApplicationCommandCallbackEditMonoGenerator extends Mono<Void>
        implements InteractionApplicationCommandCallbackSpecGenerator {

    abstract ComponentInteractionEvent event();

    @Override
    public void subscribe(CoreSubscriber<? super Void> actual) {
        event().edit(InteractionApplicationCommandCallbackSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
