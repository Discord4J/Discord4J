package discord4j.core.object.guildmessagesearch;

import discord4j.core.object.entity.Guild;

/**
 * Represents a type of sorting, used to search for messages.
 *
 * @see Guild#searchMessages()
 */
public enum SortBy {

    TIMESTAMP,
    RELEVANCE;

    public String getValue() {
        return name().toLowerCase();
    }

}
