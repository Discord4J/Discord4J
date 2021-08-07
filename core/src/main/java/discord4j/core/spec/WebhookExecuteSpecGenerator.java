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

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Webhook;
import discord4j.discordjson.json.WebhookExecuteRequest;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

@Value.Immutable(singleton = true)
interface WebhookExecuteSpecGenerator extends FilesMessageRequestSpec<WebhookExecuteRequest> {

    @Value.Default
    default boolean tts() {
        return false;
    }

    Possible<String> username();

    Possible<String> avatarUrl();

    @Override
    default WebhookExecuteRequest getMessageRequest() {
        return WebhookExecuteRequest.builder()
                .from(getBaseRequest())
                .tts(tts())
                .username(username())
                .avatarUrl(avatarUrl())
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class WebhookExecuteMonoGenerator extends Mono<Message> implements WebhookExecuteSpecGenerator {

    abstract boolean waitForMessage();

    abstract Webhook webhook();

    @Override
    public void subscribe(CoreSubscriber<? super Message> actual) {
        webhook().execute(waitForMessage(), WebhookExecuteSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
