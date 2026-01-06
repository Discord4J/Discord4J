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


import discord4j.common.store.api.object.InvalidationCause;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/**
 * Holds the storage configuration for a {@link LocalStoreLayout}.
 */
public class StorageConfig {

    private final StorageBackend messageBackend;
    private final EnumSet<InvalidationCause> invalidationFilter;

    private StorageConfig(Builder b) {
        this.messageBackend = b.messageBackend == null ? defaultMessageBackend() : b.messageBackend;
        this.invalidationFilter = b.invalidationFilter == null ? defaultInvalidationFilter() : b.invalidationFilter;
    }

    /**
     * Creates a new builder to customize a {@link StorageConfig}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private static StorageBackend defaultMessageBackend() {
        return StorageBackend.caffeine(builder -> builder.maximumSize(1000L));
    }

    private static EnumSet<InvalidationCause> defaultInvalidationFilter() {
        return EnumSet.allOf(InvalidationCause.class);
    }

    /**
     * Returns the {@link StorageBackend} to use for message caching.
     *
     * @return the message backend
     */
    public StorageBackend getMessageBackend() {
        return messageBackend;
    }

    /**
     * Returns the filter represented as a subset of {@link InvalidationCause} to apply when a shard is invalidated.
     *
     * @return the invalidation filter
     */
    public EnumSet<InvalidationCause> getInvalidationFilter() {
        return invalidationFilter;
    }

    public static class Builder {

        private @Nullable StorageBackend messageBackend;
        private @Nullable EnumSet<InvalidationCause> invalidationFilter;

        private Builder() {
        }

        /**
         * Sets the {@link StorageBackend} to use for message caching. By default it uses a backend based on Caffeine
         * that keeps only the last 1000 messages.
         *
         * @param messageBackend the {@link StorageBackend}, or null to use default
         * @return this builder
         */
        public Builder setMessageBackend(@Nullable StorageBackend messageBackend) {
            this.messageBackend = messageBackend;
            return this;
        }

        /**
         * Sets the filter represented as a subset of {@link InvalidationCause} to apply when a shard is invalidated.
         * Only the causes included in the set will trigger cleanup of data related to the shard that's being
         * invalidated. By default no filter is applied and data will be cleared on shard invalidation regardless of
         * the reason ({@code EnumSet.allOf(InvalidationCause.class)})
         *
         * @param invalidationFilter the filter as an {@link EnumSet}, or null to use default
         * @return this builder
         */
        public Builder setInvalidationFilter(@Nullable EnumSet<InvalidationCause> invalidationFilter) {
            this.invalidationFilter = invalidationFilter;
            return this;
        }

        /**
         * Builds the {@link StorageConfig}.
         *
         * @return a new {@link StorageConfig}
         */
        public StorageConfig build() {
            return new StorageConfig(this);
        }
    }
}
