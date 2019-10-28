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
package discord4j.core.object.data.stored;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import discord4j.gateway.json.dispatch.GuildCreate;
import discord4j.gateway.json.dispatch.PresenceUpdate;
import discord4j.gateway.json.response.GameResponse;
import discord4j.gateway.json.response.PresenceResponse;
import reactor.util.annotation.Nullable;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public final class PresenceBean implements Serializable {

    public static final PresenceBean DEFAULT_OFFLINE = new PresenceBean(null, "offline");
    private static final long serialVersionUID = -2046485730083712716L;

    @Nullable
    private ActivityBean activity;
    @Nullable
    private String status;

    public PresenceBean(final PresenceResponse response) {
        final GameResponse game = response.getGame();
        if (game == null) {
            activity = null;
        } else if (game.getTimestamps() != null || game.getSessionId() != null || game.getApplicationId() != null ||
            game.getDetails() != null || game.getSyncId() != null || game.getState() != null ||
            game.getParty() != null || game.getAssets() != null) {
            activity = new RichActivityBean(game);
        } else {
            activity = new ActivityBean(game);
        }
        status = response.getStatus();
    }

    public PresenceBean(final PresenceUpdate update) {
        final GameResponse game = update.getGame();

        if (game == null) {
            activity = null;
        } else if (game.getTimestamps() != null || game.getSessionId() != null || game.getApplicationId() != null ||
                game.getDetails() != null || game.getSyncId() != null || game.getState() != null ||
                game.getParty() != null || game.getAssets() != null) {
            activity = new RichActivityBean(game);
        } else {
            activity = new ActivityBean(game);
        }
        status = update.getStatus();
    }

    public PresenceBean(final GuildCreate.Presence presence) {
        final GameResponse game = presence.getGame();

        if (game == null) {
            activity = null;
        } else if (game.getTimestamps() != null || game.getSessionId() != null || game.getApplicationId() != null ||
                game.getDetails() != null || game.getSyncId() != null || game.getState() != null ||
                game.getParty() != null || game.getAssets() != null) {
            activity = new RichActivityBean(game);
        } else {
            activity = new ActivityBean(game);
        }
        status = presence.getStatus();
    }

    private PresenceBean(@Nullable final ActivityBean activity, final String status) {
        this.activity = activity;
        this.status = status;
    }

    public PresenceBean() {}

    @Nullable
    public ActivityBean getActivity() {
        return activity;
    }

    public void setActivity(@Nullable final ActivityBean activity) {
        this.activity = activity;
    }

    @Nullable
    public String getStatus() {
        return status;
    }

    public void setStatus(@Nullable final String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "PresenceBean{" +
                "activity=" + activity +
                ", status='" + status + '\'' +
                '}';
    }
}
