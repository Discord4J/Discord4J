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
package discord4j.core.object.presence;

import discord4j.discordjson.json.ActivityUpdateRequest;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Activity data that can be sent to Discord.
 * <p>
 * This is as opposed to {@link Activity} which is <i>received from</i> Discord.
 * <p>
 * An activity is combined with a {@link Status} to create a {@link ClientPresence}.
 */
public class ClientActivity {

    /**
     * Creates a {@link Activity.Type#PLAYING playing} activity.
     *
     * @param name The name of the activity.
     * @return A playing activity with the given name.
     */
    public static ClientActivity playing(String name) {
        return of(Activity.Type.PLAYING, name, null);
    }

    /**
     * Creates a {@link Activity.Type#STREAMING streaming} activity.
     *
     * @param name The name of the activity.
     * @param url The stream url.
     * @return A streaming activity with the given name and url.
     */
    public static ClientActivity streaming(String name, String url) {
        return of(Activity.Type.STREAMING, name, url);
    }

    /**
     * Creates a {@link Activity.Type#LISTENING listening} activity.
     *
     * @param name The name of the activity.
     * @return A listening activity with the given name.
     */
    public static ClientActivity listening(String name) {
        return of(Activity.Type.LISTENING, name, null);
    }

    /**
     * Creates a {@link Activity.Type#WATCHING watching} activity.
     *
     * @param name The name of the activity.
     * @return A watching activity with the given name.
     */
    public static ClientActivity watching(String name) {
        return of(Activity.Type.WATCHING, name, null);
    }

    /**
     * Creates a {@link Activity.Type#COMPETING competing} activity.
     *
     * @param name The name of the activity.
     * @return A competing activity with the given name.
     */
    public static ClientActivity competing(String name) {
        return of(Activity.Type.COMPETING, name, null);
    }

    /**
     * Creates an activity with the given type, name, and url.
     *
     * @param type The type of the activity.
     * @param name The name of the activity.
     * @param url The url of the activity (only valid for {@link Activity.Type#STREAMING streaming} activities).
     * @return An activity with the given type, name, and url.
     */
    public static ClientActivity of(Activity.Type type, String name, @Nullable String url) {
        return new ClientActivity(ActivityUpdateRequest.builder()
                .type(type.getValue())
                .name(name)
                .url(Optional.ofNullable(url))
                .build());
    }

    private final ActivityUpdateRequest activityUpdateRequest;

    private ClientActivity(ActivityUpdateRequest activityUpdateRequest) {
        this.activityUpdateRequest = activityUpdateRequest;
    }

    /**
     * Converts this activity's data to an object for use by the gateway.
     *
     * @return An equivalent {@code ActivityUpdateRequest} for this activity.
     */
    public ActivityUpdateRequest getActivityUpdateRequest() {
        return activityUpdateRequest;
    }
}
