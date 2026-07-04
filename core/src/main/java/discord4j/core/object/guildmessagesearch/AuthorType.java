package discord4j.core.object.guildmessagesearch;

import discord4j.core.object.entity.Guild;

/**
 * Represents the type of author of a message, used to search for messages.
 *
 * @see Guild#searchMessages()
 */
public enum AuthorType {

    USER,
    BOT,
    WEBHOOK;

    public String getValue() {
        return name().toLowerCase();
    }

}
