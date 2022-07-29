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
import discord4j.core.object.component.LayoutComponent;
import discord4j.discordjson.json.InteractionApplicationCommandCallbackData;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable(singleton = true)
interface InteractionPresentModalSpecGenerator extends Spec<InteractionApplicationCommandCallbackData> {

    Possible<String> title();

    Possible<String> customId();

    Possible</*~~>*/List<LayoutComponent>> components();

    @Override
    default InteractionApplicationCommandCallbackData asRequest() {
        return InteractionApplicationCommandCallbackData.builder()
                .title(title())
                .customId(customId())
                .components(mapPossible(components(), components -> components.stream()
                        .map(LayoutComponent::getData)
                        .collect(Collectors.toList())))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class InteractionPresentModalMonoGenerator extends Mono<Void>
        implements InteractionPresentModalSpecGenerator {

    abstract DeferrableInteractionEvent event();

    @Override
    public void subscribe(CoreSubscriber<? super Void> actual) {
        event().presentModal(InteractionPresentModalSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
