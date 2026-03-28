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
package discord4j.core.object.component;

import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.possible.Possible;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Label extends LayoutComponent implements TopLevelModalComponent {

    /**
     * Creates a {@link Label} with the given component.
     *
     * @param component The component of the label.
     * @return A {@link Label} containing the given components.
     */
    public static Label of(String label, ICanBeUsedInLabelComponent component) {
        return new Label(null, label, null, component);
    }

    /**
     * Creates a {@link Label} with the given component.
     *
     * @param component The component of the label.
     * @param description The description of the label.
     * @return A {@link Label} containing the given components.
     */
    public static Label of(String label, String description, ICanBeUsedInLabelComponent component) {
        return new Label(null, label, description, component);
    }

    /**
     * Creates a {@link Label} with the given component.
     *
     * @param component The component of the label.
     * @return A {@link Label} containing the given components.
     */
    public static Label of(int componentId, String label, ICanBeUsedInLabelComponent component) {
        return new Label(componentId, label, null, component);
    }

    /**
     * Creates a {@link Label} with the given component.
     *
     * @param component The component of the label.
     * @param description The description of the label.
     * @return A {@link Label} containing the given components.
     */
    public static Label of(int componentId, String label, String description, ICanBeUsedInLabelComponent component) {
        return new Label(componentId, label, description, component);
    }


    protected Label(@Nullable Integer componentId, String label, @Nullable String description, ICanBeUsedInLabelComponent component) {
        this(
            MessageComponent.getBuilder(Type.LABEL)
                .id(Possible.ofNullable(componentId))
                .label(Possible.of(Optional.of(label)))
                .component((component).getData())
                .description(Possible.ofNullable(description).map(Optional::ofNullable))
                .build()
        );
    }

    Label(ComponentData data) {
        super(data);
    }

    /**
     * Create a new {@link Label} instance from {@code this}, using a given component.
     *
     * @param component the child component to be replaced
     * @return a {@link Label} containing the new component
     */
    public Label withComponent(ICanBeUsedInLabelComponent component) {
        return new Label(ComponentData.builder()
            .from(getData())
            .component(Possible.of((component).getData()))
            .build());
    }

    /**
     * Get the direct children of this {@link LayoutComponent}
     *
     * @return The direct children of this component
     */
    @Override
    public List<MessageComponent> getChildren() {
        return this.getData()
            .component()
            .toOptional()
            .map(MessageComponent::fromData)
            .map(Collections::singletonList)
            .orElse(Collections.emptyList());
    }

    /**
     * Retrieves the component attached to this {@link Label}.
     *
     * @return The {@link MessageComponent} attached to this label.
     * @see ICanBeUsedInLabelComponent
     */
    public MessageComponent getComponent() {
        return this.getChildren().get(0);
    }
}
