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

import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Holds the caffeine configurations for each type of cache managed by the {@link LocalStoreLayout}.
 */
public class CaffeineRegistry {

    private final Supplier<Caffeine<Object, Object>> channelCaffeine;
    private final Supplier<Caffeine<Object, Object>> emojiCaffeine;
    private final Supplier<Caffeine<Object, Object>> guildCaffeine;
    private final Supplier<Caffeine<Object, Object>> memberCaffeine;
    private final Supplier<Caffeine<Object, Object>> messageCaffeine;
    private final Supplier<Caffeine<Object, Object>> presenceCaffeine;
    private final Supplier<Caffeine<Object, Object>> roleCaffeine;
    private final Supplier<Caffeine<Object, Object>> userCaffeine;
    private final Supplier<Caffeine<Object, Object>> voiceStateCaffeine;

    private CaffeineRegistry(Builder b) {
        this.channelCaffeine = b.channelCaffeine == null ? CaffeineRegistry::defaultChannelCaffeine : b.channelCaffeine;
        this.emojiCaffeine = b.emojiCaffeine == null ? CaffeineRegistry::defaultEmojiCaffeine : b.emojiCaffeine;
        this.guildCaffeine = b.guildCaffeine == null ? CaffeineRegistry::defaultGuildCaffeine : b.guildCaffeine;
        this.memberCaffeine = b.memberCaffeine == null ? CaffeineRegistry::defaultMemberCaffeine : b.memberCaffeine;
        this.messageCaffeine = b.messageCaffeine == null ? CaffeineRegistry::defaultMessageCaffeine : b.messageCaffeine;
        this.presenceCaffeine = b.presenceCaffeine == null ? CaffeineRegistry::defaultPresenceCaffeine : b.presenceCaffeine;
        this.roleCaffeine = b.roleCaffeine == null ? CaffeineRegistry::defaultRoleCaffeine : b.roleCaffeine;
        this.userCaffeine = b.userCaffeine == null ? CaffeineRegistry::defaultUserCaffeine : b.userCaffeine;
        this.voiceStateCaffeine = b.voiceStateCaffeine == null ? CaffeineRegistry::defaultVoiceStateCaffeine : b.voiceStateCaffeine;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a fresh {@link Caffeine} instance for the channel cache.
     *
     * @return a {@link Caffeine}
     */
    public Caffeine<Object, Object> getChannelCaffeine() {
        return channelCaffeine.get();
    }

    /**
     * Returns a fresh {@link Caffeine} instance for the emoji cache.
     *
     * @return a {@link Caffeine}
     */
    public Caffeine<Object, Object> getEmojiCaffeine() {
        return emojiCaffeine.get();
    }

    /**
     * Returns a fresh {@link Caffeine} instance for the guild cache.
     *
     * @return a {@link Caffeine}
     */
    public Caffeine<Object, Object> getGuildCaffeine() {
        return guildCaffeine.get();
    }

    /**
     * Returns a fresh {@link Caffeine} instance for the member cache.
     *
     * @return a {@link Caffeine}
     */
    public Caffeine<Object, Object> getMemberCaffeine() {
        return memberCaffeine.get();
    }

    /**
     * Returns a fresh {@link Caffeine} instance for the message cache.
     *
     * @return a {@link Caffeine}
     */
    public Caffeine<Object, Object> getMessageCaffeine() {
        return messageCaffeine.get();
    }

    /**
     * Returns a fresh {@link Caffeine} instance for the presence cache.
     *
     * @return a {@link Caffeine}
     */
    public Caffeine<Object, Object> getPresenceCaffeine() {
        return presenceCaffeine.get();
    }

    /**
     * Returns a fresh {@link Caffeine} instance for the role cache.
     *
     * @return a {@link Caffeine}
     */
    public Caffeine<Object, Object> getRoleCaffeine() {
        return roleCaffeine.get();
    }

    /**
     * Returns a fresh {@link Caffeine} instance for the user cache.
     *
     * @return a {@link Caffeine}
     */
    public Caffeine<Object, Object> getUserCaffeine() {
        return userCaffeine.get();
    }

    /**
     * Returns a fresh {@link Caffeine} instance for the voice state cache.
     *
     * @return a {@link Caffeine}
     */
    public Caffeine<Object, Object> getVoiceStateCaffeine() {
        return voiceStateCaffeine.get();
    }

    /**
     * Provides the {@link Caffeine} used by default for channel cache.
     *
     * @return a {@link Caffeine}
     */
    public static Caffeine<Object, Object> defaultChannelCaffeine() {
        return Caffeine.newBuilder();
    }

    /**
     * Provides the {@link Caffeine} used by default for emoji cache.
     *
     * @return a {@link Caffeine}
     */
    public static Caffeine<Object, Object> defaultEmojiCaffeine() {
        return Caffeine.newBuilder();
    }

    /**
     * Provides the {@link Caffeine} used by default for guild cache.
     *
     * @return a {@link Caffeine}
     */
    public static Caffeine<Object, Object> defaultGuildCaffeine() {
        return Caffeine.newBuilder();
    }

    /**
     * Provides the {@link Caffeine} used by default for member cache.
     *
     * @return a {@link Caffeine}
     */
    public static Caffeine<Object, Object> defaultMemberCaffeine() {
        return Caffeine.newBuilder();
    }

    /**
     * Provides the {@link Caffeine} used by default for message cache. It keeps only the 100 last messages of each
     * channel that are less than 2 weeks old.
     *
     * @return a {@link Caffeine}
     */
    public static Caffeine<Object, Object> defaultMessageCaffeine() {
        return Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofDays(14))
                .maximumSize(100L);
    }

    /**
     * Provides the {@link Caffeine} used by default for presence cache.
     *
     * @return a {@link Caffeine}
     */
    public static Caffeine<Object, Object> defaultPresenceCaffeine() {
        return Caffeine.newBuilder();
    }

    /**
     * Provides the {@link Caffeine} used by default for role cache.
     *
     * @return a {@link Caffeine}
     */
    public static Caffeine<Object, Object> defaultRoleCaffeine() {
        return Caffeine.newBuilder();
    }

    /**
     * Provides the {@link Caffeine} used by default for user cache. It makes the values weak which allows users to
     * be garbage collected when no member or presence reference them anymore.
     *
     * @return a {@link Caffeine}
     */
    public static Caffeine<Object, Object> defaultUserCaffeine() {
        return Caffeine.newBuilder().weakValues();
    }

    /**
     * Provides the {@link Caffeine} used by default for voice state cache.
     *
     * @return a {@link Caffeine}
     */
    public static Caffeine<Object, Object> defaultVoiceStateCaffeine() {
        return Caffeine.newBuilder();
    }

    public static class Builder {

        private Supplier<Caffeine<Object, Object>> channelCaffeine;
        private Supplier<Caffeine<Object, Object>> emojiCaffeine;
        private Supplier<Caffeine<Object, Object>> guildCaffeine;
        private Supplier<Caffeine<Object, Object>> memberCaffeine;
        private Supplier<Caffeine<Object, Object>> messageCaffeine;
        private Supplier<Caffeine<Object, Object>> presenceCaffeine;
        private Supplier<Caffeine<Object, Object>> roleCaffeine;
        private Supplier<Caffeine<Object, Object>> userCaffeine;
        private Supplier<Caffeine<Object, Object>> voiceStateCaffeine;
        
        private Builder() {
        }

        /**
         * Overrides the default {@link Caffeine} to supply for the channel cache.
         *
         * @param channelCaffeine a supplier for the {@link Caffeine}
         * @return this builder
         * @see #defaultChannelCaffeine()
         */
        public Builder setChannelCaffeine(Supplier<Caffeine<Object, Object>> channelCaffeine) {
            this.channelCaffeine = channelCaffeine;
            return this;
        }

        /**
         * Overrides the default {@link Caffeine} to supply for the emoji cache.
         *
         * @param emojiCaffeine a supplier for the {@link Caffeine}
         * @return this builder
         * @see #defaultEmojiCaffeine()
         */
        public Builder setEmojiCaffeine(Supplier<Caffeine<Object, Object>> emojiCaffeine) {
            this.emojiCaffeine = emojiCaffeine;
            return this;
        }

        /**
         * Overrides the default {@link Caffeine} to supply for the guild cache.
         *
         * @param guildCaffeine a supplier for the {@link Caffeine}
         * @return this builder
         * @see #defaultGuildCaffeine()
         */
        public Builder setGuildCaffeine(Supplier<Caffeine<Object, Object>> guildCaffeine) {
            this.guildCaffeine = guildCaffeine;
            return this;
        }

        /**
         * Overrides the default {@link Caffeine} to supply for the member cache.
         *
         * @param memberCaffeine a supplier for the {@link Caffeine}
         * @return this builder
         * @see #defaultMemberCaffeine()
         */
        public Builder setMemberCaffeine(Supplier<Caffeine<Object, Object>> memberCaffeine) {
            this.memberCaffeine = memberCaffeine;
            return this;
        }

        /**
         * Overrides the default {@link Caffeine} to supply for the message cache.
         *
         * @param messageCaffeine a supplier for the {@link Caffeine}
         * @return this builder
         * @see #defaultMemberCaffeine()
         */
        public Builder setMessageCaffeine(Supplier<Caffeine<Object, Object>> messageCaffeine) {
            this.messageCaffeine = messageCaffeine;
            return this;
        }

        /**
         * Overrides the default {@link Caffeine} to supply for the presence cache.
         *
         * @param presenceCaffeine a supplier for the {@link Caffeine}
         * @return this builder
         * @see #defaultPresenceCaffeine()
         */
        public Builder setPresenceCaffeine(Supplier<Caffeine<Object, Object>> presenceCaffeine) {
            this.presenceCaffeine = presenceCaffeine;
            return this;
        }

        /**
         * Overrides the default {@link Caffeine} to supply for the role cache.
         *
         * @param roleCaffeine a supplier for the {@link Caffeine}
         * @return this builder
         * @see #defaultRoleCaffeine()
         */
        public Builder setRoleCaffeine(Supplier<Caffeine<Object, Object>> roleCaffeine) {
            this.roleCaffeine = roleCaffeine;
            return this;
        }

        /**
         * Overrides the default {@link Caffeine} to supply for the user cache.
         *
         * @param userCaffeine a supplier for the {@link Caffeine}
         * @return this builder
         * @see #defaultEmojiCaffeine()
         */
        public Builder setUserCaffeine(Supplier<Caffeine<Object, Object>> userCaffeine) {
            this.userCaffeine = userCaffeine;
            return this;
        }

        /**
         * Overrides the default {@link Caffeine} to supply for the voice state cache.
         *
         * @param voiceStateCaffeine a supplier for the {@link Caffeine}
         * @return this builder
         * @see #defaultVoiceStateCaffeine()
         */
        public Builder setVoiceStateCaffeine(Supplier<Caffeine<Object, Object>> voiceStateCaffeine) {
            this.voiceStateCaffeine = voiceStateCaffeine;
            return this;
        }

        /**
         * Builds the {@link CaffeineRegistry}.
         *
         * @return a new {@link CaffeineRegistry}
         */
        public CaffeineRegistry build() {
            return new CaffeineRegistry(this);
        }
    }
}
