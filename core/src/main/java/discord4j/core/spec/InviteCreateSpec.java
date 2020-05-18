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
package discord4j.core.spec;

import discord4j.core.object.Invite;
import discord4j.discordjson.json.InviteCreateRequest;
import reactor.util.annotation.Nullable;

/**
 * Spec used to create guild channel {@link Invite} objects.
 *
 * @see
 * <a href="https://discord.com/developers/docs/resources/channel#create-channel-invite">Create Channel Invite</a>
 */
public class InviteCreateSpec implements AuditSpec<InviteCreateRequest> {

    private int maxAge;
    private int maxUses;
    private boolean temporary;
    private boolean unique;
    private String reason;

    /**
     * Sets the duration of the created {@link Invite} in seconds before expiration, or {@code 0} to never expire. If
     * unset, the default of 24 hours will be used.
     *
     * @param maxAge The duration of the invite in seconds, or {@code 0} to never expire.
     * @return This spec.
     */
    public InviteCreateSpec setMaxAge(int maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    /**
     * Sets the maximum number of uses the created {@link Invite} has before expiring, or {@code 0} for unlimited
     * uses. If unset, the default is unlimited usages.
     *
     * @param maxUses The maximum number of uses, or {@code 0} for unlimited usage.
     * @return This spec.
     */
    public InviteCreateSpec setMaxUses(int maxUses) {
        this.maxUses = maxUses;
        return this;
    }

    /**
     * Sets whether the created {@link Invite} only grants temporary membership. This property is {@code false} by
     * default.
     *
     * @param temporary {@code true} if this invite is temporary, {@code false} otherwise.
     * @return This spec.
     */
    public InviteCreateSpec setTemporary(boolean temporary) {
        this.temporary = temporary;
        return this;
    }

    /**
     * Sets whether the created {@link Invite} is unique. If {@code true}, don't try to reuse a similar invite
     * (useful for creating many unique one time use invites).
     *
     * @param unique {@code true} if the created invite is unique, {@code false} otherwise.
     * @return This spec.
     */
    public InviteCreateSpec setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    @Override
    public InviteCreateSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public InviteCreateRequest asRequest() {
        return InviteCreateRequest.builder()
                .maxAge(maxAge)
                .maxUses(maxUses)
                .temporary(temporary)
                .unique(unique)
                .build();
    }
}
