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
package discord4j.core.object;

import discord4j.common.json.response.InviteResponse;
import discord4j.core.Client;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Metadata for a Discord invite.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/invite#invite-metadata-object">Metadata Object</a>
 */
public final class ExtendedInvite extends Invite {

    /**
     * Constructs a {@code ExtendedInvite} with an associated client and Discord data.
     *
     * @param client The Client associated to this object, must be non-null.
     * @param invite The raw data as represented by Discord, must be non-null.
     */
    public ExtendedInvite(final Client client, final InviteResponse invite) {
        super(client, invite);
    }

    /**
     * Gets the ID of the user who created the invite.
     *
     * @return The ID of the user who created the invite.
     */
    public Snowflake getInviterId() {
        return Optional.ofNullable(getInvite().getInviter())
                .map(inviter -> Snowflake.of(inviter.getId()))
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * Requests to retrieve the user who created the invite.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User user} who created the invite. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getInviter() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets the number of times this invite has been used.
     *
     * @return The number of times this invite has been used.
     */
    public int getUses() {
        return Optional.ofNullable(getInvite().getUses()).orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the max number of times this invite can be used.
     *
     * @return The max number of times this invite can be used.
     */
    public int getMaxUses() {
        return Optional.ofNullable(getInvite().getMaxUses()).orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the instant this invite expires, if possible.
     *
     * @return The instant this invite expires, if possible.
     */
    public Optional<Instant> getExpiration() {
        final boolean temporary = Optional.ofNullable(getInvite().getTemporary())
                .orElseThrow(IllegalStateException::new);
        final int maxAge = Optional.ofNullable(getInvite().getMaxAge()).orElseThrow(IllegalStateException::new);

        return temporary ? Optional.of(getCreation().plus(maxAge, ChronoUnit.SECONDS)) : Optional.empty();
    }

    /**
     * Gets when this invite was created.
     *
     * @return When this invite was created.
     */
    public Instant getCreation() {
        return Optional.ofNullable(getInvite().getCreatedAt())
                .map(Instant::parse)
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets whether this invite is revoked.
     *
     * @return {@code true} if this invite was revoked, {@code false} otherwise.
     */
    public boolean isRevoked() {
        return Optional.ofNullable(getInvite().getRevoked()).orElseThrow(IllegalStateException::new);
    }
}
