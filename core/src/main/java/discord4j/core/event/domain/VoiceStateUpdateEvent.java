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
package discord4j.core.event.domain;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.VoiceState;
import discord4j.gateway.ShardInfo;

import java.util.Optional;

/**
 * Dispatched when a user's voice state changes.
 * <p>
 * This change can include the change of any property in {@link discord4j.core.object.VoiceState VoiceState}.
 * <p>
 * The old voice state may not be present if voice states are not stored.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#voice-state-update">Voice State Update</a>
 */
public class VoiceStateUpdateEvent extends Event {

    private final VoiceState current;
    private final VoiceState old;

    public VoiceStateUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, VoiceState current, @Nullable VoiceState old) {
        super(gateway, shardInfo);
        this.current = current;
        this.old = old;
    }

    /**
     * Gets the current, new, {@link VoiceState} that has been updated in this event.
     *
     * @return The current {@link VoiceState}.
     */
    public VoiceState getCurrent() {
        return current;
    }

    /**
     * Gets the old {@link VoiceState} that has been updated in this event, if present.
     * This may not be available if {@code VoiceStates} are not stored.
     *
     * @return The old {@link VoiceState}, if present.
     */
    public Optional<VoiceState> getOld() {
        return Optional.ofNullable(old);
    }

    /**
     * Gets whether this event is a voice channel join event.
     *
     * @return {@code true} if this is a voice channel join event, {@code false} otherwise.
     */
    public boolean isJoinEvent() {
        return current.getChannelId().isPresent() && old == null;
    }

    /**
     * Gets whether this event is a voice channel leave event.
     *
     * @return {@code true} if this is a voice channel leave event, {@code false} otherwise.
     */
    public boolean isLeaveEvent() {
        return !current.getChannelId().isPresent() && old != null;
    }

    /**
     * Gets whether this event is a voice channel move event.
     *
     * @return {@code true} if this is a voice channel move event, {@code false} otherwise.
     */
    public boolean isMoveEvent() {
        if(old == null) {
            return false;
        }
        return !current.getChannelId().flatMap(current -> old.getChannelId().map(current::equals)).orElse(true);
    }

    @Override
    public String toString() {
        return "VoiceStateUpdateEvent{" +
                "current=" + current +
                ", old=" + old +
                '}';
    }
}
