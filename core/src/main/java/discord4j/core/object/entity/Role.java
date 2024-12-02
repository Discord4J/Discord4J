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

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.RoleTags;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.RoleEditMono;
import discord4j.core.spec.RoleEditSpec;
import discord4j.core.spec.legacy.LegacyRoleEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.core.util.ImageUtil;
import discord4j.core.util.MentionUtil;
import discord4j.core.util.OrderUtil;
import discord4j.discordjson.json.RoleData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.entity.RestRole;
import discord4j.rest.util.Color;
import discord4j.rest.util.Image;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A Discord role.
 *
 * @see <a href="https://discord.com/developers/docs/topics/permissions#role-object">Role Object</a>
 */
public final class Role implements Entity {

    /** The default {@link Color} of a {@code Role}. */
    public static final Color DEFAULT_COLOR = Color.of(0);

    /** The path for role icon image URLs. */
    private static final String ICON_IMAGE_PATH = "role-icons/%s/%s";

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final RoleData data;

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
    public Role(final GatewayDiscordClient gateway, final RoleData data, final long guildId) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
        this.rest = RestRole.create(gateway.rest(), Snowflake.of(guildId), Snowflake.of(data.id()));
        this.guildId = guildId;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the data of the role.
     *
     * @return The data of the role.
     */
    public RoleData getData() {
        return data;
    }

    /**
     * Gets the sorting position of the role.
     *
     * @return The sorting position of the role.
     */
    public int getRawPosition() {
        return data.position();
    }

    /**
     * Requests to retrieve the position of the role relative to other roles in the guild.
     * <p>
     * This is determined by the index of this role in the {@link OrderUtil#orderRoles(Flux) sorted} list of roles of
     * the guild.
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
     * Requests to retrieve the position of the role relative to other roles in the guild, using the given retrieval
     * strategy.
     * <p>
     * This is determined by the index of this role in the {@link OrderUtil#orderRoles(Flux) sorted} list of roles of
     * the guild.
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
     * @param retrievalStrategy the strategy to use to get the guild and the other roles
     * @return A {@link Mono} where, upon successful completion, emits the position of the role. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Integer> getPosition(EntityRetrievalStrategy retrievalStrategy) {
        return getGuild(retrievalStrategy)
                .flatMapMany(guild -> guild.getRoles(retrievalStrategy))
                .transform(OrderUtil::orderRoles)
                .collectList()
                .map(roles -> roles.indexOf(this));
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
     * Gets the role name.
     *
     * @return The role name.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the color assigned to this role.
     *
     * @return The color assigned to this role.
     */
    public Color getColor() {
        return Color.of(data.color());
    }

    /**
     * Gets whether if this role is pinned in the user listing.
     *
     * @return {@code true} if this role is pinned in the user listing, {@code false} otherwise.
     */
    public boolean isHoisted() {
        return data.hoist();
    }

    /**
     * Gets the permissions assigned to this role.
     *
     * @return The permissions assigned to this role.
     */
    public PermissionSet getPermissions() {
        return PermissionSet.of(data.permissions());
    }

    /**
     * Gets whether this role is managed by an integration.
     *
     * @return {@code true} if this role is managed by an integration, {@code false} otherwise.
     */
    public boolean isManaged() {
        return data.managed();
    }

    /**
     * Gets whether this role is mentionable.
     *
     * @return {@code true} if this role is mentionable, {@code false} otherwise.
     */
    public boolean isMentionable() {
        return data.mentionable();
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
     * Requests to retrieve the guild this role is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this role is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return gateway.getGuildById(getGuildId());
    }

    /**
     * Requests to retrieve the guild this role is associated to, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the guild
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this role is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getGuildById(getGuildId());
    }

    /**
     * Gets the <i>raw</i> mention. This is the format utilized to directly mention another role (assuming the role
     * exists in context of the mention).
     *
     * @return The <i>raw</i> mention.
     */
    public String getMention() {
        if (isEveryone()) {
            return MentionUtil.EVERYONE;
        }
        return MentionUtil.forRole(getId());
    }

    /**
     * Gets the <i>raw</i> mention for linked role. This is the format utilized to directly mention another role (assuming the role
     * exists in context of the mention and is linked).
     *
     * @see RoleTags#isGuildLinkedRole()
     * @return The <i>raw</i> mention.
     */
    public String getLinkedMention() {
        return Guild.ResourceNavigation.LINKED_ROLE.getMention(this.getId().asString());
    }

    /**
     * Gets the tags this role has, if present.
     *
     * @return The tags this role has, if present.
     */
    public Optional<RoleTags> getTags() {
        return data.tags().toOptional().map(data -> new RoleTags(gateway, data));
    }

    /**
     * Returns the flags of this {@link Role}.
     *
     * @return A {@code EnumSet} with the flags of this role.
     */
    public EnumSet<Flag> getFlags() {
        int flags = data.flags();
        if (flags != 0) {
            return Flag.of(flags);
        }
        return EnumSet.noneOf(Flag.class);
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Requests to edit this role.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyRoleEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Role}. If an error is received,
     * it is emitted through the {@code Mono}.
     * @deprecated use {@link #edit(RoleEditSpec)} or {@link #edit()} which offer an immutable approach to build specs
     */
    @Deprecated
    public Mono<Role> edit(final Consumer<? super LegacyRoleEditSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyRoleEditSpec mutatedSpec = new LegacyRoleEditSpec();
                    spec.accept(mutatedSpec);
                    return rest.edit(mutatedSpec.asRequest(), mutatedSpec.getReason())
                            .map(bean -> new Role(gateway, bean, getGuildId().asLong()));
                });
    }

    /**
     * Requests to edit this role. Properties specifying how to edit this role can be set via the {@code withXxx}
     * methods of the returned {@link RoleEditMono}.
     *
     * @return A {@link RoleEditMono} where, upon successful completion, emits the edited {@link Role}. If an error is
     * received, it is emitted through the {@code RoleEditMono}.
     */
    public RoleEditMono edit() {
        return RoleEditMono.of(this);
    }

    /**
     * Requests to edit this role.
     *
     * @param spec an immutable object that specifies how to edit this role
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Role}. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<Role> edit(RoleEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(() -> rest.edit(spec.asRequest(), spec.reason())
                .map(bean -> new Role(gateway, bean, getGuildId().asLong())));
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
        return rest.delete(reason);
    }

    /**
     * Requests to change this role's position.
     *
     * @param position The position to change for this role.
     * @param reason The reason, if present.
     * @return A {@link Flux} that continually emits all the {@link Role roles} associated to this role's
     * {@link #getGuild() guild}. If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<Role> changePosition(final int position, @Nullable final String reason) {
        return rest.changePosition(position, reason)
            .map(data -> new Role(gateway, data, getGuildId().asLong()));
    }

    /**
     * Requests to change this role's position.
     *
     * @param position The position to change for this role.
     * @return A {@link Flux} that continually emits all the {@link Role roles} associated to this role's
     * {@link #getGuild() guild}. If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<Role> changePosition(final int position) {
        return this.changePosition(position, null);
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

    /**
     * Describes the flags of a Role.
     *
     * @see
     * <a href="https://discord.com/developers/docs/topics/permissions#role-object-role-flags">Discord</a>
     */
    public enum Flag {
        /**
         * Role can be selected by members in an onboarding prompt
         */
        IN_PROMPT(0);

        /** The underlying value as represented by Discord. */
        private final int value;

        /** The flag value as represented by Discord. */
        private final int flag;

        /**
         * Constructs a {@code Role.Flag}.
         */
        Flag(final int value) {
            this.value = value;
            this.flag = 1 << value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the flag value as represented by Discord.
         *
         * @return The flag value as represented by Discord.
         */
        public int getFlag() {
            return flag;
        }

        /**
         * Gets the flags of a role. It is guaranteed that invoking {@link #getValue()} from the returned enum
         * will be equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The flags value as represented by Discord.
         * @return The {@link EnumSet} of flags.
         */
        public static EnumSet<Flag> of(final int value) {
            final EnumSet<Flag> flagSet = EnumSet.noneOf(Flag.class);
            for (Flag flag : Flag.values()) {
                long flagValue = flag.getFlag();
                if ((flagValue & value) == flagValue) {
                    flagSet.add(flag);
                }
            }
            return flagSet;
        }
    }
}
