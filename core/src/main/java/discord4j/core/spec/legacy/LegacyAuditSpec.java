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

import reactor.util.annotation.Nullable;

/** A spec which can optionally have a reason in the audit logs when built. */
public interface LegacyAuditSpec<T> extends LegacySpec<T> {

    /**
     * Sets the reason to show in the audit logs when the spec is built.
     *
     * @param reason The audit log reason.
     * @return This spec.
     */
    LegacyAuditSpec<T> setReason(@Nullable String reason);

    /**
     * Returns the current audit log reason set on the spec.
     *
     * @return The current audit log reason.
     */
    @Nullable
    String getReason();
}
