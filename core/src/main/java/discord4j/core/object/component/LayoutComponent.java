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
package discord4j.core.object.component;

import discord4j.common.annotations.Experimental;
import discord4j.discordjson.json.ComponentData;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A message component that determines how components are laid out in a message and has {@link ActionComponent}
 * children.
 */
@Experimental
public abstract class LayoutComponent extends MessageComponent {

    LayoutComponent(ComponentData data) {
        super(data);
    }

    public /*~~>*/List<MessageComponent> getChildren() {
        return getData().components().toOptional()
                .map(components -> components.stream()
                        .map(MessageComponent::fromData)
                        .collect(Collectors.toList()))
                .orElseThrow(IllegalStateException::new); // components should always be present on a layout component
    }
}
