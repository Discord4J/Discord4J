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

package discord4j.rest.entity;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.GuildUpdateData;
import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.RoleData;
import discord4j.rest.RestClient;
import discord4j.rest.util.OrderUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.math.MathFlux;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a user (bot or normal) that is member of a specific guild.
 */
public class RestMember {

    private final RestClient restClient;
    private final long guildId;
    private final long id;

    private RestMember(RestClient restClient, long guildId, long id) {
        this.restClient = restClient;
        this.guildId = guildId;
        this.id = id;
    }

    /**
     * Create a {@link RestMember} with the given parameters. This method does not perform any API request.
     *
     * @param restClient REST API resources
     * @param guildId the ID of the guild this member belongs to
     * @param id the ID of this member
     * @return a {@code RestMember} represented by the given parameters.
     */
    public static RestMember create(RestClient restClient, Snowflake guildId, Snowflake id) {
        return new RestMember(restClient, guildId.asLong(), id.asLong());
    }

    static RestMember create(RestClient restClient, long guildId, long id) {
        return new RestMember(restClient, guildId, id);
    }

    /**
     * Returns the ID of the guild this member belongs to.
     *
     * @return The ID of the the guild this member belongs to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Returns the ID of this member.
     *
     * @return The ID of this member
     */
    public Snowflake getId() {
        return Snowflake.of(id);
    }

    /**
     * Create a {@link RestGuild} with data from this Member. This method does not perform any API request.
     *
     * @return a {@code RestGuild} represented by the data from this Member
     */
    public RestGuild guild() {
        return RestGuild.create(restClient, guildId);
    }

    /**
     * Create a {@link RestUser} with daa from this Member. This method does not perform any API request.
     *
     * @return a {@code RestUser} represented by the data from this Member
     */
    public RestUser user() {
        return RestUser.create(restClient, id);
    }

    /**
     * Requests to retrieve the Member's {@link MemberData}
     *
     * @return A {@link Mono} where, upon successful completion, emits the Member's {@link MemberData}. If an error
     * is received, it is emitted through the Mono.
     */
    public Mono<MemberData> getData() {
        return restClient.getGuildService()
                .getGuildMember(guildId, id);
    }

    /**
     * Requests to retrieve the member's highest guild role.
     * <p>
     * The highest role is defined to be the role with the highest position, based on Discord's ordering. This is the
     * role that appears at the <b>top</b> in Discord's UI.
     *
     * @return A {@link Mono} where, upon successful completion, emits the member's highest {@link RoleData role}. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<RoleData> getHighestRole() {
        return getData().map(MemberData::roles)
                .flatMap(roles -> MathFlux.max(Flux.fromIterable(roles)
                                .map(id -> restClient.getRoleById(Snowflake.of(guildId), Snowflake.of(id)))
                                .flatMap(RestRole::getData),
                        OrderUtil.ROLE_ORDER));
    }

    /**
     * Requests to determine if this member is higher in the role hierarchy than the provided member or signal
     * {@code IllegalArgumentException} if the provided member is in a different guild than this member.
     *
     * @param otherMember The member to compare in the role hierarchy with this member.
     * @return A {@link Mono} where, upon successful completion, emits {@code true} if this member is higher in the
     * role hierarchy than the provided member, {@code false} otherwise. If an error is received, it is emitted
     * through the @{code Mono}.
     */
    public Mono<Boolean> isHigher(RestMember otherMember) {
        if (guildId != otherMember.guildId) {
            return Mono.error(new IllegalArgumentException("The provided member is in a different guild."));
        }

        // A member is not considered to be higher in the role hierarchy than themself
        if (this.equals(otherMember)) {
            return Mono.just(false);
        }

        return guild().getData().map(GuildUpdateData::ownerId)
                .flatMap(ownerId -> {
                    // The owner of the guild is higher in the role hierarchy than everyone
                    if (ownerId.asLong() == id) {
                        return Mono.just(true);
                    }

                    if (ownerId.asLong() == otherMember.id) {
                        return Mono.just(false);
                    }

                    return otherMember.getData()
                            .flatMapMany(data -> Flux.fromIterable(data.roles()))
                            .map(Snowflake::of)
                            .collectList()
                            .flatMap(this::hasHigherRoles);
                });
    }

    /**
     * Requests to determine if this member is nigher in the role hierarchy than the member as represented by the
     * supplied ID or signal {@code IllegalArgumentException} if the member as represented by the supplied ID is in a
     * different guild than this member.
     * This is determined by the positions of each of the members' highest roles.
     *
     * @param id The ID of the member to compare in the role hierarchy with this member.
     * @return A {@link Mono} where, upon successful completion, emits {@code true} if this member is higher in the
     * role hierarchy than the member as represented by the supplied ID, {@code false} otherwise. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Boolean> isHigher(Snowflake id) {
        return Mono.just(restClient.getMemberById(Snowflake.of(guildId), id)).flatMap(this::isHigher);
    }

    /**
     * Requests to determine if the position of this member's highest role is greater than the highest position of
     * the provided roles.
     * <p>
     * The behavior of this operation is undefined if a given role is from a different guild.
     *
     * @param otherRoles The collection of roles to compare in the role hierarchy with this member's roles.
     * @return A {@link Mono} where, upon successful completion, emits {@code true} if the position of this member's
     * highest role is greater than the highest position of the provided roles, {@code false} otherwise.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Boolean> hasHigherRoles(Collection<Snowflake> otherRoles) {
        return guild().getRoles()
                .transform(OrderUtil::orderRoles)
                .collectList()
                .flatMap(guildRoles -> { // Get the sorted list of guild roles
                    Mono</*~~>*/List<Snowflake>> thisRoleIds = this.getData()
                            .map(MemberData::roles)
                            .flatMapMany(Flux::fromIterable)
                            .map(Snowflake::of)
                            .collectList()
                            .cache();

                    // Get the position of this member's highest role by finding the maximum element in guildRoles which
                    // the member has and then finding its index in the sorted list of guild roles (the role's actual
                    // position). The @everyone role is not included, so if we end up with empty, that is their only
                    // role which is always at position 0.
                    Mono<Integer> thisHighestRolePos = thisRoleIds.map(thisRoles ->
                            guildRoles.stream()
                                    .filter(role -> thisRoles.contains(Snowflake.of(role.id())))
                                    .max(OrderUtil.ROLE_ORDER)
                                    .map(guildRoles::indexOf)
                                    .orElse(0));

                    int otherHighestPos = guildRoles.stream()
                            .filter(role -> otherRoles.contains(Snowflake.of(role.id())))
                            .max(OrderUtil.ROLE_ORDER)
                            .map(guildRoles::indexOf)
                            .orElse(0);

                    return thisHighestRolePos.map(thisPos -> thisPos > otherHighestPos);
                });
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RestMember that = (RestMember) o;
        return guildId == that.guildId && id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(guildId, id);
    }
}
