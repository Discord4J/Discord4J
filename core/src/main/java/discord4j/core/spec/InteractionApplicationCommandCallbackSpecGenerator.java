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

import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.InteractionApplicationCommandCallbackData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable(singleton = true)
interface InteractionApplicationCommandCallbackSpecGenerator extends Spec<InteractionApplicationCommandCallbackData> {

    Possible<String> content();

    Possible<Boolean> tts();

    Possible<Boolean> ephemeral();

    Possible<List<EmbedCreateSpec>> embeds();

    Possible<AllowedMentions> allowedMentions();

    Possible<List<LayoutComponent>> components();

    @Override
    default InteractionApplicationCommandCallbackData asRequest() {
        return InteractionApplicationCommandCallbackData.builder()
                .content(content())
                .tts(tts())
                .flags(mapPossible(ephemeral(), eph -> eph ? Message.Flag.EPHEMERAL.getFlag() : 0))
                .embeds(mapPossible(embeds(), embeds -> embeds.stream()
                        .map(EmbedCreateSpec::asRequest)
                        .collect(Collectors.toList())))
                .components(mapPossible(components(), components -> components.stream()
                    .map(LayoutComponent::getData)
                    .collect(Collectors.toList())))
                .allowedMentions(mapPossible(allowedMentions(), AllowedMentions::toData))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class InteractionApplicationCommandCallbackMonoGenerator extends Mono<Void>
        implements InteractionApplicationCommandCallbackSpecGenerator {

    abstract InteractionCreateEvent event();

    @Override
    public void subscribe(CoreSubscriber<? super Void> actual) {
        event().reply(InteractionApplicationCommandCallbackSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
