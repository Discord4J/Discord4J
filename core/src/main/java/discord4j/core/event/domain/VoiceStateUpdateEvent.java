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

import discord4j.core.DiscordClient;
import discord4j.core.object.VoiceState;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when a user's voice state changes.
 * <p>
 * This change can include the change of any property in {@link discord4j.core.object.VoiceState VoiceState}.
 * <p>
 * The old voice state may not be present if voice states are not stored.
 * <p>
 * This event is dispatched by Discord
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#voice-state-update">Voice State Update</a>
 */
public class VoiceStateUpdateEvent extends Event {

    private final VoiceState current;
    private final VoiceState old;

    public VoiceStateUpdateEvent(DiscordClient client, VoiceState current, @Nullable VoiceState old) {
        super(client);
        this.current = current;
        this.old = old;
    }

    /**
     * Gets the current, new, VoiceState that has been updated in this event.
     * @return The current VoiceState.
     */
    public VoiceState getCurrent() {
        return current;
    }

    /**
     * Gets the old VoiceState that has been updated in this event. This may not be available if VoiceStates are not stored.
     * @return The old VoiceState.
     */
    public Optional<VoiceState> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public String toString() {
        return "VoiceStateUpdateEvent{" +
                "current=" + current +
                ", old=" + old +
                '}';
    }
}
