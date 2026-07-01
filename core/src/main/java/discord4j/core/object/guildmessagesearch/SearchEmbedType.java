package discord4j.core.object.guildmessagesearch;

public enum SearchEmbedType {

    IMAGE,
    VIDEO,
    GIF,
    SOUND,
    ARTICLE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
