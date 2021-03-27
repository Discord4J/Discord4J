package discord4j.core.object.clientpresence;

import discord4j.core.object.presence.Status;
import discord4j.discordjson.json.gateway.StatusUpdate;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

public class ClientPresence {

    public static ClientPresence online() {
        return new ClientPresence(StatusUpdate.builder()
                .status(Status.ONLINE.getValue())
                .activities(Optional.empty())
                .afk(false)
                .since(Optional.empty())
                .build());
    }

    public static ClientPresence online(ClientActivity activity) {
        return new ClientPresence(StatusUpdate.builder()
                .status(Status.ONLINE.getValue())
                .activities(Collections.singletonList(activity.getActivityUpdateRequest()))
                .afk(false)
                .since(Optional.empty())
                .build());
    }

    public static ClientPresence doNotDisturb() {
        return new ClientPresence(StatusUpdate.builder()
                .status(Status.DO_NOT_DISTURB.getValue())
                .activities(Optional.empty())
                .afk(false)
                .since(Optional.empty())
                .build());
    }

    public static ClientPresence doNotDisturb(ClientActivity activity) {
        return new ClientPresence(StatusUpdate.builder()
                .status(Status.DO_NOT_DISTURB.getValue())
                .activities(Collections.singletonList(activity.getActivityUpdateRequest()))
                .afk(false)
                .since(Optional.empty())
                .build());
    }

    public static ClientPresence idle() {
        return new ClientPresence(StatusUpdate.builder()
                .status(Status.IDLE.getValue())
                .activities(Optional.empty())
                .afk(true)
                .since(Instant.now().toEpochMilli())
                .build());
    }

    public static ClientPresence idle(ClientActivity activity) {
        return new ClientPresence(StatusUpdate.builder()
                .status(Status.IDLE.getValue())
                .activities(Collections.singletonList(activity.getActivityUpdateRequest()))
                .afk(true)
                .since(Instant.now().toEpochMilli())
                .build());
    }

    public static ClientPresence invisible() {
        return new ClientPresence(StatusUpdate.builder()
                .status(Status.INVISIBLE.getValue())
                .activities(Optional.empty())
                .afk(false)
                .since(Optional.empty())
                .build());
    }

    private final StatusUpdate statusUpdate;

    private ClientPresence(StatusUpdate statusUpdate) {
        this.statusUpdate = statusUpdate;
    }

    public StatusUpdate getStatusUpdate() {
        return statusUpdate;
    }
}
