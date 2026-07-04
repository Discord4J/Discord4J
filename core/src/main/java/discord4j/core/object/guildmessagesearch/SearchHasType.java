package discord4j.core.object.guildmessagesearch;

import discord4j.core.object.entity.Guild;

/**
 * Represents a type of content in a message, used to search for messages.
 *
 * @see Guild#searchMessages()
 */
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

    public String getValue() {
        return name().toLowerCase();
    }

}
