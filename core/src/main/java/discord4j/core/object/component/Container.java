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
import discord4j.rest.util.Color;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A container component for message.
 * <br>
 * Currently discord just support these components being added:
 * <ul>
 *     <li>{@link ActionRow}</li>
 *     <li>{@link TextDisplay}</li>
 *     <li>{@link Section}</li>
 *     <li>{@link MediaGallery}</li>
 *     <li>{@link Separator}</li>
 *     <li>{@link File}</li>
 * </ul>
 *
 * @apiNote This component require {@link discord4j.core.object.entity.Message.Flag#IS_COMPONENTS_V2}
 * @see <a href="https://discord.com/developers/docs/components/reference#container">Containers</a>
 */
public class Container extends LayoutComponent implements TopLevelMessageComponent {

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param components The child components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(ICanBeUsedInContainerComponent... components) {
        return new Container(null, null, false, Arrays.asList(components));
    }

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param color      The accent color
     * @param components The child components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(@Nullable Color color, ICanBeUsedInContainerComponent... components) {
        return new Container(null, color, false, Arrays.asList(components));
    }

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param spoiler    If the container should be blurred
     * @param components The child components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(boolean spoiler, ICanBeUsedInContainerComponent... components) {
        return new Container(null, null, spoiler, Arrays.asList(components));
    }

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param color      The accent color
     * @param spoiler    If the container should be blurred
     * @param components The child components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(@Nullable Color color, boolean spoiler, ICanBeUsedInContainerComponent... components) {
        return new Container(null, color, spoiler, Arrays.asList(components));
    }

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param components The child components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(List<? extends ICanBeUsedInContainerComponent> components) {
        return new Container(null, null, false, components);
    }

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param spoiler    If the container should be blurred
     * @param components The child components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(boolean spoiler, List<? extends ICanBeUsedInContainerComponent> components) {
        return new Container(null, null, spoiler, components);
    }

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param color      The accent color
     * @param components The components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(@Nullable Color color, List<? extends ICanBeUsedInContainerComponent> components) {
        return new Container(null, color, false, components);
    }

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param color      The accent color
     * @param spoiler    If the container should be blurred
     * @param components The components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(@Nullable Color color, boolean spoiler,
                               List<? extends ICanBeUsedInContainerComponent> components) {
        return new Container(null, color, spoiler, components);
    }

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param id         the component id
     * @param components The child components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(int id, ICanBeUsedInContainerComponent... components) {
        return new Container(id, null, false, Arrays.asList(components));
    }

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param id         the component id
     * @param spoiler    If the container should be blurred
     * @param components The child components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(int id, boolean spoiler, ICanBeUsedInContainerComponent... components) {
        return new Container(id, null, spoiler, Arrays.asList(components));
    }

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param id         the component id
     * @param color      The accent color
     * @param components The child components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(int id, @Nullable Color color, ICanBeUsedInContainerComponent... components) {
        return new Container(id, color, false, Arrays.asList(components));
    }

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param id         the component id
     * @param color      The accent color
     * @param spoiler    If the container should be blurred
     * @param components The child components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(int id, @Nullable Color color, boolean spoiler,
                               ICanBeUsedInContainerComponent... components) {
        return new Container(id, color, spoiler, Arrays.asList(components));
    }

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param id         the component id
     * @param components The child components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(int id, List<? extends ICanBeUsedInContainerComponent> components) {
        return new Container(id, null, false, components);
    }

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param id         the component id
     * @param color      The accent color
     * @param components The components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(int id, @Nullable Color color, List<? extends ICanBeUsedInContainerComponent> components) {
        return new Container(id, color, false, components);
    }

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param id         the component id
     * @param spoiler    If the container should be blurred
     * @param components The components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(int id, boolean spoiler, List<? extends ICanBeUsedInContainerComponent> components) {
        return new Container(id, null, spoiler, components);
    }

    /**
     * Creates a {@link Container} with the given components.
     *
     * @param id         the component id
     * @param color      The accent color
     * @param spoiler    If the container should be blurred
     * @param components The components of the container.
     * @return A {@link Container} containing the given components.
     */
    public static Container of(int id, @Nullable Color color, boolean spoiler,
                               List<? extends ICanBeUsedInContainerComponent> components) {
        return new Container(id, color, spoiler, components);
    }

    protected Container(@Nullable Integer id, @Nullable Color color, boolean spoiler,
                        List<? extends ICanBeUsedInContainerComponent> components) {
        this(
                MessageComponent.getBuilder(Type.CONTAINER)
                        .id(Possible.ofNullable(id))
                        .spoiler(spoiler)
                        .components(components.stream()
                                .filter(c -> c instanceof MessageComponent)
                                .map(BaseMessageComponent::getData)
                                .collect(Collectors.toList()))
                        .accentColor(Possible.of(Optional.ofNullable(color).map(Color::getRGB)))
                        .build()
        );
    }

    Container(ComponentData data) {
        super(data);
    }

    /**
     * Create a new {@link Container} instance from {@code this}, adding a given component.
     *
     * @param component the child component to be added
     * @return a {@link Container} containing the existing and added components
     */
    public Container withAddedComponent(ICanBeUsedInContainerComponent component) {
        List<BaseMessageComponent> components = new ArrayList<>(getChildren());
        components.add(component);
        return new Container(ComponentData.builder()
                .from(this.getData())
                .components(components.stream()
                        .map(BaseMessageComponent::getData)
                        .collect(Collectors.toList()))
                .build());
    }

    /**
     * Create a new {@link Container} instance from {@code this}, adding given components.
     *
     * @param components the child components to be added
     * @return a {@link Container} containing the existing and added components
     */
    public final Container withAddedComponents(ICanBeUsedInContainerComponent... components) {
        List<BaseMessageComponent> componentsToAdd = new ArrayList<>(getChildren());
        componentsToAdd.addAll(Arrays.asList(components));
        return new Container(ComponentData.builder().from(this.getData())
                .components(componentsToAdd.stream()
                        .map(BaseMessageComponent::getData)
                        .collect(Collectors.toList()))
                .build());
    }

    /**
     * Create a new {@link Container} instance from {@code this}, adding given components.
     *
     * @param components the child components to be added
     * @return a {@link Container} containing the existing and added components
     */
    public Container withAddedComponents(List<? extends ICanBeUsedInContainerComponent> components) {
        return new Container(ComponentData.builder()
                .from(this.getData())
                .components(Stream.concat(getChildren().stream(), components.stream())
                        .filter(c -> c instanceof MessageComponent)
                        .map(BaseMessageComponent::getData)
                        .collect(Collectors.toList()))
                .build());
    }

    /**
     * Create a new {@link Container} instance from {@code this}, removing any existing component by {@code customId}.
     *
     * @param componentId the id of the component to remove
     * @return a {@link Container} containing all components that did not match the given {@code customId}
     */
    public Container withRemovedComponent(int componentId) {
        List<MessageComponent> components = new ArrayList<>(getChildren());
        components.removeIf(messageComponent -> componentId == messageComponent.getId());

        return new Container(ComponentData.builder()
                .from(this.getData())
                .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
                .build());
    }

    /**
     * Create a new {@link Container} instance from {@code this}, changing the accent color or removing it
     *
     * @param color The new accent color or null to remove it
     * @return The newly created {@link Container}
     */
    public Container withColor(@Nullable Color color) {
        return new Container(ComponentData.builder()
                .from(this.getData())
                .accentColor(Possible.of(Optional.ofNullable(color).map(Color::getRGB)))
                .build());
    }

    /**
     * Gets the accent color for this container.
     *
     * @return An optional color.
     */
    public Optional<Color> getColor() {
        return Possible.flatOpt(this.getData().accentColor()).map(Color::of);
    }

    /**
     * Gets if this component is a spoiler.
     *
     * @return {@code true} if is spoiler, false otherwise
     */
    public boolean isSpoiler() {
        return this.getData().spoiler().toOptional().orElse(false);
    }
}
