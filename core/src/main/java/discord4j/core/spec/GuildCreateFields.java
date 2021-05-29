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

import discord4j.core.object.entity.channel.Channel;
import discord4j.discordjson.json.PartialChannelCreateRequest;
import org.immutables.value.Value;

@InlineFieldStyle
@Value.Enclosing
public final class GuildCreateFields {

    private GuildCreateFields() {
        throw new AssertionError();
    }

    @Value.Immutable
    public interface PartialChannel extends Spec<PartialChannelCreateRequest> {

        static PartialChannel of(String name, Channel.Type type) {
            return ImmutableGuildCreateFields.PartialChannel.of(name, type);
        }

        String name();

        Channel.Type type();

        @Override
        default PartialChannelCreateRequest asRequest() {
            return PartialChannelCreateRequest.builder()
                    .name(name())
                    .type(type().getValue())
                    .build();
        }
    }
}
