package discord4j.core.object.guildmessagesearch;

public enum SortOrder {

    ASC,
    DESC;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
