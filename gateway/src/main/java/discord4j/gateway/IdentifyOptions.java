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

import discord4j.gateway.json.StatusUpdate;

import javax.annotation.Nullable;

public class IdentifyOptions {

    private final int shardIndex;

    private final int shardCount;

    @Nullable
    private final StatusUpdate initialStatus;

    @Nullable
    private volatile Integer resumeSequence;

    @Nullable
    private volatile String resumeSessionId;

    public IdentifyOptions(int shardIndex, int shardCount, @Nullable StatusUpdate initialStatus) {
        this.shardIndex = shardIndex;
        this.shardCount = shardCount;
        this.initialStatus = initialStatus;
    }

    public int getShardIndex() {
        return shardIndex;
    }

    public int getShardCount() {
        return shardCount;
    }

    @Nullable
    public StatusUpdate getInitialStatus() {
        return initialStatus;
    }

    @Nullable
    public Integer getResumeSequence() {
        return resumeSequence;
    }

    public void setResumeSequence(@Nullable Integer resumeSequence) {
        this.resumeSequence = resumeSequence;
    }

    @Nullable
    public String getResumeSessionId() {
        return resumeSessionId;
    }

    public void setResumeSessionId(@Nullable String resumeSessionId) {
        this.resumeSessionId = resumeSessionId;
    }

    @Override
    public String toString() {
        return "IdentifyOptions{" +
                "shardIndex=" + shardIndex +
                ", shardCount=" + shardCount +
                ", initialStatus=" + initialStatus +
                ", resumeSequence=" + resumeSequence +
                ", resumeSessionId='" + resumeSessionId + '\'' +
                '}';
    }
}
