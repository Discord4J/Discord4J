package discord4j.core.object.guildmessagesearch;

public enum SortBy {

    TIMESTAMP,
    RELEVANCE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
