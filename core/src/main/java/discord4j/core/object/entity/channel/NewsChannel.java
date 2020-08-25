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
import discord4j.core.spec.NewsChannelEditMono;
import discord4j.discordjson.json.ChannelData;
import reactor.core.publisher.Mono;

/** A Discord news channel. */
public final class NewsChannel extends BaseGuildMessageChannel {

    /**
     * Constructs an {@code NewsChannel} with an associated ServiceMediator and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public NewsChannel(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
    }

    /**
     * Requests to edit this news channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link NewsChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public NewsChannelEditMono edit() {
        return new NewsChannelEditMono(getClient(), getId().asLong());
    }

    @Override
    public String toString() {
        return "NewsChannel{} " + super.toString();
    }
}
