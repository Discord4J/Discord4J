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
import discord4j.discordjson.json.OverwriteData;
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.possible.Possible;

import java.util.List;
import java.util.Optional;

/**
 * ChannelData with mutable lastMessageId
 */
class StoredChannelData implements ChannelData {
    
    private final String id;
    private final int type;
    private final Possible<String> guildId;
    private final Possible<Integer> position;
    private final Possible<List<OverwriteData>> permissionOverwrites;
    private final Possible<String> name;
    private final Possible<Optional<String>> topic;
    private final Possible<Boolean> nsfw;
    private volatile long lastMessageId; // -1 = absent, 0 = null
    private final Possible<Integer> bitrate;
    private final Possible<Integer> userLimit;
    private final Possible<Integer> rateLimitPerUser;
    private final Possible<List<UserData>> recipients;
    private final Possible<Optional<String>> icon;
    private final Possible<String> ownerId;
    private final Possible<String> applicationId;
    private final Possible<Optional<String>> parentId;
    private final Possible<Optional<String>> lastPinTimestamp;
    
    StoredChannelData(ChannelData original) {
        this.id = original.id();
        this.type = original.type();
        this.guildId = original.guildId();
        this.position = original.position();
        this.permissionOverwrites = original.permissionOverwrites();
        this.name = original.name();
        this.topic = original.topic();
        this.nsfw = original.nsfw();
        this.lastMessageId = original.lastMessageId().isAbsent() ? -1L :
                original.lastMessageId().get().map(ImplUtils::toLongId).orElse(0L);
        this.bitrate = original.bitrate();
        this.userLimit = original.userLimit();
        this.rateLimitPerUser = original.rateLimitPerUser();
        this.recipients = original.recipients();
        this.icon = original.icon();
        this.ownerId = original.ownerId();
        this.applicationId = original.applicationId();
        this.parentId = original.parentId();
        this.lastPinTimestamp = original.lastPinTimestamp();
    }

    void setLastMessageId(long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }
    
    @Override
    public String id() {
        return id;
    }

    @Override
    public int type() {
        return type;
    }

    @Override
    public Possible<String> guildId() {
        return guildId;
    }

    @Override
    public Possible<Integer> position() {
        return position;
    }

    @Override
    public Possible<List<OverwriteData>> permissionOverwrites() {
        return permissionOverwrites;
    }

    @Override
    public Possible<String> name() {
        return name;
    }

    @Override
    public Possible<Optional<String>> topic() {
        return topic;
    }

    @Override
    public Possible<Boolean> nsfw() {
        return nsfw;
    }

    @Override
    public Possible<Optional<String>> lastMessageId() {
        if (lastMessageId == -1) {
            return Possible.absent();
        } else if (lastMessageId == 0) {
            return Possible.of(Optional.empty());
        } else {
            return Possible.of(Optional.of("" + lastMessageId));
        }
    }

    @Override
    public Possible<Integer> bitrate() {
        return bitrate;
    }

    @Override
    public Possible<Integer> userLimit() {
        return userLimit;
    }

    @Override
    public Possible<Integer> rateLimitPerUser() {
        return rateLimitPerUser;
    }

    @Override
    public Possible<List<UserData>> recipients() {
        return recipients;
    }

    @Override
    public Possible<Optional<String>> icon() {
        return icon;
    }

    @Override
    public Possible<String> ownerId() {
        return ownerId;
    }

    @Override
    public Possible<String> applicationId() {
        return applicationId;
    }

    @Override
    public Possible<Optional<String>> parentId() {
        return parentId;
    }

    @Override
    public Possible<Optional<String>> lastPinTimestamp() {
        return lastPinTimestamp;
    }

}
