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
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.intent.IntentSet;
import reactor.util.annotation.Nullable;

/**
 * An object that contains all the parameters used for identifying a bot to Discord gateway.
 * <p>
 * If you register a {@link GatewayObserver} when building a client, you can receive the current
 * {@code IdentifyOptions} with updated values until that point, and used to resume a session.
 */
public class IdentifyOptions {

    private final ShardInfo shardInfo;

    @Nullable
    private final StatusUpdate initialStatus;

    private final Possible<Long> intents;

    private final boolean guildSubscriptions;

    @Nullable
    private volatile Integer resumeSequence;

    @Nullable
    private volatile String resumeSessionId;

    /**
     * Create a new identifying policy.
     *
     * @param shardInfo shard index and count the client using this policy will identify with
     * @param initialStatus initial presence status the bot will identify with
     * @param intents intents to subscribe from the gateway or just {@code Possible.absent()}
     * @param guildSubscriptions whether to enable presence and typing events while identifying
     */
    public IdentifyOptions(ShardInfo shardInfo, @Nullable StatusUpdate initialStatus, Possible<IntentSet> intents, boolean guildSubscriptions) {
        this.shardInfo = shardInfo;
        this.initialStatus = initialStatus;
        if(!intents.isAbsent()) {
            this.intents = Possible.of(intents.get().getRawValue());
        } else {
            this.intents = Possible.absent();
        }
        this.guildSubscriptions = guildSubscriptions;
    }

    public ShardInfo getShardInfo() {
        return shardInfo;
    }

    /**
     * Retrieve the shard index in this policy.
     *
     * @return an identifier indicating the shard number used by this policy
     */
    public int getShardIndex() {
        return shardInfo.getIndex();
    }

    /**
     * Retrieve the number of shards used by this policy.
     *
     * @return number of shards the client using this policy will identify with
     */
    public int getShardCount() {
        return shardInfo.getCount();
    }

    /**
     * Retrieve the initial status used to identify bots.
     *
     * @return the presence used to identify bots
     */
    @Nullable
    public StatusUpdate getInitialStatus() {
        return initialStatus;
    }

    /**
     * Retrieve the intents which should be subscribed from the gateway when identifying.
     *
     * @return {@code Possible.absent()} when no intents are set or the raw intent value which should be subscribed
     */
    public Possible<Long> getIntents() {
        return intents;
    }

    /**
     * Retrieve whether to enable presence and typing events when identifying.
     *
     * @return {@code true} if guild subscriptions should be enabled, {@code false} otherwise
     */
    public boolean isGuildSubscriptions() {
        return guildSubscriptions;
    }

    /**
     * Retrieve the last gateway sequence observed by the client using this policy. It is one of the two required
     * values required to resume a gateway session, the other one being {@link #getResumeSessionId()}.
     *
     * @return the last observed gateway sequence number
     */
    @Nullable
    public Integer getResumeSequence() {
        return resumeSequence;
    }

    /**
     * Set a new value representing the last observed gateway sequence. Modifying this value after a connection has
     * been established will lead to unexpected behavior.
     *
     * @param resumeSequence the new observed gateway sequence
     */
    public void setResumeSequence(@Nullable Integer resumeSequence) {
        this.resumeSequence = resumeSequence;
    }

    /**
     * Retrieve the unique gateway session identifier for the current session. It is one of the two required
     * values required to resume a gateway session, the other one being {@link #getResumeSequence()}.
     *
     * @return the current session id
     */
    @Nullable
    public String getResumeSessionId() {
        return resumeSessionId;
    }

    /**
     * Set a new session id for the client using this policy. Modifying this value after a connection has been
     * established will lead to unexpected behavior.
     *
     * @param resumeSessionId the new session id
     */
    public void setResumeSessionId(@Nullable String resumeSessionId) {
        this.resumeSessionId = resumeSessionId;
    }

    @Override
    public String toString() {
        return "IdentifyOptions{" +
                "shardInfo=" + shardInfo +
                ", initialStatus=" + initialStatus +
                ", guildSubscriptions=" + guildSubscriptions +
                '}';
    }
}
