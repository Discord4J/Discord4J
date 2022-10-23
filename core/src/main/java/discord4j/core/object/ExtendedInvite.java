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

import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.InviteData;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

// TODO FIXME: invites are generally just kind of a mess rn

/**
 * Metadata for a Discord invite.
 *
 * @see <a href="https://discord.com/developers/docs/resources/invite#invite-metadata-object">Metadata Object</a>
 */
public final class ExtendedInvite extends Invite {

    /**
     * Constructs a {@code ExtendedInvite} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ExtendedInvite(final GatewayDiscordClient gateway, final InviteData data) {
        super(gateway, data);
    }

    /**
     * Gets the number of times this invite has been used.
     *
     * @return The number of times this invite has been used.
     */
    public int getUses() {
        return getData().uses().toOptional()
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the max number of times this invite can be used.
     *
     * @return The max number of times this invite can be used.
     */
    public int getMaxUses() {
        return getData().maxUses().toOptional()
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the instant this invite expires, if possible.
     *
     * @return The instant this invite expires, if empty, invite is never expiring.
     */
    public Optional<Instant> getExpiration() {
        final int maxAge = getData().maxAge().toOptional().orElseThrow(IllegalStateException::new);
        return maxAge > 0 ? Optional.of(getCreation().plus(maxAge, ChronoUnit.SECONDS)): Optional.empty();
    }

    /**
     * Gets whether this invite only grants temporary membership.
     *
     * @return {@code true} if this invite only grants temporary membership
     */
    public boolean isTemporary() {
        return getData().temporary().toOptional()
            .orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets when this invite was created.
     *
     * @return When this invite was created.
     */
    public Instant getCreation() {
        String createdAt = getData().createdAt().toOptional().orElseThrow(IllegalStateException::new);
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(createdAt, Instant::from);
    }

    @Override
    public String toString() {
        return "ExtendedInvite{} " + super.toString();
    }
}
