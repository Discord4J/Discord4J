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
package discord4j.core.spec.legacy;

import discord4j.common.util.Snowflake;
import discord4j.core.object.Invite;
import discord4j.discordjson.json.ImmutableInviteCreateRequest;
import discord4j.discordjson.json.InviteCreateRequest;
import org.jspecify.annotations.Nullable;

/**
 * LegacySpec used to create guild channel {@link Invite} objects.
 *
 * @see
 * <a href="https://discord.com/developers/docs/resources/channel#create-channel-invite">Create Channel Invite</a>
 */
public class LegacyInviteCreateSpec implements LegacyAuditSpec<InviteCreateRequest> {

    private final ImmutableInviteCreateRequest.Builder requestBuilder = InviteCreateRequest.builder();
    @Nullable
    private String reason;

    /**
     * Sets the duration of the created {@link Invite} in seconds before expiration, or {@code 0} to never expire. If
     * unset, the default of 24 hours will be used.
     *
     * @param maxAge The duration of the invite in seconds, or {@code 0} to never expire.
     * @return This spec.
     */
    public LegacyInviteCreateSpec setMaxAge(int maxAge) {
        requestBuilder.maxAge(maxAge);
        return this;
    }

    /**
     * Sets the maximum number of uses the created {@link Invite} has before expiring, or {@code 0} for unlimited
     * uses. If unset, the default is unlimited usages.
     *
     * @param maxUses The maximum number of uses, or {@code 0} for unlimited usage.
     * @return This spec.
     */
    public LegacyInviteCreateSpec setMaxUses(int maxUses) {
        requestBuilder.maxUses(maxUses);
        return this;
    }

    /**
     * Sets whether the created {@link Invite} only grants temporary membership. This property is {@code false} by
     * default.
     *
     * @param temporary {@code true} if this invite is temporary, {@code false} otherwise.
     * @return This spec.
     */
    public LegacyInviteCreateSpec setTemporary(boolean temporary) {
        requestBuilder.temporary(temporary);
        return this;
    }

    /**
     * Sets whether the created {@link Invite} is unique. If {@code true}, don't try to reuse a similar invite
     * (useful for creating many unique one time use invites).
     *
     * @param unique {@code true} if the created invite is unique, {@code false} otherwise.
     * @return This spec.
     */
    public LegacyInviteCreateSpec setUnique(boolean unique) {
        requestBuilder.unique(unique);
        return this;
    }

    /**
     * Sets the type of target for this voice channel invite.
     *
     * @param targetType The type of target for this voice channel invite.
     * @return This spec.
     */
    public LegacyInviteCreateSpec setTargetType(Invite.Type targetType) {
        requestBuilder.targetType(targetType.getValue());
        return this;
    }

    /**
     * Sets the id of the user whose stream to display for this invite, required if `target_type` is 1,
     * the user must be streaming in the channel.
     *
     * @param targetUserId The id of the user whose stream to display for this invite.
     * @return This spec.
     */
    public LegacyInviteCreateSpec setTargetUserId(Snowflake targetUserId) {
        requestBuilder.targetUserId(targetUserId.asString());
        return this;
    }

    /**
     * Sets the id of the embedded application to open for this invite, required if `target_type` is 2, the
     * application must have the `EMBEDDED` flag.
     *
     * @param targetApplicationId The id of the embedded application to open for this invite.
     * @return This spec.
     */
    public LegacyInviteCreateSpec setTargetApplicationId(Snowflake targetApplicationId) {
        requestBuilder.targetApplicationId(targetApplicationId.asString());
        return this;
    }

    @Override
    public LegacyInviteCreateSpec setReason(@Nullable final String reason) {
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
        return requestBuilder.build();
    }
}
