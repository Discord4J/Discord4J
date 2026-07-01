package discord4j.core.object.guildmessagesearch;

public enum AuthorType {

    USER,
    BOT,
    WEBHOOK;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
