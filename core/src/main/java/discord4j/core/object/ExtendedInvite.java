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

import discord4j.core.ServiceMediator;
import discord4j.core.object.bean.ExtendedInviteBean;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
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
     * Constructs a {@code ExtendedInvite} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ExtendedInvite(final ServiceMediator serviceMediator, final ExtendedInviteBean data) {
        super(serviceMediator, data);
    }

    /**
     * Gets the ID of the user who created the invite.
     *
     * @return The ID of the user who created the invite.
     */
    public Snowflake getInviterId() {
        return Snowflake.of(getData().getInviterId());
    }

    /**
     * Requests to retrieve the user who created the invite.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User user} who created the invite. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getInviter() {
        return getClient().getUserById(getInviterId());
    }

    /**
     * Gets the number of times this invite has been used.
     *
     * @return The number of times this invite has been used.
     */
    public int getUses() {
        return getData().getUses();
    }

    /**
     * Gets the max number of times this invite can be used.
     *
     * @return The max number of times this invite can be used.
     */
    public int getMaxUses() {
        return getData().getMaxUses();
    }

    /**
     * Gets the instant this invite expires, if possible.
     *
     * @return The instant this invite expires, if possible.
     */
    public Optional<Instant> getExpiration() {
        final boolean temporary = getData().isTemporary();
        final int maxAge = getData().getMaxAge();

        return temporary ? Optional.of(getCreation().plus(maxAge, ChronoUnit.SECONDS)) : Optional.empty();
    }

    /**
     * Gets when this invite was created.
     *
     * @return When this invite was created.
     */
    public Instant getCreation() {
        return Instant.parse(getData().getCreatedAt());
    }

    /**
     * Gets whether this invite is revoked.
     *
     * @return {@code true} if this invite was revoked, {@code false} otherwise.
     */
    public boolean isRevoked() {
        return getData().isRevoked();
    }

    @Override
    protected ExtendedInviteBean getData() {
        return (ExtendedInviteBean) super.getData();
    }
}
