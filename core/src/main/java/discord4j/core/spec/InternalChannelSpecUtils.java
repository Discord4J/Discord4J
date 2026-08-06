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
package discord4j.core.spec;

import discord4j.core.object.entity.channel.Channel;
import discord4j.discordjson.json.ImmutableChannelCreateRequest;
import discord4j.discordjson.json.ImmutableChannelModifyRequest;
import discord4j.discordjson.possible.Possible;

import java.util.EnumSet;

final class InternalChannelSpecUtils {

    private InternalChannelSpecUtils() {
        throw new AssertionError();
    }

    public static void handleContentVisibilityMode(
            final ImmutableChannelModifyRequest.Builder builder,
            final Possible<Channel.ContentVisibilityMode> contentVisibilityModePossible,
            final EnumSet<Channel.Flag> currentFlags
    ) {
        if (contentVisibilityModePossible.isAbsent()) {
            return;
        }
        final Channel.ContentVisibilityMode contentVisibilityMode = contentVisibilityModePossible.get();
        if (contentVisibilityMode == Channel.ContentVisibilityMode.SPOILER) {
            currentFlags.add(Channel.Flag.IS_SPOILER_CHANNEL);
        } else {
            currentFlags.remove(Channel.Flag.IS_SPOILER_CHANNEL);
        }
        builder.flags(Channel.Flag.toBitfield(currentFlags));
        builder.nsfw(contentVisibilityMode == Channel.ContentVisibilityMode.NSFW);
    }

    public static void handleContentVisibilityMode(
            final ImmutableChannelCreateRequest.Builder builder,
            final Possible<Channel.ContentVisibilityMode> contentVisibilityModePossible,
            final EnumSet<Channel.Flag> currentFlags
    ) {
        if (contentVisibilityModePossible.isAbsent()) {
            return;
        }
        final Channel.ContentVisibilityMode contentVisibilityMode = contentVisibilityModePossible.get();
        if (contentVisibilityMode == Channel.ContentVisibilityMode.SPOILER) {
            currentFlags.add(Channel.Flag.IS_SPOILER_CHANNEL);
        } else {
            currentFlags.remove(Channel.Flag.IS_SPOILER_CHANNEL);
        }
        builder.flags(Channel.Flag.toBitfield(currentFlags));
        builder.nsfw(contentVisibilityMode == Channel.ContentVisibilityMode.NSFW);
    }

}
