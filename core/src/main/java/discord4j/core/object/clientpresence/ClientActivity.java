package discord4j.core.object.clientpresence;

import discord4j.core.object.presence.Activity;
import discord4j.discordjson.json.ActivityUpdateRequest;

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
