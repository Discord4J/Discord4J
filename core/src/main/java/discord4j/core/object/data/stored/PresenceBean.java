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
import discord4j.common.jackson.Possible;
import discord4j.gateway.json.dispatch.PresenceUpdate;
import reactor.util.annotation.Nullable;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public final class PresenceBean implements Serializable {

    public static final PresenceBean DEFAULT_OFFLINE = new PresenceBean(null, "offline", null, null, null);
    private static final long serialVersionUID = -2046485730083712716L;

    @Nullable
    private ActivityBean activity;
    private String status;
    @Nullable
    private String desktopStatus;
    @Nullable
    private String mobileStatus;
    @Nullable
    private String webStatus;

    public PresenceBean(final PresenceUpdate presence) {
        this(
                presence.getGame() == null ? null : new ActivityBean(presence.getGame()),
                presence.getStatus(),
                Possible.orElseNull(presence.getClientStatus(), PresenceUpdate.ClientStatus::getDesktop),
                Possible.orElseNull(presence.getClientStatus(), PresenceUpdate.ClientStatus::getMobile),
                Possible.orElseNull(presence.getClientStatus(), PresenceUpdate.ClientStatus::getWeb)
        );
    }

    public PresenceBean(@Nullable ActivityBean activity, String status, @Nullable String desktopStatus,
                        @Nullable String mobileStatus, @Nullable String webStatus) {
        this.activity = activity;
        this.status = status;
        this.desktopStatus = desktopStatus;
        this.mobileStatus = mobileStatus;
        this.webStatus = webStatus;
    }

    @Nullable
    public ActivityBean getActivity() {
        return activity;
    }

    public void setActivity(@Nullable ActivityBean activity) {
        this.activity = activity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Nullable
    public String getDesktopStatus() {
        return desktopStatus;
    }

    public void setDesktopStatus(@Nullable String desktopStatus) {
        this.desktopStatus = desktopStatus;
    }

    @Nullable
    public String getMobileStatus() {
        return mobileStatus;
    }

    public void setMobileStatus(@Nullable String mobileStatus) {
        this.mobileStatus = mobileStatus;
    }

    @Nullable
    public String getWebStatus() {
        return webStatus;
    }

    public void setWebStatus(@Nullable String webStatus) {
        this.webStatus = webStatus;
    }

    @Override
    public String toString() {
        return "PresenceBean{" +
                "activity=" + activity +
                ", status='" + status + '\'' +
                ", desktopStatus='" + desktopStatus + '\'' +
                ", mobileStatus='" + mobileStatus + '\'' +
                ", webStatus='" + webStatus + '\'' +
                '}';
    }
}
