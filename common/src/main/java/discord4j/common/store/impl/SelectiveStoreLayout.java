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

package discord4j.common.store.impl;

import discord4j.common.store.api.StoreFlag;
import discord4j.common.store.api.layout.DataAccessor;
import discord4j.common.store.api.layout.GatewayDataUpdater;
import discord4j.common.store.api.layout.StoreLayout;

import java.util.EnumSet;

/**
 * {@link StoreLayout} that enables caches based on the passed in {@link StoreFlag} values.
 */
public class SelectiveStoreLayout implements StoreLayout {

    private final EnumSet<StoreFlag> enabledFlags;
    private final DataAccessor dataAccessor;
    private final GatewayDataUpdater gatewayDataUpdater;

    /**
     * Create a new store layout that can partially enable entity stores depending on passed {@code enabledStoreFlags}.
     * Refer to {@link StoreFlag} values to understand each affected store operation.
     *
     * @param enabledStoreFlags a set of values indicating how to partially enable the delegate capabilities
     * @param delegate a delegate for store queries and updates
     */
    public static <U extends DataAccessor & GatewayDataUpdater> StoreLayout create(EnumSet<StoreFlag> enabledStoreFlags, U delegate) {
        return new SelectiveStoreLayout(enabledStoreFlags, delegate, delegate);
    }

    /**
     * Create a new store layout that can partially enable entity stores depending on passed {@code enabledStoreFlags}.
     * Refer to {@link StoreFlag} values to understand affected store operations.
     *
     * @param enabledFlags a set of values indicating how to partially enable the delegate capabilities
     * @param dataAccessor a delegate for store queries
     * @param gatewayDataUpdater a delegate for store updates
     */
    public static StoreLayout create(EnumSet<StoreFlag> enabledFlags,
                                     DataAccessor dataAccessor,
                                     GatewayDataUpdater gatewayDataUpdater) {
        return new SelectiveStoreLayout(enabledFlags, dataAccessor, gatewayDataUpdater);
    }

    private SelectiveStoreLayout(EnumSet<StoreFlag> enabledFlags,
                                 DataAccessor dataAccessor,
                                 GatewayDataUpdater gatewayDataUpdater) {
        this.enabledFlags = enabledFlags;
        this.dataAccessor = dataAccessor;
        this.gatewayDataUpdater = gatewayDataUpdater;
    }

    @Override
    public DataAccessor getDataAccessor() {
        return dataAccessor;
    }

    @Override
    public GatewayDataUpdater getGatewayDataUpdater() {
        return gatewayDataUpdater;
    }

    @Override
    public EnumSet<StoreFlag> getEnabledFlags() {
        return enabledFlags;
    }
}
