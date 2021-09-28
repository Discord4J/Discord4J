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
package discord4j.core.spec.legacy;

import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.discordjson.json.ChannelModifyRequest;
import discord4j.discordjson.json.ImmutableChannelModifyRequest;
import discord4j.discordjson.json.OverwriteData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Permission;
import discord4j.common.util.Snowflake;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LegacySpec used to modify a {@link VoiceChannel} entity.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#modify-channel">Modify Channel</a>
 */
public class LegacyVoiceChannelEditSpec extends LegacyAudioChannelEditSpec {

    private final ImmutableChannelModifyRequest.Builder requestBuilder = ChannelModifyRequest.builder();

    /**
     * Sets the user limit for the modified {@link VoiceChannel}.
     * <p>
     * Users with {@link Permission#MOVE_MEMBERS} ignore this limit and can also move other users into the channel
     * past the limit.
     *
     * @param userLimit The maximum number of users that can join the voice channel at once.
     * @return This spec.
     */
    public LegacyVoiceChannelEditSpec setUserLimit(int userLimit) {
        requestBuilder.userLimit(userLimit);
        return this;
    }

    /**
     * Sets the camera video quality mode of the voice channel.
     *
     * @param videoQualityMode The camera video quality mode of the voice channel.
     * @return This spec.
     */
    public LegacyVoiceChannelEditSpec setVideoQualityMode(VoiceChannel.Mode videoQualityMode) {
        requestBuilder.videoQualityModeOrNull(videoQualityMode.getValue());
        return this;
    }

    @Override
    public ChannelModifyRequest asRequest() {
        return requestBuilder.build();
    }
}
