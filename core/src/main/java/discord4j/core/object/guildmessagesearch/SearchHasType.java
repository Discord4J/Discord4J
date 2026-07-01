package discord4j.core.object.guildmessagesearch;

public enum SearchHasType {

    IMAGE,
    SOUND,
    VIDEO,
    FILE,
    STICKER,
    EMBED,
    LINK,
    POLL,
    SNAPSHOT;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
