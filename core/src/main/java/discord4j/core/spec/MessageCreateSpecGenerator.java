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
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.MultipartRequest;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static discord4j.core.spec.InternalSpecUtils.mapNullable;
import static discord4j.core.spec.InternalSpecUtils.toPossible;

@SpecStyle
@Value.Immutable(singleton = true)
interface MessageCreateSpecGenerator extends Spec<MultipartRequest<MessageCreateRequest>> {

    @Nullable
    String content();

    @Nullable
    String nonce();

    @Value.Default
    default boolean tts() {
        return false;
    }

    @Nullable
    EmbedCreateSpec embed();

    @Value.Default
    default List<MessageCreateFields.File> files() {
        return Collections.emptyList();
    }

    @Value.Default
    default List<MessageCreateFields.FileSpoiler> fileSpoilers() {
        return Collections.emptyList();
    }

    @Nullable
    AllowedMentions allowedMentions();

    @Nullable
    Snowflake messageReference();

    @Override
    default MultipartRequest<MessageCreateRequest> asRequest() {
        MessageCreateRequest json = MessageCreateRequest.builder()
                .content(toPossible(content()))
                .nonce(toPossible(nonce()))
                .tts(tts())
                .embed(toPossible(mapNullable(embed(), EmbedCreateSpecGenerator::asRequest)))
                .allowedMentions(toPossible(mapNullable(allowedMentions(), AllowedMentions::toData)))
                .messageReference(toPossible(mapNullable(messageReference(),
                        ref -> MessageReferenceData.builder()
                                .messageId(ref.asString())
                                .build())))
                .build();
        return MultipartRequest.ofRequestAndFiles(json, Stream.concat(files().stream(), fileSpoilers().stream())
                .map(MessageCreateFields.File::asRequest)
                .collect(Collectors.toList()));
    }
}

@SuppressWarnings("immutables:subtype")
@SpecStyle
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