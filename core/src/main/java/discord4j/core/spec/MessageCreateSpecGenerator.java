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
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.discordjson.json.MessageReferenceData;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable(singleton = true)
interface MessageCreateSpecGenerator extends FilesMessageRequestSpec<MessageCreateRequest> {

    Possible<String> nonce();

    Possible<Snowflake> messageReference();

    @Value.Default
    default boolean tts() {
        return false;
    }

    @Override
    default MessageCreateRequest getMessageRequest() {
        return MessageCreateRequest.builder()
                .from(getBaseRequest())
                .nonce(nonce())
                .messageReference(mapPossible(messageReference(),
                        ref -> MessageReferenceData.builder()
                                .messageId(ref.asString())
                                .build()))
                .tts(tts())
                .build();
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