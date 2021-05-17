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

import discord4j.discordjson.json.ApplicationInfoData;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.rest.util.Image;
import discord4j.common.util.Snowflake;
import discord4j.core.util.EntityUtil;
import discord4j.core.util.ImageUtil;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.*;

/**
 * Represents the Current (typically) Application Information.
 *
 * @see <a href="https://discord.com/developers/docs/resources/application">Application Resource</a>
 */
public final class ApplicationInfo implements Entity {

    /** The path for application icon image URLs. */
    private static final String ICON_IMAGE_PATH = "app-icons/%s/%s";

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ApplicationInfoData data;

    /**
     * Constructs a {@code ApplicationInfo} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ApplicationInfo(final GatewayDiscordClient gateway, final ApplicationInfoData data) {
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
     * Gets the data of the app.
     *
     * @return The data of the app.
     */
    public ApplicationInfoData getData() {
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
     * Gets the icon of the application.
     *
     * @param format The format in which to get the icon.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Image icon} of the application. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Image> getIcon(final Image.Format format) {
        return Mono.justOrEmpty(getIconUrl(format)).flatMap(Image::ofUrl);
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
     * Gets whether only the app owner can join the app's bot to guilds.
     *
     * @return {@code true} if only the app owner can join the app's bot to guilds, {@code false} otherwise.
     */
    public boolean isPublic() {
        return data.botPublic();
    }

    /**
     * Gets whether the app's bot will only join upon completion of the full OAuth2 code grant flow.
     *
     * @return {@code true} if the app's bot will only join upon completion of the full OAuth2 code grant flow,
     * {@code false} otherwise.
     */
    public boolean requireCodeGrant() {
        return data.botRequireCodeGrant();
    }

    /**
     * Gets the url of the app's terms of service, if present.
     *
     * @return The url of the app's terms of service, if present.
     */
    public Optional<String> getTermsOfServiceUrl() {
        return data.termsOfServiceUrl().toOptional();
    }

    /**
     * Gets the url of the app's privacy policy, if present.
     *
     * @return The url of the app's privacy policy, if present.
     */
    public Optional<String> getPrivacyPolicyUrl() {
        return data.privacyPolicyUrl().toOptional();
    }

    /**
     * Gets the ID of the owner of the application.
     *
     * @return The ID of the owner of the application.
     */
    public Snowflake getOwnerId() {
        return Snowflake.of(data.owner().id());
    }

    /**
     * Requests to retrieve the owner of the application.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User owner} of the application. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getOwner() {
        return gateway.getUserById(getOwnerId());
    }

    /**
     * Requests to retrieve the owner of the application, using the given retrieval strategy.
     *
     * @param retrievalStrategy The strategy to use to get the owner.
     * @return A {@link Mono} where, upon successful completion, emits the {@link User owner} of the application. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getOwner(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getUserById(getOwnerId());
    }

    /**
     * Gets the members of the application team, if the application belongs to a team.
     *
     * @return The members of the application's team, if the application belongs to a team.
     */
    public Optional<ApplicationTeam> getTeam() {
        return data.team().map(data -> new ApplicationTeam(gateway, data));
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return EntityUtil.hashCode(this);
    }

    @Override
    public String toString() {
        return "ApplicationInfo{" +
                "data=" + data +
                '}';
    }
}
