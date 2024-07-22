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

import discord4j.core.object.entity.ApplicationEmoji;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.discordjson.json.ApplicationEmojiCreateRequest;
import discord4j.rest.util.Image;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

@Value.Immutable
interface ApplicationEmojiCreateSpecGenerator extends Spec<ApplicationEmojiCreateRequest> {

    String name();

    Image image();

    @Override
    default ApplicationEmojiCreateRequest asRequest() {
        return ApplicationEmojiCreateRequest.builder()
                .name(name())
                .image(image().getDataUri())
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class ApplicationEmojiCreateMonoGenerator extends Mono<ApplicationEmoji> implements ApplicationEmojiCreateSpecGenerator {

    abstract ApplicationInfo applicationInfo();

    @Override
    public void subscribe(CoreSubscriber<? super ApplicationEmoji> actual) {
        applicationInfo().createEmoji(ApplicationEmojiCreateSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
