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
package discord4j.core.event.domain.channel;

import discord4j.core.DiscordClient;
import discord4j.core.event.Update;
import discord4j.core.object.Snowflake;
import discord4j.core.object.entity.PermissionOverwrite;
import discord4j.core.object.entity.VoiceChannel;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

public class VoiceChannelUpdateEvent extends ChannelEvent {

    private final VoiceChannel current;
    private final VoiceChannel old;

    public VoiceChannelUpdateEvent(DiscordClient client, VoiceChannel current, @Nullable VoiceChannel old) {
        super(client);
        this.current = current;
        this.old = old;
    }

    public VoiceChannel getCurrent() {
        return current;
    }

    public Optional<VoiceChannel> getOld() {
        return Optional.ofNullable(old);
    }
}
