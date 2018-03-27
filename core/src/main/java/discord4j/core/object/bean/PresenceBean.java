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
package discord4j.core.object.bean;

import discord4j.common.json.response.GameResponse;
import discord4j.common.json.response.PresenceResponse;

import javax.annotation.Nullable;
import java.io.Serializable;

public final class PresenceBean implements Serializable {

    private static final long serialVersionUID = -643257747475306026L;

    private long userId;
    @Nullable
    private ActivityBean activity;
    @Nullable
    private Long guildId;
    @Nullable
    private String status;

    public PresenceBean(final PresenceResponse response) {
        userId = response.getUser().getId();
        final GameResponse game = response.getGame();
        activity = (game == null) ? null : new ActivityBean(game);
        guildId = response.getGuildId();
        status = response.getStatus();
    }

    public PresenceBean() {}

    public long getUserId() {
        return userId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    @Nullable
    public ActivityBean getActivity() {
        return activity;
    }

    public void setActivity(@Nullable final ActivityBean activity) {
        this.activity = activity;
    }

    @Nullable
    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(@Nullable final Long guildId) {
        this.guildId = guildId;
    }

    @Nullable
    public String getStatus() {
        return status;
    }

    public void setStatus(@Nullable final String status) {
        this.status = status;
    }
}
