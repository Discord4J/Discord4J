package discord4j.core.object.guildmessagesearch;

import discord4j.core.object.entity.Guild;

/**
 * Represents an order of sorting, used to search for messages.
 *
 * @see Guild#searchMessages()
 */
public enum SortOrder {

    ASC,
    DESC;

    public String getValue() {
        return name().toLowerCase();
    }

}
