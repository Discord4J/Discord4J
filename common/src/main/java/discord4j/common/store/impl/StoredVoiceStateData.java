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

import discord4j.discordjson.json.VoiceStateData;
import discord4j.discordjson.possible.Possible;
import reactor.util.annotation.Nullable;

import java.util.Optional;

import static discord4j.common.store.impl.ImplUtils.*;

class StoredVoiceStateData {
    private final long guildId_value;
    private final boolean guildId_absent;
    private final long channelId;
    private final boolean channelId_null;
    private final long userId;
    private final String sessionId;
    private final boolean deaf;
    private final boolean mute;
    private final boolean selfDeaf;
    private final boolean selfMute;
    private final boolean selfStream_value;
    private final boolean selfStream_absent;
    private final boolean selfVideo;
    private final boolean suppress;

    StoredVoiceStateData(VoiceStateData original) {
        this.guildId_value = idFromPossibleString(original.guildId()).orElse(-1L);
        this.guildId_absent = original.guildId().isAbsent();
        this.channelId = original.channelId().map(ImplUtils::toLongId).orElse(-1L);
        this.channelId_null = !original.channelId().map(ImplUtils::toLongId).isPresent();
        this.userId = toLongId(original.userId());
        this.sessionId = original.sessionId();
        this.deaf = original.deaf();
        this.mute = original.mute();
        this.selfDeaf = original.selfDeaf();
        this.selfMute = original.selfMute();
        this.selfStream_value = original.selfStream().toOptional().orElse(false);
        this.selfStream_absent = original.selfStream().isAbsent();
        this.selfVideo = original.selfVideo();
        this.suppress = original.suppress();
    }

    long guildId() {
        return guildId_value;
    }

    long userId() {
        return userId;
    }

    VoiceStateData toImmutable(@Nullable StoredMemberData member) {
        return VoiceStateData.builder()
                .guildId(toPossibleStringId(guildId_value, guildId_absent))
                .channelId(Optional.ofNullable(channelId_null ? null : channelId).map(String::valueOf))
                .userId("" + userId)
                .member(member == null ? Possible.absent() : Possible.of(member.toImmutable()))
                .sessionId(sessionId)
                .deaf(deaf)
                .mute(mute)
                .selfDeaf(selfDeaf)
                .selfMute(selfMute)
                .selfStream(toPossible(selfStream_value, selfStream_absent))
                .selfVideo(selfVideo)
                .suppress(suppress)
                .build();
    }
}
