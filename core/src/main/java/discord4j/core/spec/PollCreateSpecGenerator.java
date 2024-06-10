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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.spec;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.poll.Poll;
import discord4j.discordjson.json.PollAnswerObject;
import discord4j.discordjson.json.PollCreateData;
import discord4j.discordjson.json.PollMediaObject;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.List;

@Value.Immutable
public interface PollCreateSpecGenerator extends Spec<PollCreateData> {

    Possible<PollMediaObject> question();

    Possible<List<PollAnswerObject>> answers();

    Possible<Integer> duration();

    @JsonProperty("allow_multiselect")
    Possible<Boolean> allowMultiselect();

    @JsonProperty("layout_type")
    Possible<Integer> layoutType();

    @Override
    default PollCreateData asRequest() {
        return PollCreateData.builder()
            .question(question())
            .answers(answers())
            .duration(duration())
            .allowMultiselect(allowMultiselect())
            .layoutType(layoutType())
            .build();
    }

}

@Value.Immutable(builder = false)
abstract class PollCreateMonoGenerator extends Mono<Poll> implements PollCreateSpecGenerator {

    abstract MessageChannel channel();

    @Override
    public void subscribe(CoreSubscriber<? super Poll> coreSubscriber) {
        channel().createMessage(MessageCreateSpec.builder()
            .poll(this.asRequest())
            .build())
            .map(Message::getPoll)
            .flatMap(Mono::justOrEmpty)
            .subscribe(coreSubscriber);
    }

    @Override
    public abstract String toString();

}
