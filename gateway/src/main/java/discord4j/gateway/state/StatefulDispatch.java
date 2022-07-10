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
package discord4j.gateway.state;

import discord4j.gateway.ShardInfo;
import reactor.util.annotation.Nullable;

import java.util.Optional;

public class StatefulDispatch<D, S> {

    private final ShardInfo shardInfo;
    private final D dispatch;
    private final S oldState;

    private StatefulDispatch(ShardInfo shardInfo, D dispatch, S oldState) {
        this.shardInfo = shardInfo;
        this.dispatch = dispatch;
        this.oldState = oldState;
    }

    public static <D, S> StatefulDispatch<D, S> of(ShardInfo shardInfo, D dispatch,
                                                   @Nullable S oldState) {
        return new StatefulDispatch<>(shardInfo, dispatch, oldState);
    }

    public ShardInfo getShardInfo() {
        return shardInfo;
    }

    public D getDispatch() {
        return dispatch;
    }

    public Optional<S> getOldState() {
        return Optional.ofNullable(oldState);
    }

    @Override
    public String toString() {
        return "StatefulDispatch{" +
            "shardInfo=" + shardInfo +
            ", dispatch=" + dispatch +
            ", oldState=" + oldState +
            '}';
    }
}
