package discord4j.core.object.guildmessagesearch;

import discord4j.core.object.entity.Guild;

/**
 * Represents a type of embed in a message, used to search for messages.
 *
 * @see Guild#searchMessages()
 */
public enum SearchEmbedType {

    IMAGE,
    VIDEO,
    GIF,
    SOUND,
    ARTICLE;

    public String getValue() {
        return name().toLowerCase();
    }

}
