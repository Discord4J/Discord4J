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
package discord4j.core.object;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.discordjson.json.ApplicationTeamMemberData;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * A Discord application team member.
 *
 * @see <a href="https://discord.com/developers/docs/topics/teams#data-models-team-member-object">Team Member Object</a>
 */
public class ApplicationTeamMember implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ApplicationTeamMemberData data;

    /**
     * Constructs an {@code ApplicationTeamMember} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ApplicationTeamMember(final GatewayDiscordClient gateway, final ApplicationTeamMemberData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the data of the application team member.
     *
     * @return The data of the application team member.
     */
    public ApplicationTeamMemberData getData() {
        return data;
    }

    /**
     * Gets the user's membership state on the team.
     *
     * @return The user's membership state on the team.
     */
    public MembershipState getMembershipState() {
        return MembershipState.of(data.membershipState());
    }

    /**
     * Gets the id of the parent team of which they are a member.
     *
     * @return The id of the parent team of which they are a member.
     */
    public Snowflake getTeamId() {
        return Snowflake.of(data.teamId().asLong());
    }

    /**
     * Gets the id of the user associated with this team member.
     *
     * @return The id of the user associated with this team member.
     */
    public Snowflake getUserId() {
        return Snowflake.of(data.user().id().asLong());
    }

    /**
     * Requests to retrieve the user this member is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} this member is associated to.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser() {
        return gateway.getUserById(getUserId());
    }

    /**
     * Requests to retrieve the user this member is associated to, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the user
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} this member is associated to.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getUserById(getUserId());
    }

    /**
     * Gets the user's role on the team.
     *
     * @return The user's role on the team.
     */
    public TeamMemberRole getRole() {
        return data.role().toOptional().map(TeamMemberRole::of).orElse(TeamMemberRole.ADMIN);
    }

    /**
     * Represents the roles of a {@link ApplicationTeamMember}.
     *
     * @see <a href="https://discord.com/developers/docs/topics/teams#team-member-roles-team-member-role-types">
     *     Team Member Role Types</a>
     */
    public enum TeamMemberRole {
        UNKNOWN("unknown"),
        ADMIN("admin"),
        DEVELOPER("developer"),
        READ_ONLY("read_only"),
        ;

        /** The underlying value as represented by Discord. */
        private final String value;

        /**
         * Constructs an {@code ApplicationTeamMember.TeamMemberRole}.
         *
         * @param value The underlying value as represented by Discord.
         */
        TeamMemberRole(String value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public String getValue() {
            return value;
        }

        /**
         * Gets the role of team member. It is guaranteed that invoking {@link #getValue()} from the
         * returned enum will equal ({@link #equals(Object)}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The role of team member.
         */
        public static ApplicationTeamMember.TeamMemberRole of(final String value) {
            switch (value) {
                case "admin": return ADMIN;
                case "developer": return DEVELOPER;
                case "read_only": return READ_ONLY;
                default: return UNKNOWN;
            }
        }
    }

    /**
     * Represents the various types of membership state.
     *
     * @see <a href="https://discord.com/developers/docs/topics/teams#data-models-membership-state-enum">
     *     Membership State Enum</a>
     */
    public enum MembershipState {

        UNKNOWN(-1),

        INVITED(1),

        ACCEPTED(2);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs an {@code ApplicationTeamMember.MembershipState}.
         *
         * @param value The underlying value as represented by Discord.
         */
        MembershipState(final int value) {
            this.value = value;
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
         * Gets the membership state of team member. It is guaranteed that invoking {@link #getValue()} from the
         * returned enum will equal ({@link #equals(Object)}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The membership state of team member.
         */
        public static ApplicationTeamMember.MembershipState of(final int value) {
            switch (value) {
                case 1: return INVITED;
                case 2: return ACCEPTED;
                default: return UNKNOWN;
            }
        }
    }

    @Override
    public String toString() {
        return "ApplicationTeamMember{" +
                "data=" + data +
                '}';
    }
}
