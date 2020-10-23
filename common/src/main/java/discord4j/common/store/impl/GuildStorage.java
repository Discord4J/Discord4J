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

import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.GuildData;
import discord4j.discordjson.json.UserData;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

class GuildStorage extends Storage<GuildNode, GuildData> {

    private final Storage<ChannelNode, ChannelData> channelStorage;

    GuildStorage(CaffeineRegistry caffeineRegistry, Storage<ChannelNode, ChannelData> channelStorage,
                 IdentityStorage<AtomicReference<UserData>> userStorage) {
        super(caffeineRegistry.getGuildCaffeine(),
                data -> LocalStoreLayout.toLongId(data.id()),
                data -> new GuildNode(data, userStorage, caffeineRegistry),
                GuildNode::getData,
                GuildNode::setData);
        this.channelStorage = channelStorage;
    }

    @Override
    Optional<GuildData> delete(long id) {
        findNode(id).ifPresent(node -> channelStorage.cache.invalidateAll(node.getChannelIds()));
        return super.delete(id);
    }

    void invalidateShard(int shardIndex, int shardCount) {
        Set<Long> toRemove = cache.asMap().keySet().stream()
                .filter(guildId -> ((guildId >> 22) % shardCount) == shardIndex)
                .collect(Collectors.toSet());
        toRemove.forEach(this::delete);
    }
}
