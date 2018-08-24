package discord4j.core.object;

import discord4j.core.object.util.Snowflake;

import java.util.Optional;

public class TargetedPermissionOverwrite extends PermissionOverwrite {

    private final long targetId;
    private final Type type;

    TargetedPermissionOverwrite(long allowed, long denied, long targetId, String type) {
        super(allowed, denied);
        this.targetId = targetId;
        this.type = Type.of(type);
    }

    public Snowflake getTargetId() {
        return Snowflake.of(targetId);
    }

    public Optional<Snowflake> getRoleId() {
        return type == Type.ROLE ? Optional.of(getTargetId()) : Optional.empty();
    }

    public Optional<Snowflake> getUserId() {
        return type == Type.MEMBER ? Optional.of(getTargetId()) : Optional.empty();
    }

    public Type getType() {
        return type;
    }
}
