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
import discord4j.discordjson.json.AllowedMentionsData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.List;

import static discord4j.core.spec.InternalSpecUtils.*;

@SpecStyle
@Value.Immutable(singleton = true)
interface MessageEditSpecGenerator extends Spec<MessageEditRequest> {

    @Value.Default
    @Nullable
    default Possible<String> content() {
        return Possible.absent();
    }

    @Value.Default
    @Nullable
    default Possible<EmbedCreateSpec> embed() {
        return Possible.absent();
    }

    @Value.Default
    @Nullable
    default Possible<AllowedMentions> allowedMentions() {
        return Possible.absent();
    }

    @Value.Default
    @Nullable
    default Possible<List<Message.Flag>> flags() {
        return Possible.absent();
    }

    @Override
    default MessageEditRequest asRequest() {
        Possible<EmbedData> embed = mapPossible(embed(), EmbedCreateSpec::asRequest);
        Possible<AllowedMentionsData> allowedMentions = mapPossible(allowedMentions(), AllowedMentions::toData);
        Possible<Integer> flags = mapPossible(flags(), f -> f.stream()
                .mapToInt(Message.Flag::getValue)
                .reduce(0, (left, right) -> left | right));
        return MessageEditRequest.builder()
                .content(toPossibleOptional(content()))
                .embed(toPossibleOptional(embed))
                .allowedMentions(toPossibleOptional(allowedMentions))
                .flags(toPossibleOptional(flags))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@SpecStyle
@Value.Immutable(builder = false)
abstract class MessageEditMonoGenerator extends Mono<Message> implements MessageEditSpecGenerator {

    abstract Message message();

    @Override
    public void subscribe(CoreSubscriber<? super Message> actual) {
        message().edit(MessageEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}