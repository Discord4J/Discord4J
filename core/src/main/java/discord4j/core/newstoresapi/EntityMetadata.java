package discord4j.core.newstoresapi;

import discord4j.common.util.Snowflake;

import java.util.Arrays;
import java.util.List;

import static discord4j.core.newstoresapi.DataIdentifier.id;
import static discord4j.core.newstoresapi.DataIdentifier.idWithParent;
import static discord4j.core.newstoresapi.EntityType.*;

public class EntityMetadata {

    private final DataIdentifier identifier;
    private final List<String> tags;

    private EntityMetadata(DataIdentifier identifier, String[] tags) {
        this.identifier = identifier;
        this.tags = Arrays.asList(tags);
    }

    public DataIdentifier getIdentifier() {
        return identifier;
    }

    public List<String> getTags() {
        return tags;
    }

    public static EntityMetadata channel(Snowflake channelId, String... tags) {
        return new EntityMetadata(id(CHANNEL, channelId), tags);
    }

    public static EntityMetadata guild(Snowflake guildId, String... tags) {
        return new EntityMetadata(id(GUILD, guildId), tags);
    }

    public static EntityMetadata emoji(Snowflake emojiId, String... tags) {
        return new EntityMetadata(id(EMOJI, emojiId), tags);
    }

    public static EntityMetadata member(Snowflake guildId, Snowflake memberId, String... tags) {
        return new EntityMetadata(idWithParent(id(GUILD, guildId), MEMBER, memberId), tags);
    }

    public static EntityMetadata message(Snowflake channelId, Snowflake messageId, String... tags) {
        return new EntityMetadata(idWithParent(id(CHANNEL, channelId), MESSAGE, messageId), tags);
    }

    public static EntityMetadata presence(Snowflake guildId, Snowflake userId, String... tags) {
        return new EntityMetadata(idWithParent(id(GUILD, guildId), PRESENCE, userId), tags);
    }

    public static EntityMetadata role(Snowflake roleId, String... tags) {
        return new EntityMetadata(id(ROLE, roleId), tags);
    }

    public static EntityMetadata user(Snowflake userId, String... tags) {
        return new EntityMetadata(id(USER, userId), tags);
    }

    public static EntityMetadata voiceState(Snowflake channelId, Snowflake userId, String... tags) {
        return new EntityMetadata(idWithParent(id(CHANNEL, channelId), VOICE_STATE, userId), tags);
    }
}
