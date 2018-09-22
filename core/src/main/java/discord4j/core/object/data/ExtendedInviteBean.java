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
package discord4j.core.object.data;

import discord4j.rest.json.response.InviteResponse;

import java.util.Objects;

public final class ExtendedInviteBean extends InviteBean {

    private static final long serialVersionUID = -3024688485065214682L;

    private long inviterId;
    private int uses;
    private int maxUses;
    private int maxAge;
    private boolean temporary;
    private String createdAt;
    private boolean revoked;

    public ExtendedInviteBean(final InviteResponse response) {
        super(response);
        inviterId = Objects.requireNonNull(response.getInviter()).getId();
        uses = Objects.requireNonNull(response.getUses());
        maxUses = Objects.requireNonNull(response.getMaxUses());
        maxAge = Objects.requireNonNull(response.getMaxAge());
        temporary = Objects.requireNonNull(response.getTemporary());
        createdAt = Objects.requireNonNull(response.getCreatedAt());
        revoked = Objects.requireNonNull(response.getRevoked());
    }

    public ExtendedInviteBean() {}

    public long getInviterId() {
        return inviterId;
    }

    public void setInviterId(final long inviterId) {
        this.inviterId = inviterId;
    }

    public int getUses() {
        return uses;
    }

    public void setUses(final int uses) {
        this.uses = uses;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(final int maxUses) {
        this.maxUses = maxUses;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(final int maxAge) {
        this.maxAge = maxAge;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(final boolean temporary) {
        this.temporary = temporary;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(final boolean revoked) {
        this.revoked = revoked;
    }

    @Override
    public String toString() {
        return "ExtendedInviteBean{" +
                "inviterId=" + inviterId +
                ", uses=" + uses +
                ", maxUses=" + maxUses +
                ", maxAge=" + maxAge +
                ", temporary=" + temporary +
                ", createdAt='" + createdAt + '\'' +
                ", revoked=" + revoked +
                "} " + super.toString();
    }
}
