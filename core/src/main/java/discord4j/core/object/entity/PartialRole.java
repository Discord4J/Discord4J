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
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.util.ImageUtil;
import discord4j.discordjson.json.PartialRoleDataFields;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.entity.RestRole;
import discord4j.rest.util.Color;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

/**
 * A Discord partial role.
 *
 */
public class PartialRole implements Entity {

    /** The path for role icon image URLs. */
    private static final String ICON_IMAGE_PATH = "role-icons/%s/%s";

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final PartialRoleDataFields data;

    /** A handle to make API requests associated to this entity. */
    private final RestRole rest;

    /** The ID of the guild this role is associated to. */
    private final long guildId;

    /**
     * Constructs a {@code Role} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param guildId The ID of the guild this role is associated to.
     */
    public PartialRole(final GatewayDiscordClient gateway, final PartialRoleDataFields data, final long guildId) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
        this.rest = RestRole.create(gateway.rest(), Snowflake.of(guildId), Snowflake.of(data.id()));
        this.guildId = guildId;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(this.getData().id());
    }

    /**
     * Gets the role name.
     *
     * @return The role name.
     */
    public String getName() {
        return this.getData().name();
    }

    /**
     * Gets the sorting position of the role.
     *
     * @return The sorting position of the role.
     */
    public int getRawPosition() {
        return this.getData().position();
    }

    /**
     * Gets the primary color assigned to this role.
     *
     * @return The primary color assigned to this role.
     */
    public Color getPrimaryColor() {
        return Color.of(this.getData().colors().primaryColor());
    }

    /**
     * Gets the secondary color assigned to this role.
     *
     * @return The secondary color assigned to this role.
     */
    public Optional<Color> getSecondaryColor() {
        return this.getData().colors().secondaryColor().map(Color::of);
    }

    /**
     * Gets the tertiary color assigned to this role.
     *
     * @return The tertiary color assigned to this role.
     */
    public Optional<Color> getTertiaryColor() {
        return this.getData().colors().tertiaryColor().map(Color::of);
    }

    /**
     * Gets the icon URL of the role, if present.
     *
     * @param format The format for the URL.
     * @return The icon URL of the role, if present.
     */
    public Optional<String> getIconUrl(final Image.Format format) {
        return Possible.flatOpt(data.icon())
                .map(icon -> ImageUtil.getUrl(String.format(ICON_IMAGE_PATH, getId().asString(), icon), format));
    }

    /**
     * Gets the Unicode Emoji of the role, if present.
     *
     * @return The Unicode Emoji of the role, if present.
     */
    public Optional<String> getUnicodeEmoji() {
        return Possible.flatOpt(data.unicodeEmoji());
    }

    /**
     * Gets the ID of the guild this role is associated to.
     *
     * @return The ID of the guild this role is associated to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the complete role.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Role role}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getRole() {
        return this.getClient().getGuildById(this.getGuildId()).flatMap(guild -> guild.getRoleById(this.getId()));
    }

    /**
     * Gets the data of the role.
     *
     * @return The data of the role.
     */
    public PartialRoleDataFields getData() {
        return this.data;
    }

    @Override
    public String toString() {
        return "PartialRole{" +
                "data=" + this.data +
                ", guildId=" + this.guildId +
                '}';
    }

}
