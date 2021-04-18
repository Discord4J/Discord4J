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
package discord4j.core.object.audit;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.AuditLogPartialRoleData;

import java.util.Objects;

/**
 * A partial role with only an ID and a name. This is returned by Discord in an {@link AuditLogChange} for
 * {@link ChangeKey#ROLES_ADD} and {@link ChangeKey#ROLES_REMOVE}.
 */
public final class AuditLogRole {

    private final AuditLogPartialRoleData data;

    public AuditLogRole(AuditLogPartialRoleData data) {
        this.data = data;
    }

    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    public String getName() {
        return data.name();
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
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return "AuditLogRole{" +
                "data=" + data +
                '}';
    }
}
