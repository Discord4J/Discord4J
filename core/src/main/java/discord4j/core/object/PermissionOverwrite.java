package discord4j.core.object;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import discord4j.core.util.EntityUtil;

public class PermissionOverwrite {

    public static PermissionOverwrite of(PermissionSet allowed, PermissionSet denied) {
        return new PermissionOverwrite(allowed.getRawValue(), denied.getRawValue());
    }

    public static TargetedPermissionOverwrite forMember(Snowflake memberId, PermissionSet allowed, PermissionSet denied) {
        return new TargetedPermissionOverwrite(allowed.getRawValue(), denied.getRawValue(), memberId.asLong(), Type.MEMBER.value);
    }

    public static TargetedPermissionOverwrite forRole(Snowflake roleId, PermissionSet allowed, PermissionSet denied) {
        return new TargetedPermissionOverwrite(allowed.getRawValue(), denied.getRawValue(), roleId.asLong(), Type.ROLE.value);
    }

    private final long allowed;
    private final long denied;

    PermissionOverwrite(long allowed, long denied) {
        this.allowed = allowed;
        this.denied = denied;
    }

    public PermissionSet getAllowed() {
        return PermissionSet.of(allowed);
    }

    public PermissionSet getDenied() {
        return PermissionSet.of(denied);
    }

    /** The type of entity a {@link PermissionOverwrite} is explicitly for. */
    public enum Type {

        /** The {@link Role} entity. */
        ROLE("role"),

        /** The {@link Member} entity. */
        MEMBER("member");

        /** The underlying value as represented by Discord. */
        private final String value;

        /**
         * Constructs a {@code ExtendedPermissionOverwrite.Type}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Type(final String value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public String getValue() {
            return value;
        }

        /**
         * Gets the type of permission overwrite. It is guaranteed that invoking {@link #getValue()} from the returned
         * enum will equal ({@link #equals(Object)}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of permission overwrite.
         */
        public static Type of(final String value) {
            switch (value) {
                case "role": return ROLE;
                case "member": return MEMBER;
                default: return EntityUtil.throwUnsupportedDiscordValue(value);
            }
        }
    }

    @Override
    public String toString() {
        return "PermissionOverwrite{" +
                "data=" + data +
                ", guildId=" + guildId +
                ", channelId=" + channelId +
                '}';
    }
}
