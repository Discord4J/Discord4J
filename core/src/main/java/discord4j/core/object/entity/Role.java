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
import discord4j.core.object.data.stored.RoleBean;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.RoleEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.core.util.OrderUtil;
import discord4j.rest.json.request.PositionModifyRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.awt.Color;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A Discord role.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/permissions#role-object">Role Object</a>
 */
public final class Role implements Entity {

    /** The default {@link Color} of a {@code Role}. */
    public static final Color DEFAULT_COLOR = new Color(0, true);

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final RoleBean data;

    /** The ID of the guild this role is associated to. */
    private final long guildId;

    /**
     * Constructs a {@code Role} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param guildId The ID of the guild this role is associated to.
     */
    public Role(final ServiceMediator serviceMediator, final RoleBean data, final long guildId) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
        this.guildId = guildId;
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    public int getRawPosition() {
        return data.getPosition();
    }

    /**
     * Requests to retrieve the position of the role relative to other roles in the guild.
     * <p>
     * This is determined by the index of this role in the {@link OrderUtil#orderRoles(Flux) sorted} list of roles of the guild.
     * <p>
     * Warning: Because this method must sort the guild roles, it is inefficient to make repeated invocations for the
     * same set of roles (meaning that roles haven't been added or removed). For example, instead of writing:
     * <pre>
     * {@code
     * guild.getRoles()
     *   .flatMap(r -> r.getPosition().map(pos -> r.getName() + " : " + pos))
     * }
     * </pre>
     * It would be much more efficient to write:
     * <pre>
     * {@code
     * guild.getRoles()
     *   .transform(OrderUtil::orderRoles)
     *   .index((pos, r) -> r.getName() + " : " + pos)
     * }
     * </pre>
     *
     * @return A {@link Mono} where, upon successful completion, emits the position of the role. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Integer> getPosition() {
        return getGuild()
                .flatMapMany(Guild::getRoles)
                .transform(OrderUtil::orderRoles)
                .collectList()
                .map(roles -> roles.indexOf(this));
    }

    /**
     * Gets the role name.
     *
     * @return The role name.
     */
    public String getName() {
        return data.getName();
    }

    /**
     * Gets the color assigned to this role.
     *
     * @return The color assigned to this role.
     */
    public Color getColor() {
        return new Color(data.getColor(), true);
    }

    /**
     * Gets whether if this role is pinned in the user listing.
     *
     * @return {@code true} if this role is pinned in the user listing, {@code false} otherwise.
     */
    public boolean isHoisted() {
        return data.isHoist();
    }

    /**
     * Gets the permissions assigned to this role.
     *
     * @return The permissions assigned to this role.
     */
    public PermissionSet getPermissions() {
        return PermissionSet.of(data.getPermissions());
    }

    /**
     * Gets whether this role is managed by an integration.
     *
     * @return {@code true} if this role is managed by an integration, {@code false} otherwise.
     */
    public boolean isManaged() {
        return data.isManaged();
    }

    /**
     * Gets whether this role is mentionable.
     *
     * @return {@code true} if this role is mentionable, {@code false} otherwise.
     */
    public boolean isMentionable() {
        return data.isMentionable();
    }

    /**
     * Gets whether this role corresponds to the @everyone role.
     *
     * @return {@code true} if this role represents the @everyone role, {@code false} otherwise.
     */
    public boolean isEveryone() {
        return getId().equals(getGuildId());
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
     * Requests to retireve the guild this role is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this role is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the <i>raw</i> mention. This is the format utilized to directly mention another role (assuming the role
     * exists in context of the mention).
     *
     * @return The <i>raw</i> mention.
     */
    public String getMention() {
        return "<@&" + getId().asString() + ">";
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.getId());
    }

    /**
     * Requests to edit this role.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link RoleEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Role}. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<Role> edit(final Consumer<? super RoleEditSpec> spec) {
        final RoleEditSpec mutatedSpec = new RoleEditSpec();
        spec.accept(mutatedSpec);

        return serviceMediator.getRestClient().getGuildService()
                .modifyGuildRole(getGuildId().asLong(), getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason())
                .map(RoleBean::new)
                .map(bean -> new Role(serviceMediator, bean, getGuildId().asLong()))
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Requests to delete this role.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the role has been deleted. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return delete(null);
    }

    /**
     * Requests to delete this role while optionally specifying the reason.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the role has been deleted. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(@Nullable final String reason) {
        return serviceMediator.getRestClient().getGuildService()
                .deleteGuildRole(getGuildId().asLong(), getId().asLong(), reason)
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Requests to change this role's position.
     *
     * @param position The position to change for this role.
     * @return A {@link Flux} that continually emits all the {@link Role roles} associated to this role's
     * {@link #getGuild() guild}. If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<Role> changePosition(final int position) {
        final PositionModifyRequest[] requests = { new PositionModifyRequest(getId().asLong(), position) };

        return serviceMediator.getRestClient().getGuildService()
                .modifyGuildRolePositions(getGuildId().asLong(), requests)
                .map(RoleBean::new)
                .map(bean -> new Role(serviceMediator, bean, getGuildId().asLong()))
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
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
        return "Role{" +
                "data=" + data +
                ", guildId=" + guildId +
                '}';
    }
}
