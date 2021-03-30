package discord4j.core.object.audit;

import discord4j.common.util.Snowflake;

import java.util.Objects;

public final class AuditLogRole {

    private final long id;
    private final String name;

    public AuditLogRole(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Snowflake getId() {
        return Snowflake.of(id);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuditLogRole that = (AuditLogRole) o;
        return id == that.id && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "AuditLogRole{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
