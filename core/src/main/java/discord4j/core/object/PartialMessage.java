/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.component.MessageComponent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.PartialMember;
import discord4j.core.object.entity.PartialSticker;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.PartialMessageData;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represent a partial message where just a few elements from a {@link Message} are present.
 *
 * @see <a href="https://discord.com/developers/docs/resources/message#message-object">
 * Message Object</a>
 * @see MessageSnapshot
 */
public class PartialMessage implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final PartialMessageData data;

    public PartialMessage(GatewayDiscordClient gateway, PartialMessageData data) {
        this.gateway = gateway;
        this.data = data;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    public PartialMessageData getData() {
        return this.data;
    }

    /**
     * Gets the type of message.
     *
     * @return The type of message.
     */
    public Optional<Message.Type> getType() {
        return data.type().toOptional().map(Message.Type::of);
    }

    /**
     * Returns the flags of this {@link PartialMessage} if are present, describing its features.
     *
     * @return A {@code EnumSet} with the flags of this message.
     */
    public EnumSet<Message.Flag> getFlags() {
        return data.flags().toOptional()
            .map(Message.Flag::of)
            .orElse(EnumSet.noneOf(Message.Flag.class));
    }

    /**
     * Gets the ID of the guild this message is associated to, if this {@code PartialMessage} was built from Gateway
     * data,
     * like an incoming event. If requested from REST API, this field will be empty.
     *
     * @return The ID of the guild this message is associated to, if present.
     */
    public Optional<Snowflake> getGuildId() {
        return data.guildId().toOptional().map(Snowflake::of);
    }

    /**
     * Gets the author of this message, if present.
     *
     * @return The author of this message, if present.
     */
    public Optional<User> getAuthor() {
        return (!data.author().isAbsent() && (data.webhookId().isAbsent() || !data.interaction().isAbsent())) ?
            Optional.of(new User(gateway, data.author().get())) : Optional.empty();
    }

    /**
     * Gets the partial members specifically mentioned in this message, without duplication and with the same order
     * as in the message.
     *
     * @return The partial members specifically mentioned in this message, without duplication and with the same order
     * as in the message.
     */
    public List<PartialMember> getMemberMentions() {
        if (data.guildId().isAbsent()) {
            return Collections.emptyList();
        }
        long guildId = data.guildId().get().asLong();
        return data.mentions().stream()
            .map(data -> new PartialMember(gateway, data, data.member().get(), guildId))
            .collect(Collectors.toList());
    }

    /**
     * Gets the IDs of the roles specifically mentioned in this message, without duplication and with the same order
     * as in the message.
     *
     * @return The IDs of the roles specifically mentioned in this message, without duplication and with the same order
     * as in the message.
     */
    public List<Snowflake> getRoleMentionIds() {
        return data.mentionRoles().stream()
            .map(Snowflake::of)
            .collect(Collectors.toList());
    }

    /**
     * Gets the contents of the message, if present.
     *
     * @return The contents of the message, if present.
     */
    public Optional<String> getContent() {
        return data.content().toOptional();
    }

    /**
     * Gets any embedded content.
     *
     * @return Any embedded content.
     */
    public List<Embed> getEmbeds() {
        return data.embeds().stream()
            .map(data1 -> new Embed(gateway, data1))
            .collect(Collectors.toList());
    }

    /**
     * Gets any attached files, with the same order as in the message.
     *
     * @return Any attached files, with the same order as in the message.
     */
    public List<Attachment> getAttachments() {
        return data.attachments().stream()
            .map(data -> new Attachment(gateway, data))
            .collect(Collectors.toList());
    }

    /**
     * Gets the partial stickers sent with the message.
     *
     * @return The partial stickers sent with the message.
     */
    @Experimental
    public List<PartialSticker> getStickersItems() {
        return data.stickerItems().toOptional()
            .map(partialStickers -> partialStickers.stream()
                .map(data -> new PartialSticker(gateway, data))
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    /**
     * Gets the components on the message.
     *
     * @return The components on the message.
     */
    public List<LayoutComponent> getComponents() {
        return data.components().toOptional()
            .map(componentList -> componentList.stream()
                .map(MessageComponent::fromData)
                // top level message components should only be LayoutComponents
                .filter(component -> component instanceof LayoutComponent)
                .map(component -> (LayoutComponent) component)
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    /**
     * Gets when this message was sent.
     *
     * @return When this message was sent.
     */
    public Optional<Instant> getTimestamp() {
        return data.timestamp().toOptional()
            .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    /**
     * Gets when this message was edited, if present.
     *
     * @return When this message was edited, if present.
     */
    public Optional<Instant> getEditedTimestamp() {
        return data.editedTimestamp()
            .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }


}
