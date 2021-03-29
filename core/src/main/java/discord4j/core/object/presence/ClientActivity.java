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

/**
 * Activity data that can be sent to Discord.
 * <p>
 * This is as opposed to {@link Activity} which is <i>received from</i> Discord.
 * <p>
 * An activity is combined with a {@link Status} to create a presence.
 */
public class ClientActivity {

    public static ClientActivity playing(String name) {
        return new ClientActivity(ActivityUpdateRequest.builder()
                .name(name)
                .type(Activity.Type.PLAYING.getValue())
                .build());
    }

    public static ClientActivity streaming(String name, String url) {
        return new ClientActivity(ActivityUpdateRequest.builder()
                .name(name)
                .type(Activity.Type.STREAMING.getValue())
                .url(url)
                .build());
    }

    public static ClientActivity listening(String name) {
        return new ClientActivity(ActivityUpdateRequest.builder()
                .name(name)
                .type(Activity.Type.LISTENING.getValue())
                .build());
    }

    public static ClientActivity watching(String name) {
        return new ClientActivity(ActivityUpdateRequest.builder()
                .name(name)
                .type(Activity.Type.WATCHING.getValue())
                .build());
    }

    public static ClientActivity competing(String name) {
        return new ClientActivity(ActivityUpdateRequest.builder()
                .name(name)
                .type(Activity.Type.COMPETING.getValue())
                .build());
    }

    private final ActivityUpdateRequest activityUpdateRequest;

    private ClientActivity(ActivityUpdateRequest activityUpdateRequest) {
        this.activityUpdateRequest = activityUpdateRequest;
    }

    public ActivityUpdateRequest getActivityUpdateRequest() {
        return activityUpdateRequest;
    }
}
