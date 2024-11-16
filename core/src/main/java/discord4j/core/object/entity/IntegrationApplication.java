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
package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.util.ImageUtil;
import discord4j.discordjson.json.IntegrationApplicationData;
import discord4j.rest.util.Image;

import java.util.Objects;
import java.util.Optional;

/**
 * A Discord Integration Application.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#integration-object">Integration
 * Application Resource</a>
 */
public class IntegrationApplication implements Entity {

    /** The path for application icon image URLs. */
    private static final String ICON_IMAGE_PATH = "app-icons/%s/%s";

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final IntegrationApplicationData data;

    /**
     * Constructs a {@code IntegrationApplication} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public IntegrationApplication(final GatewayDiscordClient gateway, final IntegrationApplicationData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the data of the account.
     *
     * @return The data of the account.
     */
    public IntegrationApplicationData getData() {
        return data;
    }

    /**
     * Gets the name of the app.
     *
     * @return The name of the app.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the icon URL of the application, if present.
     *
     * @param format The format for the URL.
     * @return The icon URL of the application, if present.
     */
    public Optional<String> getIconUrl(final Image.Format format) {
        return data.icon()
                .map(icon -> ImageUtil.getUrl(String.format(ICON_IMAGE_PATH, getId().asString(), icon), format));
    }

    /**
     * Gets the description of the app.
     *
     * @return The description of the app.
     */
    public String getDescription() {
        return data.description();
    }

    /**
     * Gets the summary of the app.
     *
     * @return The description of the app.
     * @deprecated Discord just return an empty string you can consider use {@link #getDescription()}
     */
    @Deprecated
    public String getSummary() {
        return data.summary();
    }

    /**
     * Gets the bot associated with this application, if present.
     *
     * @return The bot associated with this application, if present.
     */
    public Optional<User> getBot() {
        return data.bot().toOptional()
                .map(data -> new User(gateway, data));
    }

}

