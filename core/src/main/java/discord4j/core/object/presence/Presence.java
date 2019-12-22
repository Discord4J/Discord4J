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
package discord4j.core.object.presence;

import discord4j.common.jackson.Possible;
import discord4j.core.object.data.stored.ActivityBean;
import discord4j.core.object.data.stored.PresenceBean;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * A Discord presence.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#presence">Presence</a>
 */
public final class Presence {

    public static Presence online() {
        return new Presence(Status.ONLINE, null);
    }

    public static Presence online(Activity activity) {
        return new Presence(Status.ONLINE, activity);
    }

    public static Presence doNotDisturb() {
        return new Presence(Status.DO_NOT_DISTURB, null);
    }

    public static Presence doNotDisturb(Activity activity) {
        return new Presence(Status.DO_NOT_DISTURB, activity);
    }

    public static Presence idle() {
        return new Presence(Status.IDLE, null);
    }

    public static Presence idle(Activity activity) {
        return new Presence(Status.IDLE, activity);
    }

    public static Presence invisible() {
        return new Presence(Status.INVISIBLE, null);
    }

    private final PresenceBean data;

    public Presence(final PresenceBean data) {
        this.data = data;
    }

    private Presence(final Status status, @Nullable final Activity activity) {
        ActivityBean activityBean = activity == null
            ? null
            : new ActivityBean(activity.getType().getValue(), activity.getName(), activity.getStreamingUrl().orElse(null));
        this.data = new PresenceBean(activityBean, status.getValue(), null, null, null);
    }

    public Status getStatus() {
        return Status.of(data.getStatus());
    }

    public Optional<Status> getStatus(Status.Platform platform) {
        switch (platform) {
            case DESKTOP: return Optional.ofNullable(data.getDesktopStatus()).map(Status::of);
            case MOBILE: return Optional.ofNullable(data.getMobileStatus()).map(Status::of);
            case WEB: return Optional.ofNullable(data.getWebStatus()).map(Status::of);
            default: throw new AssertionError();
        }
    }

    public Optional<Activity> getActivity() {
        return Optional.ofNullable(data.getActivity()).map(Activity::new);
    }

    public StatusUpdate asStatusUpdate() {
        final StatusUpdate.Game game = getActivity()
                .map(activity -> {
                    Possible<String> url = activity.getStreamingUrl().map(Possible::of).orElse(Possible.absent());
                    return new StatusUpdate.Game(activity.getName(), activity.getType().getValue(), url);
                })
                .orElse(null);

        return new StatusUpdate(game, data.getStatus());
    }

    @Override
    public String toString() {
        return "Presence{" +
            "data=" + data +
            '}';
    }
}
