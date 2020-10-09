package discord4j.common.store.util;

import discord4j.discordjson.json.PresenceData;
import discord4j.discordjson.json.UserData;
import reactor.util.annotation.Nullable;

import java.util.Optional;

public class PresenceAndUserData {

    private final PresenceData presenceData;
    private final UserData userData;

    private PresenceAndUserData(PresenceData presenceData, UserData userData) {
        this.presenceData = presenceData;
        this.userData = userData;
    }

    public static PresenceAndUserData of(@Nullable PresenceData presenceData, @Nullable UserData userData) {
        return new PresenceAndUserData(presenceData, userData);
    }

    public Optional<PresenceData> getPresenceData() {
        return Optional.ofNullable(presenceData);
    }

    public Optional<UserData> getUserData() {
        return Optional.ofNullable(userData);
    }
}
