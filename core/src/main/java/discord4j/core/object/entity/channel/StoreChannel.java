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
package discord4j.core.object.entity.channel;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.StoreChannelEditMono;
import discord4j.discordjson.json.ChannelData;
import reactor.core.publisher.Mono;

/** A Discord store channel. */
public final class StoreChannel extends BaseCategorizableChannel {

    /**
     * Constructs an {@code StoreChannel} with an associated ServiceMediator and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public StoreChannel(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
    }

    /**
     * Requests to edit this store channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link StoreChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public StoreChannelEditMono edit() {
        return new StoreChannelEditMono(getClient(), getId().asLong());
    }

    @Override
    public String toString() {
        return "StoreChannel{} " + super.toString();
    }
}
