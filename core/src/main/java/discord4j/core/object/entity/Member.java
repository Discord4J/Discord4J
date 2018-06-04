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

import discord4j.core.ClientBuilder;
import discord4j.core.ServiceMediator;
import discord4j.core.object.VoiceState;
import discord4j.core.object.data.stored.MemberBean;
import discord4j.core.object.data.stored.PresenceBean;
import discord4j.core.object.data.stored.UserBean;
import discord4j.core.object.data.stored.VoiceStateBean;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.BanQuerySpec;
import discord4j.store.util.LongLongTuple2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A Discord guild member.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/guild#guild-member-object">Guild Member Object</a>
 */
public final class Member extends User {

    /** The raw data as represented by Discord. */
    private final MemberBean data;

    /** The ID of the guild this user is associated to. */
    private final long guildId;

    /**
     * Constructs a {@code Member} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param userData The user data as represented by Discord, must be non-null.
     * @param guildId The ID of the guild this user is associated to.
     */
    public Member(final ServiceMediator serviceMediator, final MemberBean data, final UserBean userData,
                  final long guildId) {
        super(serviceMediator, userData);
        this.data = Objects.requireNonNull(data);
        this.guildId = guildId;
    }

    @Override
    public Mono<Member> asMember(final Snowflake guildId) {
        return Mono.just(this);
    }

    /**
     * Gets the user's guild roles' IDs.
     *
     * @return The user's guild roles' IDs.
     */
    public Set<Snowflake> getRoleIds() {
        return Arrays.stream(data.getRoles())
                .mapToObj(Snowflake::of)
                .collect(Collectors.toSet());
    }

    /**
     * Requests to retrieve the user's guild roles.
     *
     * @return A {@link Flux} that continually emits the user's guild {@link Role roles}. If an error is received, it is
     * emitted through the {@code Flux}.
     */
    public Flux<Role> getRoles() {
        return Flux.fromIterable(getRoleIds()).flatMap(id -> getClient().getRoleById(getGuildId(), id));
    }

    /**
     * Gets when the user joined the guild.
     *
     * @return When the user joined the guild.
     */
    public Instant getJoinTime() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(data.getJoinedAt(), Instant::from);
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
        return Optional.ofNullable(data.getNick());
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
     * @implNote If the underlying {@link ClientBuilder#getStoreService() store} does not save {@link VoiceStateBean}
     * instances <b>OR</b> the bot is currently not logged in then the returned {@code Mono} will always be empty.
     */
    public Mono<VoiceState> getVoiceState() {
        return getServiceMediator().getStateHolder().getVoiceStateStore()
                .find(LongLongTuple2.of(getGuildId().asLong(), getId().asLong()))
                .map(bean -> new VoiceState(getServiceMediator(), bean));
    }

    /**
     * Requests to retrieve the presence for this user for this guild.
     *
     * @return A {@link Mono} where, upon successful completion, emits a {@link Presence presence} for this user for
     * this guild. If an error is received, it is emitted through the {@code Mono}.
     *
     * @implNote If the underlying {@link ClientBuilder#getStoreService() store} does not save {@link PresenceBean}
     * instances <b>OR</b> the bot is currently not logged in then the returned {@code Mono} will always be empty.
     */
    public Mono<Presence> getPresence() {
        return getServiceMediator().getStateHolder().getPresenceStore()
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
        return getServiceMediator().getRestClient().getGuildService()
                .removeGuildMember(getGuildId().asLong(), getId().asLong());
    }

    /**
     * Requests to ban this user.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link BanQuerySpec} to be operated on. If some properties
     * need to be retrieved via blocking operations (such as retrieval from a database), then it is recommended to build
     * the spec externally and call {@link #ban(BanQuerySpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating this user was banned. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> ban(final Consumer<BanQuerySpec> spec) {
        final BanQuerySpec mutatedSpec = new BanQuerySpec();
        spec.accept(mutatedSpec);
        return ban(mutatedSpec);
    }

    /**
     * Requests to ban this user.
     *
     * @param spec A configured {@link BanQuerySpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating this user was banned. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> ban(final BanQuerySpec spec) {
        return getServiceMediator().getRestClient().getGuildService()
                .createGuildBan(getGuildId().asLong(), getId().asLong(), spec.asRequest());
    }

    /**
     * Requests to unban this user.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating this user was unbanned. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> unban() {
        return getServiceMediator().getRestClient().getGuildService()
                .removeGuildBan(getGuildId().asLong(), getId().asLong());
    }

    /**
     * Requests to add a role to this member.
     *
     * @param roleId The ID of the role to add to this member.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the role was added to this
     * member. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> addRole(final Snowflake roleId) {
        return getServiceMediator().getRestClient().getGuildService()
                .addGuildMemberRole(guildId, getId().asLong(), roleId.asLong());
    }

    /**
     * Requests to remove a role from this member.
     *
     * @param roleId The ID of the role to remove from this member.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the role was removed from
     * this member. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> removeRole(final Snowflake roleId) {
        return getServiceMediator().getRestClient().getGuildService()
                .removeGuildMemberRole(guildId, getId().asLong(), roleId.asLong());
    }
}
