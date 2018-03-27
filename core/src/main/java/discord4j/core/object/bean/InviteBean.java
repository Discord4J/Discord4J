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
package discord4j.core.object.bean;

import discord4j.common.json.response.InviteResponse;
import discord4j.common.json.response.UserResponse;

import javax.annotation.Nullable;
import java.io.Serializable;

public final class InviteBean implements Serializable {

    private static final long serialVersionUID = -3682327346102202724L;

    private String code;
    private long guildId;
    private long channelId;
    @Nullable
    private Long inviterId;
    @Nullable
    private Integer uses;
    @Nullable
    private Integer maxUses;
    @Nullable
    private Integer maxAge;
    @Nullable
    private Boolean temporary;
    @Nullable
    private String createdAt;
    @Nullable
    private Boolean revoked;

    public InviteBean(final InviteResponse response) {
        code = response.getCode();
        guildId = response.getGuild().getId();
        channelId = response.getChannel().getId();
        final UserResponse inviter = response.getInviter();
        inviterId = (inviter == null) ? null : inviter.getId();
        uses = response.getUses();
        maxUses = response.getMaxUses();
        maxAge = response.getMaxAge();
        temporary = response.getTemporary();
        createdAt = response.getCreatedAt();
        revoked = response.getRevoked();
    }

    public InviteBean() {}

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(final long guildId) {
        this.guildId = guildId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(final long channelId) {
        this.channelId = channelId;
    }

    @Nullable
    public Long getInviterId() {
        return inviterId;
    }

    public void setInviterId(@Nullable final Long inviterId) {
        this.inviterId = inviterId;
    }

    @Nullable
    public Integer getUses() {
        return uses;
    }

    public void setUses(@Nullable final Integer uses) {
        this.uses = uses;
    }

    @Nullable
    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(@Nullable final Integer maxUses) {
        this.maxUses = maxUses;
    }

    @Nullable
    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(@Nullable final Integer maxAge) {
        this.maxAge = maxAge;
    }

    @Nullable
    public Boolean getTemporary() {
        return temporary;
    }

    public void setTemporary(@Nullable final Boolean temporary) {
        this.temporary = temporary;
    }

    @Nullable
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@Nullable final String createdAt) {
        this.createdAt = createdAt;
    }

    @Nullable
    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(@Nullable final Boolean revoked) {
        this.revoked = revoked;
    }
}
