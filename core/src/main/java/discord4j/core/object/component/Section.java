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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A section component for message.
 * <br>
 * <ul>
 *     <li>Currently accessory only support {@link Thumbnail} and {@link Button}</li>
 *     <li>Currently the components valid are {@link TextDisplay}</li>
 * </ul>
 *
 * @apiNote This component require {@link discord4j.core.object.entity.Message.Flag#IS_COMPONENTS_V2}
 * @see <a href="https://discord.com/developers/docs/interactions/message-components#???">Section</a>
 */
public class Section extends LayoutComponent implements TopLevelMessageComponent, ICanBeUsedInContainerComponent {

    /**
     * Creates a {@link Section} with the given components.
     *
     * @param accessory The accessory component of the section
     * @param components The components of the section
     * @param <C> The type of component, must implement {@link ICanBeUsedInSectionComponent}
     * @return A {@link Section} containing the given components
     */
    public static <C extends MessageComponent & ICanBeUsedInSectionComponent> Section of(IAccessoryComponent accessory, C... components) {
        return of(accessory, Arrays.asList(components));
    }

    /**
     * Creates a {@link Section} with the given components.
     *
     * @param accessory The accessory component of the section
     * @param components The components of the section
     * @param <C> The type of component, must implement {@link ICanBeUsedInSectionComponent}
     * @return A {@link Section} containing the given components
     */
    public static <C extends MessageComponent & ICanBeUsedInSectionComponent> Section of(IAccessoryComponent accessory, List<C> components) {
        return new Section(MessageComponent.getBuilder(Type.SECTION)
            .accessory(accessory.getData())
            .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
            .build());
    }

    /**
     * Creates a {@link Section} with the given components.
     *
     * @param id the component id
     * @param accessory The accessory component of the section
     * @param components The components of the section
     * @param <C> The type of component, must implement {@link ICanBeUsedInSectionComponent}
     * @return A {@link Section} containing the given components
     */
    public static <C extends MessageComponent & ICanBeUsedInSectionComponent> Section of(int id, IAccessoryComponent accessory, C... components) {
        return of(id, accessory, Arrays.asList(components));
    }

    /**
     * Creates a {@link Section} with the given components.
     *
     * @param id the component id
     * @param accessory The accessory component of the section
     * @param components The components of the section
     * @param <C> The type of component, must implement {@link ICanBeUsedInSectionComponent}
     * @return A {@link Section} containing the given components
     */
    public static <C extends MessageComponent & ICanBeUsedInSectionComponent> Section of(int id, IAccessoryComponent accessory, List<C> components) {
        return new Section(MessageComponent.getBuilder(Type.SECTION)
            .id(id)
            .accessory(accessory.getData())
            .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
            .build());
    }


    protected <C extends MessageComponent & ICanBeUsedInSectionComponent> Section(Integer id, IAccessoryComponent accessory, List<C> components) {
        this(MessageComponent.getBuilder(Type.SECTION)
            .id(Possible.ofNullable(id))
            .accessory(accessory.getData())
            .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
            .build());
    }

    Section(ComponentData data) {
        super(data);
    }

    /**
     * Gets the accessory for this section.
     *
     * @return An component
     */
    public IAccessoryComponent getAccessory() {
        return this.getData()
            .accessory()
            .toOptional()
            .map(MessageComponent::fromData)
            .filter(IAccessoryComponent.class::isInstance)
            .map(IAccessoryComponent.class::cast)
            .orElseThrow(IllegalStateException::new); // accessory should always exist in a section
    }

    /**
     * Create a new {@link Section} instance from {@code this}, adding a given component.
     *
     * @param component the child component to be added
     * @return an {@code Section} containing the existing and added components
     */
    public Section withAddedComponent(ActionComponent component) {
        List<MessageComponent> components = new ArrayList<>(getChildren());
        components.add(component);
        return new Section(ComponentData.builder()
            .from(getData())
            .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
            .build());
    }

    /**
     * Create a new {@link Section} instance from {@code this}, removing any existing component by {@code customId}.
     *
     * @param componentId the customId of the component to remove
     * @return an {@code Section} containing all components that did not match the given {@code customId}
     */
    public Section withRemovedComponent(int componentId) {
        List<MessageComponent> components = getChildren();
        components.removeIf(messageComponent -> componentId == messageComponent.getId());

        return new Section(ComponentData.builder()
            .from(getData())
            .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
            .build());
    }
}
