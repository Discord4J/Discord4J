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

import discord4j.core.object.component.LayoutComponent;
import discord4j.discordjson.json.MessageRequestBase;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapPossibleOptional;

public interface MessageRequestSpec<T> extends Spec<T> {

    Possible<Optional<String>> content();

    Possible<Optional<List<EmbedCreateSpec>>> embeds();

    Possible<Optional<AllowedMentions>> allowedMentions();

    Possible<Optional<List<LayoutComponent>>> components();

    default MessageRequestBase getBaseRequest() {
        return MessageRequestBase.builder()
                .content(content())
                .embeds(mapPossibleOptional(embeds(), embeds -> embeds.stream()
                        .map(EmbedCreateSpec::asRequest)
                        .collect(Collectors.toList())))
                .allowedMentions(mapPossibleOptional(allowedMentions(), AllowedMentions::toData))
                .components(mapPossibleOptional(components(), components -> components.stream()
                        .map(LayoutComponent::getData)
                        .collect(Collectors.toList())))
                .build();
    }
}
