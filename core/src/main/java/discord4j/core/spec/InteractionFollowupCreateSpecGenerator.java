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

import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.component.BaseMessageComponent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.component.TopLevelComponent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.FollowupMessageRequest;
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
interface InteractionFollowupCreateSpecGenerator extends Spec<MultipartRequest<FollowupMessageRequest>> {

    Possible<String> content();

    Possible<String> username();

    Possible<String> avatarUrl();

    @Value.Default
    default boolean tts() {
        return false;
    }

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

    Possible<AllowedMentions> allowedMentions();

    /**
     * @deprecated this only allow Layouts but components v2 include more components not valid in layout, {@link #componentsV2()} can override this
     */
    @Deprecated
    Possible<List<LayoutComponent>> components();

    Possible<List<TopLevelComponent>> componentsV2();

    Possible<Boolean> ephemeral();

    @Override
    default MultipartRequest<FollowupMessageRequest> asRequest() {
        List<Message.Flag> flagsToApply = new ArrayList<>();
        if (this.ephemeral().toOptional().orElse(false)) {
            flagsToApply.add(Message.Flag.EPHEMERAL);
        }
        if (!this.componentsV2().isAbsent()) {
            flagsToApply.add(Message.Flag.IS_COMPONENTS_V2);
        }
        Possible<List<Message.Flag>> pFlagsToApply = Possible.of(flagsToApply);
        FollowupMessageRequest request = FollowupMessageRequest.builder()
                .content(content())
                .username(username())
                .avatarUrl(avatarUrl())
                .tts(tts())
                .embeds(embeds().stream().map(EmbedCreateSpec::asRequest).collect(Collectors.toList()))
                .allowedMentions(mapPossible(allowedMentions(), AllowedMentions::toData))
                .components((componentsV2().isAbsent() ? mapPossible(components(),
                    components -> components.stream()
                        .map(BaseMessageComponent::getData)
                        .collect(Collectors.toList())) : mapPossible(componentsV2(),
                    components -> components.stream()
                        .map(BaseMessageComponent::getData)
                        .collect(Collectors.toList()))))
                .flags(mapPossible(pFlagsToApply, f -> f.stream()
                    .mapToInt(Message.Flag::getFlag)
                    .reduce(0, (left, right) -> left | right)))
                .build();
        return MultipartRequest.ofRequestAndFiles(request, Stream.concat(files().stream(), fileSpoilers().stream())
                .map(MessageCreateFields.File::asRequest)
                .collect(Collectors.toList()));
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class InteractionFollowupCreateMonoGenerator extends Mono<Message> implements InteractionFollowupCreateSpecGenerator {

    abstract DeferrableInteractionEvent event();

    @Override
    public void subscribe(CoreSubscriber<? super Message> actual) {
        event().createFollowup(InteractionFollowupCreateSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
