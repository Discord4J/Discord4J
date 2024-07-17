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

import discord4j.common.store.action.read.ReadActions;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.VoiceState;
import discord4j.core.object.presence.Presence;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.BanQuerySpec;
import discord4j.core.spec.GuildMemberEditMono;
import discord4j.core.spec.GuildMemberEditSpec;
import discord4j.core.spec.MemberBanQueryMono;
import discord4j.core.spec.legacy.LegacyBanQuerySpec;
import discord4j.core.spec.legacy.LegacyGuildMemberEditSpec;
import discord4j.core.util.ImageUtil;
import discord4j.core.util.OrderUtil;
import discord4j.core.util.PermissionUtil;
import discord4j.discordjson.json.PartialMemberData;
import discord4j.discordjson.json.UpdateUserVoiceStateRequest;
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.json.gateway.ImmutableRequestGuildMembers;
import discord4j.discordjson.json.gateway.RequestGuildMembers;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Color;
import discord4j.rest.util.Image;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.math.MathFlux;
import reactor.util.annotation.Nullable;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static discord4j.rest.util.Image.Format.GIF;
import static discord4j.rest.util.Image.Format.PNG;

/** A partial Discord guild member. */
public class PartialMember extends User {

    /** The path for member avatar image URLs. */
    private static final String AVATAR_IMAGE_PATH = "guilds/%s/users/%s/avatars/%s";

    private final PartialMemberData data;

    private final long guildId;

    /**
     * Constructs a {@code PartialMember} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param memberData The raw member data as represented by Discord, must be non-null.
     * @param guildId The ID of the guild this user is associated to.
     */
    public PartialMember(GatewayDiscordClient gateway, UserData userData, PartialMemberData memberData, long guildId) {
        super(gateway, userData);
        this.data = Objects.requireNonNull(memberData);
        this.guildId = guildId;
    }

    /**
     * Gets the data of the member.
     *
     * @return The data of the member.
     */
    public PartialMemberData getMemberData() {
        return data;
    }

    /**
     * Requests to retrieve the full {@link Member} instance corresponding to this partial member.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member member} this user is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> asFullMember() {
        return asMember(getGuildId());
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
                .flatMap(id -> getClient().withRetrievalStrategy(retrievalStrategy).getRoleById(getGuildId(),
                    id)),
            OrderUtil.ROLE_ORDER);
    }

    /**
     * Gets when the user joined the guild, if present. Can be absent if it's a lurking stage channel member.
     *
     * @return When the user joined the guild, if present.
     */
    public Optional<Instant> getJoinTime() {
        return data.joinedAt().map(it -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(it, Instant::from));
    }

    /**
     * Gets when the user started boosting the server, if present.
     *
     * @return When the user started boosting the server, if present.
     */
    public Optional<Instant> getPremiumTime() {
        return Possible.flatOpt(data.premiumSince())
            .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    /**
     * Gets when the user ends their timeout, if present.
     *
     * @return When the user ends their timeout in the server, if present.
     */
    public Optional<Instant> getCommunicationDisabledUntil() {
        return Possible.flatOpt(data.communicationDisabledUntil())
            .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    /**
     * Gets the user avatar decoration, if present.
     *
     * @return The user avatar decoration, if present.
     */
    @Override
    public Optional<AvatarDecoration> getAvatarDecoration() {
        return Possible.flatOpt(data.avatarDecoration())
            .map(data -> new AvatarDecoration(this.getClient(), data));
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
        return getNickname().orElseGet(() -> getGlobalName().orElse(getUsername()));
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
     * @deprecated This type of ping has been deprecated in the Discord API.
     */
    @Deprecated
    public String getNicknameMention() {
        return "<@!" + getId().asString() + ">";
    }

    /**
     * Gets if the member's guild avatar is animated.
     *
     * @return {@code true} if the user's avatar is animated, {@code false} otherwise.
     */
    public final boolean hasAnimatedGuildAvatar() {
        final String avatar = data.avatar().orElse(null);
        return (avatar != null) && avatar.startsWith("a_");
    }

    /**
     * Gets the member's guild avatar URL, if present.
     *
     * @param format the format for the URL.
     * @return The member's guild avatar URL, if present.
     */
    public final Optional<String> getGuildAvatarUrl(final Image.Format format) {
        return data.avatar().map(avatar -> ImageUtil.getUrl(
            String.format(AVATAR_IMAGE_PATH, guildId, getId().asString(), avatar), format));
    }

    /**
     * Gets the member's effective avatar URL.
     * If the member does not have a guild avatar, this defaults to the user's global avatar.
     *
     * @return The member's effective avatar URL.
     */
    public final String getEffectiveAvatarUrl() {
        final boolean animated = hasAnimatedGuildAvatar();
        return getGuildAvatarUrl(animated ? GIF : PNG).orElse(getAvatarUrl());
    }

    /**
     * Gets the member's guild avatar. This is the avatar at the url given by {@link #getGuildAvatarUrl(Image.Format)}.
     * </p>
     * If the member does not have a guild avatar, this method emits {@code Mono.empty()}.
     *
     * @param format The format for the avatar.
     * @return a {@link Mono} where, upon successful completion, emotes the {@link Image guild avatar} of the member.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Image> getGuildAvatar(final Image.Format format) {
        return Mono.justOrEmpty(getGuildAvatarUrl(format)).flatMap(Image::ofUrl);
    }

    /**
     * Gets the member's effective avatar. This is the avatar at the url given by {@link #getEffectiveAvatarUrl()}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Image avatar} of the user.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Image> getEffectiveAvatar() {
        return Image.ofUrl(getEffectiveAvatarUrl());
    }

    /**
     * Requests to retrieve this user's voice state for this guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits a {@link VoiceState voice state} for this user
     * for this guild. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceState> getVoiceState() {
        return Mono.from(getClient().getGatewayResources().getStore()
                .execute(ReadActions.getVoiceStateById(getGuildId().asLong(), getId().asLong())))
            .map(bean -> new VoiceState(getClient(), bean));
    }

    /**
     * Requests to retrieve the presence for this user for this guild.
     * {@code Intent.GUILD_PRESENCES} is required to get the presence of the bot, otherwise the emitted {@code Mono}
     * will always be empty.
     *
     * @return A {@link Mono} where, upon successful completion, emits a {@link Presence presence} for this user for
     * this guild. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Presence> getPresence() {
        // Fix https://github.com/Discord4J/Discord4J/issues/475
        if (getClient().getSelfId().equals(getId())) {
            return Mono.defer(() -> {
                final ImmutableRequestGuildMembers request = RequestGuildMembers.builder()
                    .guildId(getGuildId().asString())
                    .addUserId(getId().asString())
                    .presences(true)
                    .limit(1)
                    .build();

                return getClient().requestMemberChunks(request)
                    .singleOrEmpty()
                    .flatMap(chunk -> Mono.justOrEmpty(chunk.presences().toOptional())
                        .flatMapIterable(list -> list)
                        .next()
                        .map(Presence::new))
                    // IllegalArgumentException can be thrown during request validation if intents are not matching
                        // the request
                    .onErrorResume(IllegalArgumentException.class, err -> Mono.empty());
            });
        }

        return Mono.from(getClient().getGatewayResources().getStore()
                .execute(ReadActions.getPresenceById(getGuildId().asLong(), getId().asLong())))
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
     * Returns the flags of this {@link PartialMember}.
     *
     * @return A {@code EnumSet} with the flags of this member.
     */
    public EnumSet<Flag> getFlags() {
        long memberFlags = data.flags();
        if (memberFlags != 0) {
            return Flag.of(memberFlags);
        }
        return EnumSet.noneOf(Flag.class);
    }

    /**
     * Requests to ban this user.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyBanQuerySpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating this user was banned. If an
     * error is received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #ban(BanQuerySpec)} or {@link #ban()} which offer an immutable approach to build specs
     */
    @Deprecated
    public Mono<Void> ban(final Consumer<? super LegacyBanQuerySpec> spec) {
        return Mono.defer(
            () -> {
                LegacyBanQuerySpec mutatedSpec = new LegacyBanQuerySpec();
                spec.accept(mutatedSpec);
                return getClient().getRestClient().getGuildService()
                    .createGuildBan(getGuildId().asLong(), getId().asLong(), mutatedSpec.asRequest(),
                        mutatedSpec.getReason());
            });
    }

    /**
     * Requests to ban this user. Properties specifying how to ban this user can be set via the {@code withXxx} methods
     * of the returned {@link MemberBanQueryMono}.
     *
     * @return A {@link MemberBanQueryMono} where, upon successful completion, emits nothing; indicating the specified
     * user was banned. If an error is received, it is emitted through the {@code MemberBanQueryMono}.
     */
    public MemberBanQueryMono ban() {
        return MemberBanQueryMono.of(this);
    }

    /**
     * Requests to ban this user.
     *
     * @param spec an immutable object that specifies how to ban this user
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating this user was banned. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> ban(BanQuerySpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
            () -> getClient().getRestClient().getGuildService()
                .createGuildBan(getGuildId().asLong(), getId().asLong(), spec.asRequest(), spec.reason()));
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
     * Requests to edit this member.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyGuildMemberEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the modified {@link Member}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #edit(GuildMemberEditSpec)} or {@link #edit()} which offer an immutable approach to build
     * specs
     */
    @Deprecated
    public Mono<Member> edit(final Consumer<? super LegacyGuildMemberEditSpec> spec) {
        return Mono.defer(
            () -> {
                LegacyGuildMemberEditSpec mutatedSpec = new LegacyGuildMemberEditSpec();
                spec.accept(mutatedSpec);
                return getClient().getRestClient().getGuildService()
                    .modifyGuildMember(getGuildId().asLong(), getId().asLong(), mutatedSpec.asRequest(),
                        mutatedSpec.getReason())
                    .map(data -> new Member(getClient(), data, getGuildId().asLong()));
            });
    }

    /**
     * Requests to edit this member. Properties specifying how to edit this member can be set via the {@code withXxx}
     * methods of the returned {@link GuildMemberEditMono}.
     *
     * @return A {@link GuildMemberEditMono} where, upon successful completion, emits the modified {@link Member}. If an
     * error is received, it is emitted through the {@code GuildMemberEditMono}.
     */
    public GuildMemberEditMono edit() {
        return GuildMemberEditMono.of(this);
    }

    /**
     * Requests to edit this member.
     *
     * @param spec an immutable object that specifies how to edit this member
     * @return A {@link Mono} where, upon successful completion, emits the modified {@link Member}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> edit(GuildMemberEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
            () -> getClient().getRestClient().getGuildService()
                .modifyGuildMember(getGuildId().asLong(), getId().asLong(), spec.asRequest(), spec.reason())
                .map(data -> new Member(getClient(), data, getGuildId().asLong())));
    }

    /**
     * Requests to invite this member to the stage speakers. Require this user to be connected to
     * a stage channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the member
     *         has been invited to the speakers. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> inviteToStageSpeakers() {
        return getVoiceState().flatMap(voiceState ->
                Mono.defer(() -> getClient().getRestClient().getGuildService()
                        .modifyOthersVoiceState(getGuildId().asLong(), getUserData().id().asLong(),
                                UpdateUserVoiceStateRequest.builder()
                                        .channelId(voiceState.getChannelId()
                                                .orElseThrow(IllegalStateException::new)
                                                .asString())
                                        .suppress(false)
                                        .build())));
    }

    /**
     * Requests to move this member to the stage audience. Require this user to be connected to
     * a stage channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the member
     *         has been moved to the audience. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> moveToStageAudience() {
        return getVoiceState().flatMap(voiceState ->
                Mono.defer(() -> getClient().getRestClient().getGuildService()
                        .modifyOthersVoiceState(getGuildId().asLong(), getUserData().id().asLong(),
                                UpdateUserVoiceStateRequest.builder()
                                        .channelId(voiceState.getChannelId()
                                                .orElseThrow(IllegalStateException::new)
                                                .asString())
                                        .suppress(true)
                                        .build())));
    }

    @Override
    public boolean equals(@Nullable final Object object) {
        if (object instanceof PartialMember) {
            PartialMember other = (PartialMember) object;
            return guildId == other.guildId && getId().equals(other.getId());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "PartialMember{" +
            "data=" + data +
            ", guildId=" + guildId +
            "} " + super.toString();
    }

    /**
     * Describes the flags of a member in a guild.
     *
     * @see
     * <a href="https://discord.com/developers/docs/resources/guild#guild-member-object-guild-member-flags">Discord Docs - Guild Member Flags</a>
     **/
    public enum Flag {
        /**
         * Member has left and rejoined the guild
         */
        DID_REJOIN(0),
        /**
         * Member has completed onboarding
         */
        COMPLETED_ONBOARDING(1),
        /**
         * Member has completed onboarding
         * <br>
         * <b>Note:</b> this flag allows a member who does not meet verification requirements to participate in a
         * server.
         */
        BYPASSES_VERIFICATION(2),
        /**
         * Member has started onboarding
         */
        STARTED_ONBOARDING(3);

        /** The underlying value as represented by Discord. */
        private final int value;

        /** The flag value as represented by Discord. */
        private final int flag;

        /**
         * Constructs a {@code PartialMember.Flag}.
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
         * Gets the flags of member. It is guaranteed that invoking {@link #getValue()} from the returned enum will be
         * equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The flags value as represented by Discord.
         * @return The {@link EnumSet} of flags.
         */
        public static EnumSet<PartialMember.Flag> of(final long value) {
            final EnumSet<PartialMember.Flag> memberFlags = EnumSet.noneOf(PartialMember.Flag.class);
            for (PartialMember.Flag flag : PartialMember.Flag.values()) {
                long flagValue = flag.getFlag();
                if ((flagValue & value) == flagValue) {
                    memberFlags.add(flag);
                }
            }
            return memberFlags;
        }
    }
}
