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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.common.store.api.layout;

import discord4j.common.store.api.ActionMapper;
import discord4j.common.store.api.StoreFlag;

import java.util.EnumSet;

/**
 * A {@link StoreLayout} defines how store actions should be handled according to their type. It enforces an
 * implementation for a minimal set of actions required by Discord4J, and enables the declaration of custom action
 * types.
 */
public interface StoreLayout {

    /**
     * Returns a {@link DataAccessor} that defines action handlers for reading data from the store.
     *
     * @return a {@link DataAccessor}
     */
    DataAccessor getDataAccessor();

    /**
     * Returns a {@link GatewayDataUpdater} that defines action handlers for updates received from the Discord gateway.
     *
     * @return a {@link GatewayDataUpdater}
     */
    GatewayDataUpdater getGatewayDataUpdater();

    /**
     * Defines a mapping for custom action types. By default, returns an empty {@link ActionMapper}, implementations
     * may override this method to supply custom mappings.
     *
     * @return an {@link ActionMapper}
     */
    default ActionMapper getCustomActionMapper() {
        return ActionMapper.empty();
    }

    /**
     * Return a set of {@link StoreFlag} values to selectively enable store actions. By default, it returns all values,
     * meaning this layout will enable all available store actions.
     *
     * @return a set of flags to partially enable store actions
     * @since 3.2.5
     */
    default EnumSet<StoreFlag> getEnabledFlags() {
        return EnumSet.allOf(StoreFlag.class);
    }
}
