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
import discord4j.discordjson.json.ImmutableComponentData;
import discord4j.rest.util.Color;
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A container component for message.
 * <br>
 * Currently discord just support this components being added:
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
 * @see <a href="https://discord.com/developers/docs/interactions/message-components#???">Containers</a>
 */
public class Container extends LayoutComponent implements TopLevelMessageComponent {

    /**
     * Creates an {@code SectionComponent} with the given components.
     *
     * @param components The child components of the container.
     * @return An {@code SectionComponent} containing the given components.
     */
    public static Container of(MessageComponent... components) {
        return of(Arrays.asList(components));
    }

    /**
     * Creates an {@code SectionComponent} with the given components.
     *
     * @param components The child components of the container.
     * @return An {@code SectionComponent} containing the given components.
     */
    public static Container of(Color color, MessageComponent... components) {
        return of(color, false, Arrays.asList(components));
    }

    /**
     * Creates an {@code SectionComponent} with the given components.
     *
     * @param components The child components of the container.
     * @return An {@code SectionComponent} containing the given components.
     */
    public static Container of(List<? extends MessageComponent> components) {
        return of(null, false, components);
    }

    /**
     * Creates an {@code SectionComponent} with the given components.
     *
     * @param components The components of the container.
     * @return An {@code SectionComponent} containing the given components.
     */
    public static Container of(@Nullable Color color, boolean spoiler, List<? extends MessageComponent> components) {
        ImmutableComponentData.Builder componentData = MessageComponent.getBuilder(Type.CONTAINER).spoiler(spoiler)
                .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()));

        if (color != null) {
            componentData.accentColor(color.getRGB());
        }

        return new Container(componentData.build());
    }

    Container(ComponentData data) {
        super(data);
    }

    /**
     * Create a new {@link Container} instance from {@code this}, adding a given component.
     *
     * @param component the child component to be added
     * @return an {@code SectionComponent} containing the existing and added components
     */
    private Container withAddedComponent(MessageComponent component) {
        List<MessageComponent> components = new ArrayList<>(getChildren());
        components.add(component);
        return new Container(ComponentData.builder()
            .type(this.getType().getValue())
            .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
            .build());
    }

    /**
     * Create a new {@link Container} instance from {@code this}, removing any existing component by {@code customId}.
     *
     * @param customId the customId of the component to remove
     * @return an {@code SectionComponent} containing all components that did not match the given {@code customId}
     */
    public Container withRemovedComponent(String customId) {
        List<MessageComponent> components = getChildren(customId);
        return new Container(ComponentData.builder()
            .type(this.getType().getValue())
            .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
            .build());
    }

    /**
     * Gets the accent color for this container.
     *
     * @return An optional color.
     */
    public Optional<Color> getColor() {
        return this.getData().accentColor().toOptional()
                .map(Color::of);
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
