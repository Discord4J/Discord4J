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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.VoiceState;
import discord4j.core.object.presence.Presence;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.PermissionSet;
import discord4j.rest.util.Snowflake;
import discord4j.core.spec.BanQuerySpec;
import discord4j.core.spec.GuildMemberEditSpec;
import discord4j.core.util.OrderUtil;
import discord4j.core.util.PermissionUtil;
import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.PresenceData;
import discord4j.discordjson.json.VoiceStateData;
import discord4j.store.api.util.LongLongTuple2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.math.MathFlux;
import reactor.util.annotation.Nullable;

import java.awt.Color;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A Discord guild member.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/guild#guild-member-object">Guild Member Object</a>
 */
public final class Member extends User {

    /** The raw data as represented by Discord. */
    private final MemberData data;

    /** The ID of the guild this user is associated to. */
    private final long guildId;

    /**
     * Constructs a {@code Member} with an associated ServiceMediator and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param guildId The ID of the guild this user is associated to.
     */
    public Member(final GatewayDiscordClient gateway, final MemberData data, final long guildId) {
        super(gateway, data.user());
        this.data = Objects.requireNonNull(data);
        this.guildId = guildId;
    }

    @Override
    public Mono<Member> asMember(final Snowflake guildId) {
        return Mono.just(this)
                .filter(member -> member.getGuildId().equals(guildId))
                .switchIfEmpty(super.asMember(guildId));
    }

    /**
     * Gets the user's guild roles' IDs.
     *
     * @return The user's guild roles' IDs.
     */
    public Set<Snowflake> getRoleIds() {
        return data.roles().stream()
                .map(Snowflake::of)
                .collect(Collectors.toSet());
    }

    /**
     * Requests to retrieve the user's guild roles.
     * <p>
     * The order of items emitted by the returned {@code Flux} is unspecified. Use {@link OrderUtil#orderRoles(Flux)}
     * to consistently order roles.
     *
     * @return A {@link Flux} that continually emits the user's guild {@link Role roles}. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<Role> getRoles() {
        return Flux.fromIterable(getRoleIds())
                .flatMap(id -> getClient().getRoleById(getGuildId(), id));
    }

    /**
     * Requests to retrieve the user's guild roles, using the given retrieval strategy.
     * <p>
     * The order of items emitted by the returned {@code Flux} is unspecified. Use {@link OrderUtil#orderRoles(Flux)}
     * to consistently order roles.
     *
     * @param retrievalStrategy the strategy to use to get the roles
     * @return A {@link Flux} that continually emits the user's guild {@link Role roles}. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<Role> getRoles(EntityRetrievalStrategy retrievalStrategy) {
        return Flux.fromIterable(getRoleIds())
                .flatMap(id -> getClient().withRetrievalStrategy(retrievalStrategy).getRoleById(getGuildId(), id));
    }

    /**
     * Requests to retrieve the user's highest guild role.
     * <p>
     * The highest role is defined to be the role with the highest position, based on Discord's ordering. This is the
     * role that appears at the <b>top</b> in Discord's UI.
     *
     * @return A {@link Mono} where, upon successful completion, emits the user's highest {@link Role role}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getHighestRole() {
        return MathFlux.max(Flux.fromIterable(getRoleIds()).flatMap(id -> getClient().getRoleById(getGuildId(), id)),
                            OrderUtil.ROLE_ORDER);
    }

    /**
     * Requests to retrieve the user's highest guild role, using the given retrieval strategy.
     * <p>
     * The highest role is defined to be the role with the highest position, based on Discord's ordering. This is the
     * role that appears at the <b>top</b> in Discord's UI.
     *
     * @param retrievalStrategy the strategy to use to get the highest role
     * @return A {@link Mono} where, upon successful completion, emits the user's highest {@link Role role}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getHighestRole(EntityRetrievalStrategy retrievalStrategy) {
        return MathFlux.max(Flux.fromIterable(getRoleIds())
                .flatMap(id -> getClient().withRetrievalStrategy(retrievalStrategy).getRoleById(getGuildId(), id)),
                            OrderUtil.ROLE_ORDER);
    }

    /**
     * Gets when the user joined the guild.
     *
     * @return When the user joined the guild.
     */
    public Instant getJoinTime() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(data.joinedAt(), Instant::from);
    }

    /**
     * Gets when the user started boosting the server, if present.
     *
     * @return When the user started boosting the server, if present.
     */
    public Optional<Instant> getPremiumTime() {
        return data.premiumSince()
                .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    /**
     * Gets the ID of the guild this user is associated to.
     *
     * @return The ID of the guild this user is associated to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the guild this user is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this user is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Requests to retrieve the guild this user is associated to, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the guild
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this user is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild(EntityRetrievalStrategy retrievalStrategy) {
        return getClient().withRetrievalStrategy(retrievalStrategy).getGuildById(getGuildId());
    }

    /**
     * Gets the name that is displayed in client.
     *
     * @return The name that is displayed in client.
     */
    public String getDisplayName() {
        return getNickname().orElse(getUsername());
    }

    /**
     * Gets the user's guild nickname (if one is set).
     *
     * @return The user's guild nickname (if one is set).
     */
    public Optional<String> getNickname() {
        return Possible.flatOpt(data.nick());
    }

    /**
     * Gets the <i>raw</i> nickname mention. This is the format utilized to directly mention another user (assuming the
     * user exists in context of the mention).
     *
     * @return The <i>raw</i> nickname mention.
     */
    public String getNicknameMention() {
        return "<@!" + getId().asString() + ">";
    }

    /**
     * Requests to retrieve this user's voice state for this guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits a {@link VoiceState voice state} for this user
     * for this guild. If an error is received, it is emitted through the {@code Mono}.
     *
     * @implNote If the underlying store does not save
     * {@link VoiceStateData} instances <b>OR</b> the bot is currently not logged in then the returned {@code Mono} will
     * always be empty.
     */
    public Mono<VoiceState> getVoiceState() {
        return getClient().getGatewayResources().getStateView().getVoiceStateStore()
                .find(LongLongTuple2.of(getGuildId().asLong(), getId().asLong()))
                .map(bean -> new VoiceState(getClient(), bean));
    }

    /**
     * Requests to retrieve the presence for this user for this guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits a {@link Presence presence} for this user for
     * this guild. If an error is received, it is emitted through the {@code Mono}.
     *
     * @implNote If the underlying store does not save
     * {@link PresenceData} instances <b>OR</b> the bot is currently not logged in then the returned {@code Mono} will
     * always be empty.
     */
    public Mono<Presence> getPresence() {
        return getClient().getGatewayResources().getStateView().getPresenceStore()
                .find(LongLongTuple2.of(getGuildId().asLong(), getId().asLong()))
                .map(Presence::new);
    }

    /**
     * Requests to kick this member.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the member was kicked. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> kick() {
        return kick(null);
    }

    /**
     * Requests to kick this member while optionally specifying the reason.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the member was kicked. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> kick(@Nullable final String reason) {
        return getClient().getRestClient().getGuildService()
                .removeGuildMember(getGuildId().asLong(), getId().asLong(), reason);
    }

    /**
     * Requests to ban this user.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link BanQuerySpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating this user was banned. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> ban(final Consumer<? super BanQuerySpec> spec) {
        final BanQuerySpec mutatedSpec = new BanQuerySpec();
        spec.accept(mutatedSpec);

        return getClient().getRestClient().getGuildService()
                .createGuildBan(getGuildId().asLong(), getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
    }

    /**
     * Requests to unban this user.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating this user was unbanned. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> unban() {
        return unban(null);
    }

    /**
     * Requests to unban this user while optionally specifying the reason.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating this user was unbanned. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> unban(@Nullable final String reason) {
        return getClient().getRestClient().getGuildService()
                .removeGuildBan(getGuildId().asLong(), getId().asLong(), reason);
    }

    /**
     * Requests to add a role to this member.
     *
     * @param roleId The ID of the role to add to this member.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the role was added to this
     * member. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> addRole(final Snowflake roleId) {
        return addRole(roleId, null);
    }

    /**
     * Requests to add a role to this member while optionally specifying the reason.
     *
     * @param roleId The ID of the role to add to this member.
     * @param reason The reason, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the role was added to this
     * member. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> addRole(final Snowflake roleId, @Nullable final String reason) {
        return getClient().getRestClient().getGuildService()
                .addGuildMemberRole(guildId, getId().asLong(), roleId.asLong(), reason);
    }

    /**
     * Requests to remove a role from this member.
     *
     * @param roleId The ID of the role to remove from this member.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the role was removed from
     * this member. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> removeRole(final Snowflake roleId) {
        return removeRole(roleId, null);
    }

    /**
     * Requests to remove a role from this member while optionally specifying the reason.
     *
     * @param roleId The ID of the role to remove from this member.
     * @param reason The reason, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the role was removed from
     * this member. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> removeRole(final Snowflake roleId, @Nullable final String reason) {
        return getClient().getRestClient().getGuildService()
                .removeGuildMemberRole(guildId, getId().asLong(), roleId.asLong(), reason);
    }

    /**
     * Requests to calculate the permissions granted to this member by his roles in the guild.
     *
     * @return The permissions granted to this member by his roles in the guild.
     */
    public Mono<PermissionSet> getBasePermissions() {
        Mono<Boolean> getIsOwner = getGuild().map(guild -> guild.getOwnerId().equals(getId()));
        Mono<PermissionSet> getEveryonePerms = getGuild().flatMap(Guild::getEveryoneRole).map(Role::getPermissions);
        Mono<List<PermissionSet>> getRolePerms = getRoles().map(Role::getPermissions).collectList();

        return getIsOwner.filter(Predicate.isEqual(Boolean.TRUE))
                .flatMap(ignored -> Mono.just(PermissionSet.all()))
                .switchIfEmpty(Mono.zip(getEveryonePerms, getRolePerms, PermissionUtil::computeBasePermissions));
    }

    /**
     * Requests to determine if this member is higher in the role hierarchy than the provided member or signal
     * IllegalArgumentException if the provided member is in a different guild than this member.
     * This is determined by the positions of each of the members' highest roles.
     *
     * @param otherMember The member to compare in the role hierarchy with this member.
     * @return A {@link Mono} where, upon successful completion, emits {@code true} if this member is higher in the
     * role hierarchy than the provided member, {@code false} otherwise. If an error is received, it is emitted
     * through the {@code Mono}.
     */
    public Mono<Boolean> isHigher(Member otherMember) {
        if (!getGuildId().equals(otherMember.getGuildId())) {
            return Mono.error(new IllegalArgumentException("The provided member is in a different guild."));
        }

        // A member is not considered to be higher in the role hierarchy than himself
        if (this.equals(otherMember)) {
            return Mono.just(false);
        }

        return getGuild().map(Guild::getOwnerId)
                .flatMap(ownerId -> {
                    // the owner of the guild is higher in the role hierarchy than everyone
                    if (ownerId.equals(getId())) {
                        return Mono.just(true);
                    }
                    if (ownerId.equals(otherMember.getId())) {
                        return Mono.just(false);
                    }

                    return hasHigherRoles(otherMember.getRoleIds());
                });
    }

    /**
     * Requests to determine if this member is higher in the role hierarchy than the member as represented
     * by the supplied ID or signal IllegalArgumentException if the member as represented by the supplied ID is in
     * a different guild than this member.
     * This is determined by the positions of each of the members' highest roles.
     *
     * @param id The ID of the member to compare in the role hierarchy with this member.
     * @return A {@link Mono} where, upon successful completion, emits {@code true} if this member is higher in the role
     * hierarchy than the member as represented by the supplied ID, {@code false} otherwise. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<Boolean> isHigher(Snowflake id) {
        return getClient().getMemberById(getGuildId(), id).flatMap(this::isHigher);
    }

    /**
     * Requests to determine if the position of this member's highest role is greater than the highest position of the
     * provided roles.
     * <p>
     * The behavior of this operation is undefined if a given role is from a different guild.
     *
     * @param otherRoles The set of roles to compare in the role hierarchy with this member's roles.
     * @return A {@link Mono} where, upon successful completion, emits {@code true} if the position of this member's
     * highest role is greater than the highest position of the provided roles, {@code false} otherwise.
     * If an error is received it is emitted through the {@code Mono}.
     */
    public Mono<Boolean> hasHigherRoles(Set<Snowflake> otherRoles) {
        return getGuild()
                .flatMapMany(Guild::getRoles)
                .transform(OrderUtil::orderRoles)
                .collectList()
                .map(guildRoles -> { // Get the sorted list of guild roles
                    Set<Snowflake> thisRoleIds = getRoleIds();

                    // Get the position of this member's highest role by finding the maximum element in guildRoles which
                    // the member has and then finding its index in the sorted list of guild roles (the role's actual
                    // position). The @everyone role is not included, so if we end up with empty, that is their only
                    // role which is always at position 0.
                    int thisHighestRolePos = guildRoles.stream()
                            .filter(role -> thisRoleIds.contains(role.getId()))
                            .max(OrderUtil.ROLE_ORDER)
                            .map(guildRoles::indexOf)
                            .orElse(0);

                    int otherHighestPos = guildRoles.stream()
                            .filter(role -> otherRoles.contains(role.getId()))
                            .max(OrderUtil.ROLE_ORDER)
                            .map(guildRoles::indexOf)
                            .orElse(0);

                    return thisHighestRolePos > otherHighestPos;
                });
    }

    /**
     * Requests to determine the {@link Color} this member would be visually represented in the Discord client.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@code Color} this member would be visually
     * represented in the Discord client. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Color> getColor() {
        Flux<Role> rolesWithColor = getRoles().filter(it -> !it.getColor().equals(Role.DEFAULT_COLOR));

        return MathFlux.max(rolesWithColor, OrderUtil.ROLE_ORDER)
                .map(Role::getColor)
                .defaultIfEmpty(Role.DEFAULT_COLOR);
    }

    /**
     * Requests to edit this member.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link GuildMemberEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the member has been edited.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> edit(final Consumer<? super GuildMemberEditSpec> spec) {
        final GuildMemberEditSpec mutatedSpec = new GuildMemberEditSpec();
        spec.accept(mutatedSpec);

        return getClient().getRestClient().getGuildService()
                .modifyGuildMember(getGuildId().asLong(), getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
    }

    @Override
    public String toString() {
        return "Member{" +
                "data=" + data +
                ", guildId=" + guildId +
                "} " + super.toString();
    }
}
