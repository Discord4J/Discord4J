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

import discord4j.core.object.entity.PinnedMessageReference;
import discord4j.core.object.entity.channel.MessageChannel;
import org.immutables.value.Value;
import org.jspecify.annotations.Nullable;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static discord4j.core.spec.InternalSpecUtils.mapNullable;
import static discord4j.core.spec.InternalSpecUtils.putIfNotNull;

@Value.Immutable(singleton = true)
interface PinnedMessagesQuerySpecGenerator extends Spec<Map<String, Object>> {

    @Nullable
    Instant before();

    @Nullable
    Integer limit();

    @Override
    default Map<String, Object> asRequest() {
        Map<String, Object> request = new HashMap<>(2);
        putIfNotNull(request, "before", mapNullable(before(), Instant::toString));
        putIfNotNull(request, "limit", limit());
        return request;
    }

}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class PinnedMessagesQueryFluxGenerator extends Flux<PinnedMessageReference> implements PinnedMessagesQuerySpecGenerator {

    abstract MessageChannel messageChannel();

    @Override
    public void subscribe(CoreSubscriber<? super PinnedMessageReference> actual) {
        messageChannel().getPinnedMessages(PinnedMessagesQuerySpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
