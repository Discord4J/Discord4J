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

package discord4j.gateway;

import discord4j.discordjson.json.gateway.StatusUpdate;
import discord4j.gateway.intent.IntentSet;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * An object that contains parameters used for authenticating a bot to Discord gateway.
 */
public class IdentifyOptions {

    private final ShardInfo shardInfo;
    @Nullable
    private final StatusUpdate initialStatus;
    @Nullable
    private final IntentSet intents;
    private final int largeThreshold;
    @Nullable
    private final SessionInfo resumeSession;

    /**
     * Create a new Gateway authentication policy.
     *
     * @param builder a builder used to configure this object
     */
    protected IdentifyOptions(Builder builder) {
        this.shardInfo = builder.shardInfo;
        this.initialStatus = builder.initialStatus;
        this.intents = builder.intents;
        this.largeThreshold = builder.largeThreshold;
        this.resumeSession = builder.resumeSession;
    }

    /**
     * Create a default {@link IdentifyOptions} using the given shard index and count.
     *
     * @param shardIndex the shard index for authentication
     * @param shardCount the shard count for authentication
     * @return a default authentication policy
     */
    public static IdentifyOptions create(int shardIndex, int shardCount) {
        return builder(ShardInfo.create(shardIndex, shardCount)).build();
    }

    /**
     * Create a default {@link IdentifyOptions} using the given shard information.
     *
     * @param shardInfo the shard index and count to be used when authenticating
     * @return a default authentication policy
     */
    public static IdentifyOptions create(ShardInfo shardInfo) {
        return builder(shardInfo).build();
    }

    /**
     * Create a builder to create an {@link IdentifyOptions} using the given shard information.
     *
     * @param shardIndex the shard index for authentication
     * @param shardCount the shard count for authentication
     * @return a {@link Builder}
     */
    public static Builder builder(int shardIndex, int shardCount) {
        return new Builder(ShardInfo.create(shardIndex, shardCount));
    }

    /**
     * Create a builder to create an {@link IdentifyOptions} using the given shard information.
     *
     * @param shardInfo the shard index and count to be used when authenticating
     * @return a {@link Builder}
     */
    public static Builder builder(ShardInfo shardInfo) {
        return new Builder(shardInfo);
    }

    /**
     * Builder to create {@link IdentifyOptions}. Requires specifying the shard information.
     */
    public static class Builder {

        private final ShardInfo shardInfo;
        private @Nullable StatusUpdate initialStatus;
        private @Nullable IntentSet intents;
        private int largeThreshold = 250;
        private @Nullable SessionInfo resumeSession;

        /**
         * Create a builder using the given shard information.
         *
         * @param shardInfo the shard index and count to be used when authenticating
         */
        protected Builder(ShardInfo shardInfo) {
            this.shardInfo = ShardInfo.create(shardInfo.getIndex(), shardInfo.getCount());
        }

        /**
         * Set the initial presence status the bot will identify with.
         *
         * @param initialStatus a {@link StatusUpdate} to be used when authenticating
         * @return this builder
         */
        public Builder initialStatus(@Nullable StatusUpdate initialStatus) {
            this.initialStatus = initialStatus;
            return this;
        }

        /**
         * Set the Gateway intents to use when authenticating.
         *
         * @param intents an {@link IntentSet} for authenticating, or {@code null} if not using this capability
         * @return this builder
         */
        public Builder intents(@Nullable IntentSet intents) {
            this.intents = intents;
            return this;
        }

        /**
         * Set the number of members a guild must have to be considered "large". Defaults to 250.
         *
         * @param largeThreshold the number of guild members to identify a large guild
         * @return this builder
         */
        public Builder largeThreshold(int largeThreshold) {
            this.largeThreshold = largeThreshold;
            return this;
        }

        /**
         * Set information about a Gateway session to be resumed. If not specified, a normal authentication is
         * performed, creating a fresh session to the Gateway.
         *
         * @param resumeSession a {@link SessionInfo} for resumption, or {@code null} if not using this capability
         * @return this builder
         */
        public Builder resumeSession(@Nullable SessionInfo resumeSession) {
            this.resumeSession = resumeSession;
            return this;
        }

        /**
         * Construct the authentication policy.
         *
         * @return a built {@link IdentifyOptions}
         */
        public IdentifyOptions build() {
            return new IdentifyOptions(this);
        }
    }

    /**
     * Derive a {@link Builder} from this object, reusing all properties.
     *
     * @return a {@link Builder} for further configuration
     */
    public Builder mutate() {
        return new Builder(shardInfo)
                .initialStatus(initialStatus)
                .intents(intents)
                .largeThreshold(largeThreshold)
                .resumeSession(resumeSession);
    }

    /**
     * Derive a {@link Builder} from this object, targeting a different {@link ShardInfo} but reusing all other
     * properties.
     *
     * @param shardInfo the shard information for authentication to be used in the builder
     * @return a {@link Builder} for further configuration
     */
    public Builder mutate(ShardInfo shardInfo) {
        return new Builder(shardInfo)
                .initialStatus(initialStatus)
                .intents(intents)
                .largeThreshold(largeThreshold)
                .resumeSession(resumeSession);
    }

    /**
     * Retrieve the {@link ShardInfo} to be used when authenticating, specifying shard index and count.
     *
     * @return the shard information used by this object
     */
    public ShardInfo getShardInfo() {
        return shardInfo;
    }

    /**
     * Retrieve the initial status used to identify bots.
     *
     * @return the presence used to identify bots
     */
    public Optional<@Nullable StatusUpdate> getInitialStatus() {
        return Optional.ofNullable(initialStatus);
    }

    /**
     * Retrieve the intents which should be subscribed from the gateway when identifying.
     *
     * @return {@code Possible.absent()} when no intents are set or the raw intent value which should be subscribed
     */
    public Optional<@Nullable IntentSet> getIntents() {
        return Optional.ofNullable(intents);
    }

    /**
     * Retrieve the number of members used to determine if a guild is "large". Gateway will not send offline member
     * information for a large guild member list.
     *
     * @return the value used to determine if a guild is considered large
     */
    public int getLargeThreshold() {
        return largeThreshold;
    }

    /**
     * Retrieve the {@link SessionInfo} that should be used to resume a Gateway session.
     *
     * @return the session details for resumption
     */
    public Optional<@Nullable SessionInfo> getResumeSession() {
        return Optional.ofNullable(resumeSession);
    }

    @Override
    public String toString() {
        return "IdentifyOptions{" +
                "shardInfo=" + shardInfo +
                ", initialStatus=" + initialStatus +
                ", intents=" + intents +
                ", largeThreshold=" + largeThreshold +
                ", resumeSession=" + resumeSession +
                '}';
    }
}
