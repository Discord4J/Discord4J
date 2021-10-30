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

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.rest.util.PermissionSet;
import discord4j.common.util.Snowflake;

import java.util.Optional;

public class PermissionOverwrite {

    /**
     * Constructs a {@code PermissionOverwrite} targeting a {@link discord4j.core.object.entity.Member}.
     *
     * @param memberId The ID of the member to target.
     * @param allowed The permissions explicitly allowed by the overwrite.
     * @param denied The permissions explicitly denied by the overwrite.
     * @return A {@code PermissionOverwrite} targeting the given member.
     */
    public static PermissionOverwrite forMember(Snowflake memberId, PermissionSet allowed, PermissionSet denied) {
        return new PermissionOverwrite(allowed.getRawValue(), denied.getRawValue(), memberId.asLong(), Type.MEMBER);
    }

    /**
     * Constructs a {@code PermissionOverwrite} targeting a {@link discord4j.core.object.entity.Role}.
     *
     * @param roleId The ID of the role to target.
     * @param allowed The permissions explicitly allowed by the overwrite.
     * @param denied The permissions explicitly denied by the overwrite.
     * @return A {@code PermissionOverwrite} targeting the given role.
     */
    public static PermissionOverwrite forRole(Snowflake roleId, PermissionSet allowed, PermissionSet denied) {
        return new PermissionOverwrite(allowed.getRawValue(), denied.getRawValue(), roleId.asLong(), Type.ROLE);
    }

    private final long allowed;
    private final long denied;
    private final long targetId;
    private final Type type;

    PermissionOverwrite(long allowed, long denied, long targetId, Type type) {
        this.allowed = allowed;
        this.denied = denied;
        this.targetId = targetId;
        this.type = type;
    }

    /**
     * Gets the permissions explicitly allowed by this overwrite.
     * @return The permissions explicitly allowed by this overwrite.
     */
    public PermissionSet getAllowed() {
        return PermissionSet.of(allowed);
    }

    /**
     * Gets the permissions explicitly denied by this overwrite.
     * @return The permissions explicitly denied by this overwrite.
     */
    public PermissionSet getDenied() {
        return PermissionSet.of(denied);
    }

    /**
     * Gets the ID of the entity this overwrite targets. This is either a role ID or a member ID.
     * @return The ID of the entity this overwrite targets.
     *
     * @see #getRoleId()
     * @see #getMemberId()
     */
    public Snowflake getTargetId() {
        return Snowflake.of(targetId);
    }

    /**
     * Gets the ID of the role this overwrite targets.
     * @return The ID of the role this overwrite targets.
     */
    public Optional<Snowflake> getRoleId() {
        return type == Type.ROLE ? Optional.of(getTargetId()) : Optional.empty();
    }

    /**
     * Gets the ID of the member this overwrite targets.
     * @return The ID of the member this overwrite targets.
     */
    public Optional<Snowflake> getMemberId() {
        return type == Type.MEMBER ? Optional.of(getTargetId()) : Optional.empty();
    }

    /**
     * Gets the type of the overwrite.
     * @return The type of the overwrite.
     */
    public Type getType() {
        return type;
    }

    /** The type of entity a {@link PermissionOverwrite} is for. */
    public enum Type {

        /** Unknown type. */
        UNKNOWN(-1),

        /** The {@link Role} entity. */
        ROLE(0),

        /** The {@link Member} entity. */
        MEMBER(1);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code PermissionOverwrite.Type}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Type(final int value) {
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
         * Gets the type of permission overwrite. It is guaranteed that invoking {@link #getValue()} from the returned
         * enum will equal ({@link #equals(Object)}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of permission overwrite.
         */
        public static Type of(final int value) {
            switch (value) {
                case 0: return ROLE;
                case 1: return MEMBER;
                default: return UNKNOWN;
            }
        }
    }

    @Override
    public String toString() {
        return "PermissionOverwrite{" +
                "allowed=" + allowed +
                ", denied=" + denied +
                ", targetId=" + targetId +
                ", type=" + type +
                '}';
    }
}
