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
package discord4j.core.object.entity;

import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.data.ApplicationInfoBean;
import discord4j.core.object.util.Image;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static discord4j.core.object.util.Image.Format.JPEG;
import static discord4j.core.object.util.Image.Format.PNG;
import static discord4j.core.object.util.Image.Format.WEB_P;

/** Represents the Current (typically) Application Information. */
public final class ApplicationInfo implements Entity {

    /** The path for application icon images for {@link Image}. */
    private static final String ICON_IMAGE_PATH = "app-icons/%s/%s";

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final ApplicationInfoBean data;

    /**
     * Constructs a {@code ApplicationInfo} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ApplicationInfo(final ServiceMediator serviceMediator, final ApplicationInfoBean data) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.getId());
    }

    /**
     * Gets the name of the app.
     *
     * @return The name of the app.
     */
    public String getName() {
        return data.getName();
    }

    /**
     * Gets the {@link Image images} for this application's icon. May be empty if no icon is set.
     *
     * @return The {@link Image images} for this application's icon. May be empty if no icon is set.
     */
    public Set<Image> getIcons() {
        final String icon = data.getIcon();
        return (icon == null)
                ? Collections.emptySet()
                : Image.of(String.format(ICON_IMAGE_PATH, getId().asString(), icon), PNG, JPEG, WEB_P);
    }

    /**
     * Gets the description of the app, if present.
     *
     * @return The description of the app, if present.
     */
    public Optional<String> getDescription() {
        return Optional.ofNullable(data.getDescription());
    }

    /**
     * Gets whether only the app owner can join the app's bot to guilds.
     *
     * @return {@code true} if only the app owner can join the app's bot to guilds, {@code false} otherwise.
     */
    public boolean isPublic() {
        return data.isBotPublic();
    }

    /**
     * Gets whether the app's bot will only join upon completion of the full OAuth2 code grant flow.
     *
     * @return {@code true} if the app's bot will only join upon completion of the full OAuth2 code grant flow,
     * {@code false} otherwise.
     */
    public boolean requireCodeGrant() {
        return data.isBotRequireCodeGrant();
    }

    /**
     * Gets the ID of the owner of the application.
     *
     * @return The ID of the owner of the application.
     */
    public Snowflake getOwnerId() {
        return Snowflake.of(data.getOwnerId());
    }

    /**
     * Requests to retrieve the owner of the application.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User owner} of the application. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getOwner() {
        return serviceMediator.getClient().getUserById(getOwnerId());
    }
}
