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

import static discord4j.common.store.impl.ImplUtils.*;

/**
 * Channel data with snowflakes stored as long, and with mutable lastMessageId.
 */
class StoredChannelData {

    private final long id;
    private final int type;
    private final long guildId_value;
    private final boolean guildId_absent;
    private final int position_value;
    private final boolean position_absent;
    private final List<OverwriteData> permissionOverwrites_value;
    private final boolean permissionOverwrites_absent;
    private final String name_value;
    private final boolean name_absent;
    private final String topic_value;
    private final boolean topic_absent;
    private final boolean nsfw_value;
    private final boolean nsfw_absent;
    private final int bitrate_value;
    private final boolean bitrate_absent;
    private final int userLimit_value;
    private final boolean userLimit_absent;
    private final int rateLimitPerUser_value;
    private final boolean rateLimitPerUser_absent;
    private final List<UserData> recipients_value;
    private final boolean recipients_absent;
    private final String icon_value;
    private final boolean icon_absent;
    private final long ownerId_value;
    private final boolean ownerId_absent;
    private final long applicationId_value;
    private final boolean applicationId_absent;
    private final long parentId_value;
    private final boolean parentId_absent;
    private final String lastPinTimestamp_value;
    private final boolean lastPinTimestamp_absent;
    private volatile long lastMessageId; // -1 = absent, 0 = null
    
    StoredChannelData(ChannelData original) {
        this.id = toLongId(original.id());
        this.type = original.type();
        this.guildId_value = idFromPossibleString(original.guildId()).orElse(-1L);
        this.guildId_absent = original.guildId().isAbsent();
        this.position_value = original.position().toOptional().orElse(-1);
        this.position_absent = original.position().isAbsent();
        this.permissionOverwrites_value = original.permissionOverwrites().toOptional().orElse(null);
        this.permissionOverwrites_absent = original.permissionOverwrites().isAbsent();
        this.name_value = original.name().toOptional().orElse(null);
        this.name_absent = original.name().isAbsent();
        this.topic_value = Possible.flatOpt(original.topic()).orElse(null);
        this.topic_absent = original.topic().isAbsent();
        this.nsfw_value = original.nsfw().toOptional().orElse(false);
        this.nsfw_absent = original.nsfw().isAbsent();
        this.bitrate_value = original.bitrate().toOptional().orElse(-1);
        this.bitrate_absent = original.bitrate().isAbsent();
        this.userLimit_value = original.userLimit().toOptional().orElse(-1);
        this.userLimit_absent = original.userLimit().isAbsent();
        this.rateLimitPerUser_value = original.rateLimitPerUser().toOptional().orElse(-1);
        this.rateLimitPerUser_absent = original.rateLimitPerUser().isAbsent();
        this.recipients_value = original.recipients().toOptional().orElse(null);
        this.recipients_absent = original.recipients().isAbsent();
        this.icon_value = Possible.flatOpt(original.icon()).orElse(null);
        this.icon_absent = original.icon().isAbsent();
        this.ownerId_value = idFromPossibleString(original.ownerId()).orElse(-1L);
        this.ownerId_absent = original.ownerId().isAbsent();
        this.applicationId_value = idFromPossibleString(original.applicationId()).orElse(-1L);
        this.applicationId_absent = original.applicationId().isAbsent();
        this.parentId_value = idFromPossibleOptionalString(original.parentId()).orElse(-1L);
        this.parentId_absent = original.parentId().isAbsent();
        this.lastPinTimestamp_value = Possible.flatOpt(original.lastPinTimestamp()).orElse(null);
        this.lastPinTimestamp_absent = original.lastPinTimestamp().isAbsent();
        this.lastMessageId = original.lastMessageId().isAbsent() ? -1L :
                original.lastMessageId().get().map(ImplUtils::toLongId).orElse(0L);
    }

    void setLastMessageId(long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    ChannelData toImmutable() {
        long lastMessageId = this.lastMessageId;
        Possible<Optional<String>> immutableLastMessageId;
        if (lastMessageId == -1) {
            immutableLastMessageId = Possible.absent();
        } else if (lastMessageId == 0) {
            immutableLastMessageId = Possible.of(Optional.empty());
        } else {
            immutableLastMessageId = Possible.of(Optional.of("" + lastMessageId));
        }
        return ChannelData.builder()
                .id("" + id)
                .type(type)
                .guildId(toPossibleStringId(guildId_value, guildId_absent))
                .position(toPossible(position_value, position_absent))
                .permissionOverwrites(toPossible(permissionOverwrites_value, permissionOverwrites_absent))
                .name(toPossible(name_value, name_absent))
                .topic(toPossibleOptional(topic_value, topic_absent))
                .nsfw(toPossible(nsfw_value, nsfw_absent))
                .lastMessageId(immutableLastMessageId)
                .bitrate(toPossible(bitrate_value, bitrate_absent))
                .userLimit(toPossible(userLimit_value, userLimit_absent))
                .rateLimitPerUser(toPossible(rateLimitPerUser_value, rateLimitPerUser_absent))
                .recipients(toPossible(recipients_value, recipients_absent))
                .icon(toPossibleOptional(icon_value, icon_absent))
                .ownerId(toPossibleStringId(ownerId_value, ownerId_absent))
                .applicationId(toPossibleStringId(applicationId_value, applicationId_absent))
                .parentId(toPossibleOptionalStringId(parentId_value, parentId_absent))
                .lastPinTimestamp(toPossibleOptional(lastPinTimestamp_value, lastPinTimestamp_absent))
                .build();
    }

}
