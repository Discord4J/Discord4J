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
package discord4j.core.object.component.kind;

import discord4j.common.annotations.Experimental;
import discord4j.core.object.component.Component;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.component.attribute.IHasChildsComponentData;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A message component that determines how components are laid out in a message and has {@link Component}
 * children.
 */
@Experimental
public abstract class LayoutComponent<D extends ComponentData & IHasChildsComponentData> extends Component<@NonNull D> {

    protected LayoutComponent(D data) {
        super(data);
    }

    /**
     * Get the direct children of this {@link LayoutComponent}
     *
     * @return The direct children of this component
     */
    public List<Component<?>> getChildren() {
        return this.getData()
                .components()
                .stream()
                .map(Component::fromData)
                .collect(Collectors.toList());
    }

    /**
     * Get all the children of this {@link LayoutComponent}: the direct children and all the children of the children...
     *
     * @return All the children of this component
     */
    public List<BaseComponent> getAllChildren() {
        List<BaseComponent> components = new ArrayList<>();

        for (Component<?> child : getChildren()) {
            if (child instanceof LayoutComponent) {
                components.addAll(((LayoutComponent<?>) child).getAllChildren());
            }

            components.add(child);
        }

        return components;
    }

}
