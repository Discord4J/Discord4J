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
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.InteractionApplicationCommandCallbackData;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable(singleton = true)
interface InteractionCallbackSpecGenerator extends Spec<InteractionApplicationCommandCallbackData> {

    Possible<Boolean> ephemeral();

    @Override
    default InteractionApplicationCommandCallbackData asRequest() {
        return InteractionApplicationCommandCallbackData.builder()
                .flags(mapPossible(ephemeral(), eph -> eph ? Message.Flag.EPHEMERAL.getFlag() : 0))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class InteractionCallbackSpecDeferReplyMonoGenerator extends Mono<Void>
        implements InteractionCallbackSpecGenerator {

    abstract DeferrableInteractionEvent event();

    @Override
    public void subscribe(CoreSubscriber<? super Void> actual) {
        event().deferReply(InteractionCallbackSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class InteractionCallbackSpecDeferEditMonoGenerator extends Mono<Void>
        implements InteractionCallbackSpecGenerator {

    abstract ComponentInteractionEvent event();

    @Override
    public void subscribe(CoreSubscriber<? super Void> actual) {
        event().deferEdit(InteractionCallbackSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
