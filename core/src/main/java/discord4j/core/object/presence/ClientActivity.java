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
import org.jspecify.annotations.Nullable;

import java.util.Objects;
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
     * @param name the name of the activity
     * @return a playing activity with the given name
     */
    public static ClientActivity playing(String name) {
        return of(Activity.Type.PLAYING, name, null);
    }

    /**
     * Creates a {@link Activity.Type#STREAMING streaming} activity.
     *
     * @param name the name of the activity
     * @param url the stream url
     * @return a streaming activity with the given name and url
     */
    public static ClientActivity streaming(String name, String url) {
        return of(Activity.Type.STREAMING, name, url);
    }

    /**
     * Creates a {@link Activity.Type#LISTENING listening} activity.
     *
     * @param name the name of the activity
     * @return a listening activity with the given name
     */
    public static ClientActivity listening(String name) {
        return of(Activity.Type.LISTENING, name, null);
    }

    /**
     * Creates a {@link Activity.Type#WATCHING watching} activity.
     *
     * @param name the name of the activity
     * @return a watching activity with the given name
     */
    public static ClientActivity watching(String name) {
        return of(Activity.Type.WATCHING, name, null);
    }

    /**
     * Creates a {@link Activity.Type#COMPETING competing} activity.
     *
     * @param name the name of the activity
     * @return a competing activity with the given name
     */
    public static ClientActivity competing(String name) {
        return of(Activity.Type.COMPETING, name, null);
    }

    /**
     * Creates a {@link Activity.Type#CUSTOM custom} activity.
     *
     * @param name the custom status used in this activity
     * @return a custom activity with the given "state" value
     */
    public static ClientActivity custom(String name) {
        return of(Activity.Type.CUSTOM, "Custom Status", null, Objects.requireNonNull(name));
    }

    /**
     * Creates an activity with the given type, name, and url.
     *
     * @param type the type of the activity
     * @param name the name of the activity
     * @param url the url of the activity (only valid for {@link Activity.Type#STREAMING streaming} activities)
     * @return an activity with the given type, name, and url
     */
    public static ClientActivity of(Activity.Type type, String name, @Nullable String url) {
        return of(type, name, url, null);
    }

    /**
     * Creates an activity with the given type, name, url and state.
     *
     * @param type the type of the activity
     * @param name the name of the activity
     * @param url the url of the activity if the type is {@link Activity.Type#STREAMING STREAMING}
     * @param state the status if the type is {@link Activity.Type#CUSTOM CUSTOM}, or shown as additional data under an
     * activity's name for other types
     * @return an activity with the given type, name, url and state
     */
    public static ClientActivity of(Activity.Type type, String name, @Nullable String url, @Nullable String state) {
        return new ClientActivity(ActivityUpdateRequest.builder()
                .type(type.getValue())
                .name(name)
                .url(Optional.ofNullable(url))
                .state(Optional.ofNullable(state))
                .build());
    }

    private final ActivityUpdateRequest activityUpdateRequest;

    private ClientActivity(ActivityUpdateRequest activityUpdateRequest) {
        this.activityUpdateRequest = activityUpdateRequest;
    }

    /**
     * Create a new {@link ClientActivity} from this one by including the given "state" field value. For activity types
     * other than {@link Activity.Type#CUSTOM}, it will be shown as additional data under an activity's name.
     *
     * @param state the custom status or additional data to include under an activity
     * @return a new client activity based on this one with the given state value
     */
    public ClientActivity withState(String state) {
        return new ClientActivity(ActivityUpdateRequest.builder()
                .from(activityUpdateRequest)
                .state(state)
                .build());
    }

    /**
     * Converts this activity's data to an object for use by the gateway.
     *
     * @return an equivalent {@code ActivityUpdateRequest} for this activity
     */
    public ActivityUpdateRequest getActivityUpdateRequest() {
        return activityUpdateRequest;
    }
}
