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

package discord4j.core.object.command;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.core.object.entity.Member;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.discordjson.json.ResolvedMemberData;
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Discord member that was resolved in a command.
 *
 * @see
 * <a href="https://discord.com/developers/docs/interactions/slash-commands#interaction-object-application-command-interaction-data-resolved-structure">
 * Application Command Interaction Data Resolved Object
 * </a>
 */
@Experimental
public class ResolvedMember implements DiscordObject {

    /**
     * The gateway associated to this object.
     */
    private final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final ResolvedMemberData data;
    private final UserData user;
    private final long guildId;

    /**
     * Constructs a {@code ResolvedMember} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data    The raw data as represented by Discord, must be non-null.
     * @param user    The raw user associated to the member, must be non-null.
     * @param guildId the ID of the guild the user is member of
     */
    public ResolvedMember(final GatewayDiscordClient gateway, final ResolvedMemberData data, final UserData user,
                          long guildId) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
        this.user = Objects.requireNonNull(user);
        this.guildId = guildId;
    }

    /**
     * Returns the raw data as represented by Discord.
     *
     * @return the raw data
     */
    public ResolvedMemberData getData() {
        return data;
    }

    /**
     * Gets the ID of this member.
     *
     * @return The ID of this member;
     */
    public Snowflake getId() {
        return Snowflake.of(user.id());
    }

    /**
     * Gets the avatar hash of this member, if provided.
     *
     * @return The avatar hash, if present.
     */
    public Optional<String> getAvatar() {
        return Possible.flatOpt(data.avatar());
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
        return Possible.flatOpt(data.premiumSince())
                .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    /**
     * Gets the name that is displayed in client.
     *
     * @return The name that is displayed in client.
     */
    public String getDisplayName() {
        return getNickname().orElse(user.username());
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
     * Gets the total permissions of the member in the channel, including overwrites.
     *
     * @return The permissions of the member.
     */
    public PermissionSet getEffectivePermissions() {
        return PermissionSet.of(data.permissions().toOptional().orElseThrow(IllegalStateException::new));
    }

    /**
     * Retrieves the full {@link Member} instance corresponding to this resolved member.
     *
     * @return a {@link Mono} where, upon successful completion, emits the full {@link Member} instance corresponding to
     * this resolved member. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> asFullMember() {
        return gateway.getMemberById(getGuildId(), getId());
    }

    /**
     * Retrieves the full {@link Member} instance corresponding to this resolved member, using the given retrieval
     * strategy.
     *
     * @return a {@link Mono} where, upon successful completion, emits the full {@link Member} instance corresponding to
     * this resolved member. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> asFullMember(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getMemberById(getGuildId(), getId());
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public String toString() {
        return "ResolvedMember{" +
                "data=" + data +
                ", user=" + user +
                ", guildId=" + guildId +
                '}';
    }
}
