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

import discord4j.discordjson.Id;
import discord4j.discordjson.json.ImmutableGuildData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class WrappedGuildData {

    private final ImmutableGuildData guild;
    private final List<Id> members;
    private final List<Id> emojis;
    private final List<Id> stickers;
    private final List<Id> channels;
    private final List<Id> roles;

    WrappedGuildData(ImmutableGuildData guild) {
        this.guild = ImmutableGuildData.builder()
                .from(guild)
                .members(Collections.emptyList())
                .emojis(Collections.emptyList())
                .stickers(Collections.emptyList())
                .channels(Collections.emptyList())
                .roles(Collections.emptyList())
                .build();
        this.members = new ArrayList<>(guild.members());
        this.emojis = new ArrayList<>(guild.emojis());
        this.stickers = new ArrayList<>(guild.stickers().toOptional().orElse(Collections.emptyList()));
        this.channels = new ArrayList<>(guild.channels());
        this.roles = new ArrayList<>(guild.roles());
    }

    ImmutableGuildData unwrap() {
        return ImmutableGuildData.builder()
                .from(guild)
                .members(new ArrayList<>(members))
                .emojis(new ArrayList<>(emojis))
                .stickers(new ArrayList<>(stickers))
                .channels(new ArrayList<>(channels))
                .roles(new ArrayList<>(roles))
                .build();
    }

    List<Id> getMembers() {
        return members;
    }

    List<Id> getEmojis() {
        return emojis;
    }

    List<Id> getStickers() {
        return stickers;
    }

    List<Id> getChannels() {
        return channels;
    }

    List<Id> getRoles() {
        return roles;
    }
}
