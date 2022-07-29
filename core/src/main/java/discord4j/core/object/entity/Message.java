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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.entity;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.Embed;
import discord4j.core.object.MessageInteraction;
import discord4j.core.object.MessageReference;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.component.MessageComponent;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.object.reaction.Reaction;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.MessageEditMono;
import discord4j.core.spec.MessageEditSpec;
import discord4j.core.spec.StartThreadSpec;
import discord4j.core.spec.legacy.LegacyMessageEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.SuppressEmbedsRequest;
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.entity.RestChannel;
import discord4j.rest.entity.RestMessage;
import discord4j.rest.util.MultipartRequest;
import discord4j.rest.util.PaginationUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A Discord message.
 * <p>
 * <a href="https://discord.com/developers/docs/resources/channel#message-object">Message Object</a>
 */
public final class Message implements Entity {

    /**
     * The maximum amount of characters that can be in the contents of a message.
     */
    public static final int MAX_CONTENT_LENGTH = 2000;

    /**
     * The maximum amount of characters that can be present when combining all title, description, field.name,
     * field.value, footer.text, and author.name fields of all embeds for this message.
     */
    public static final int MAX_TOTAL_EMBEDS_CHARACTER_LENGTH = 6000;

    /**
     * The gateway associated to this object.
     */
    private final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final MessageData data;

    /**
     * A handle to execute REST API operations for this entity.
     */
    private final RestMessage rest;

    /**
     * Constructs a {@code Message} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Message(final GatewayDiscordClient gateway, final MessageData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
        this.rest = RestMessage.create(gateway.getRestClient(), Snowflake.of(data.channelId()),
                Snowflake.of(data.id()));
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the data of the message.
     *
     * @return The data of the message.
     */
    public MessageData getData() {
        return data;
    }

    /**
     * @return A {@link RestMessage} handle to execute REST API operations on this entity.
     */
    public RestMessage getRestMessage() {
        return rest;
    }

    /**
     * @return A {@link RestChannel} handle to execute REST API operations on the channel of this message.
     */
    public RestChannel getRestChannel() {
        return RestChannel.create(gateway.getRestClient(), Snowflake.of(data.channelId()));
    }

    /**
     * Gets the ID of the channel the message was sent in.
     *
     * @return The ID of the channel the message was sent in.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(data.channelId());
    }

    /**
     * Requests to retrieve the channel the message was sent in.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel channel} the message
     * was sent in. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<MessageChannel> getChannel() {
        return gateway.getChannelById(getChannelId()).cast(MessageChannel.class);
    }

    /**
     * Requests to retrieve the channel the message was sent in, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the channel
     * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel channel} the message
     * was sent in. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<MessageChannel> getChannel(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getChannelById(getChannelId()).cast(MessageChannel.class);
    }

    /**
     * Gets the ID the webhook that generated this message, if present.
     *
     * @return The ID of the webhook that generated this message, if present.
     */
    public Optional<Snowflake> getWebhookId() {
        return data.webhookId().toOptional().map(Snowflake::of);
    }

    /**
     * Gets the author of this message, if present.
     *
     * @return The author of this message, if present.
     */
    public Optional<User> getAuthor() {
        return data.webhookId().isAbsent() || !data.interaction().isAbsent() ?
                Optional.of(new User(gateway, data.author())) : Optional.empty();
    }

    /**
     * Requests to retrieve the author of this message as a {@link Member member} of the guild in which it was sent.
     *
     * @return A {@link Mono} where, upon successful completion, emits the author of this message as a
     * {@link Member member} of the guild in which it was sent, if present. If an error is received, it is emitted
     * through the {@code Mono}.
     */
    public Mono<Member> getAuthorAsMember() {
        return Mono.justOrEmpty(getAuthor())
                .flatMap(author -> getGuild()
                        .map(Guild::getId)
                        .flatMap(author::asMember));
    }

    /**
     * Gets the raw author data of this message.
     *
     * @return The raw author data of this message.
     */
    @Experimental
    public UserData getUserData() {
        return data.author();
    }

    /**
     * Gets the contents of the message, if present.
     *
     * @return The contents of the message, if present.
     */
    public String getContent() {
        return data.content();
    }

    /**
     * Gets when this message was sent.
     *
     * @return When this message was sent.
     */
    public Instant getTimestamp() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(data.timestamp(), Instant::from);
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

    /**
     * Gets whether this was a TTS (Text-To-Speech) message.
     *
     * @return {@code true} if this message was a TTS (Text-To-Speech) message, {@code false} otherwise.
     */
    public boolean isTts() {
        return data.tts();
    }

    /**
     * Gets whether this message mentions everyone.
     *
     * @return {@code true} if this message mentions everyone, {@code false} otherwise.
     */
    public boolean mentionsEveryone() {
        return data.mentionEveryone();
    }

    /**
     * Gets the IDs of the users specifically mentioned in this message, without duplication and with the same order
     * as in the message.
     *
     * @return The IDs of the users specifically mentioned in this message, without duplication and with the same order
     * as in the message.
     */
    public /*~~>*/List<Snowflake> getUserMentionIds() {
        return data.mentions().stream()
                .map(UserData::id)
                .map(Snowflake::of)
                .collect(Collectors.toList());
    }

    /**
     * Gets the partial members specifically mentioned in this message, without duplication and with the same order
     * as in the message.
     *
     * @return The partial members specifically mentioned in this message, without duplication and with the same order
     * as in the message.
     */
    public /*~~>*/List<PartialMember> getMemberMentions() {
        if (data.guildId().isAbsent()) {
            return Collections.emptyList();
        }
        long guildId = data.guildId().get().asLong();
        return data.mentions().stream()
            .map(data -> new PartialMember(gateway, data, data.member().get(), guildId))
            .collect(Collectors.toList());
    }

    /**
     * Gets the users specifically mentioned in this message, without duplication and with the same order
     * as in the message.
     *
     * @return The users specifically mentioned in this message, without duplication and with the same order
     * as in the message.
     */
    public /*~~>*/List<User> getUserMentions() {
        return data.mentions().stream()
                .map(data -> new User(gateway, data))
                .collect(Collectors.toList());
    }

    /**
     * Gets the IDs of the roles specifically mentioned in this message, without duplication and with the same order
     * as in the message.
     *
     * @return The IDs of the roles specifically mentioned in this message, without duplication and with the same order
     * as in the message.
     */
    public /*~~>*/List<Snowflake> getRoleMentionIds() {
        return data.mentionRoles().stream()
                .map(Snowflake::of)
                .collect(Collectors.toList());
    }

    /**
     * Requests to retrieve the roles specifically mentioned in this message.
     *
     * @return A {@link Flux} that continually emits {@link Role roles} specifically mentioned in this message. If an
     * error is received, it is emitted through the {@code Flux}.
     */
    public Flux<Role> getRoleMentions() {
        return Flux.fromIterable(getRoleMentionIds())
                .flatMap(roleId -> getGuild()
                        .map(Guild::getId)
                        .flatMap(guildId -> gateway.getRoleById(guildId, roleId)));
    }

    /**
     * Requests to retrieve the roles specifically mentioned in this message, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the roles
     * @return A {@link Flux} that continually emits {@link Role roles} specifically mentioned in this message. If an
     * error is received, it is emitted through the {@code Flux}.
     */
    public Flux<Role> getRoleMentions(EntityRetrievalStrategy retrievalStrategy) {
        return Flux.fromIterable(getRoleMentionIds())
                .flatMap(roleId -> getGuild()
                        .map(Guild::getId)
                        .flatMap(guildId -> gateway.withRetrievalStrategy(retrievalStrategy)
                                .getRoleById(guildId, roleId)));
    }

    /**
     * Gets any attached files, with the same order as in the message.
     *
     * @return Any attached files, with the same order as in the message.
     */
    public /*~~>*/List<Attachment> getAttachments() {
        return data.attachments().stream()
                .map(data -> new Attachment(gateway, data))
                .collect(Collectors.toList());
    }

    /**
     * Gets any embedded content.
     *
     * @return Any embedded content.
     */
    public /*~~>*/List<Embed> getEmbeds() {
        return data.embeds().stream()
                .map(data -> new Embed(gateway, data))
                .collect(Collectors.toList());
    }

    /**
     * Gets the reactions to this message, the order is the same as in the message.
     *
     * @return The reactions to this message, the order is the same as in the message.
     */
    public /*~~>*/List<Reaction> getReactions() {
        return data.reactions().toOptional()
                .map(reactions -> reactions.stream()
                        .map(data -> new Reaction(gateway, data))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

    }

    /**
     * Requests to retrieve the reactors (users) for the specified emoji for this message.
     *
     * @param emoji The emoji to get the reactors (users) for this message.
     * @return A {@link Flux} that continually emits the {@link User reactors} for the specified emoji for this message.
     * If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<User> getReactors(final ReactionEmoji emoji) {
        final Function<Map<String, Object>, Flux<UserData>> makeRequest = params ->
                gateway.getRestClient().getChannelService()
                        .getReactions(getChannelId().asLong(), getId().asLong(),
                                EntityUtil.getEmojiString(emoji),
                                params);

        return PaginationUtil.paginateAfter(makeRequest, data -> Snowflake.asLong(data.id()), 0L, 100)
                .map(data -> new User(gateway, data));
    }

    /**
     * Gets whether this message is pinned.
     *
     * @return {@code true} if this message is pinned, {@code false} otherwise.
     */
    public boolean isPinned() {
        return data.pinned();
    }

    /**
     * Requests to retrieve the webhook that generated this message, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Webhook webhook} that generated this
     * message, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Webhook> getWebhook() {
        return Mono.justOrEmpty(getWebhookId()).flatMap(gateway::getWebhookById);
    }

    /**
     * Returns the {@link MessageReference} sent with crossposted messages and replies, if present.
     *
     * @return The {@link MessageReference} sent with crossposted messages and replies, if present.
     */
    public Optional<MessageReference> getMessageReference() {
        return data.messageReference().toOptional()
                .map(data -> new MessageReference(gateway, data));
    }

    /**
     * Returns the flags of this {@link Message}, describing its features.
     *
     * @return A {@code EnumSet} with the flags of this message.
     */
    public EnumSet<Flag> getFlags() {
        return data.flags().toOptional()
                .map(Flag::of)
                .orElse(EnumSet.noneOf(Flag.class));
    }

    /**
     * Gets the ID of the guild this message is associated to, if this {@code Message} was built from Gateway data,
     * like an incoming event. If requested from REST API, this field will be empty.
     *
     * @return The ID of the guild this message is associated to, if present.
     */
    public Optional<Snowflake> getGuildId() {
        return data.guildId().toOptional().map(Snowflake::of);
    }

    /**
     * Requests to retrieve the guild this message is associated to, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} this message is associated to,
     * if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getChannel().ofType(GuildChannel.class).flatMap(GuildChannel::getGuild);
    }

    /**
     * Requests to retrieve the guild this message is associated to, if present, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the guild
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} this message is associated to,
     * if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild(EntityRetrievalStrategy retrievalStrategy) {
        return getChannel(retrievalStrategy).ofType(GuildChannel.class)
                .flatMap(guildChannel -> guildChannel.getGuild(retrievalStrategy));
    }

    /**
     * Gets the type of message.
     *
     * @return The type of message.
     */
    public Type getType() {
        return Type.of(data.type());
    }

    /**
     * Gets the partial stickers sent with the message.
     *
     * @return The partial stickers sent with the message.
     */
    @Experimental
    public /*~~>*/List<PartialSticker> getStickersItems() {
        return data.stickerItems().toOptional()
            .map(partialStickers -> partialStickers.stream()
                .map(data -> new PartialSticker(gateway, data))
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    /**
     * Gets the stickers sent with the message.
     *
     * @return The stickers sent with the message.
     */
    @Deprecated
    public /*~~>*/List<Sticker> getStickers() {
        return data.stickers().toOptional()
                .orElse(Collections.emptyList())
                .stream()
                .map(data -> new Sticker(gateway, data))
                .collect(Collectors.toList());
    }

    /**
     * Returns the message associated with the {@link MessageReference}, if present.
     *
     * @return The message associated with the {@link MessageReference}, if present.
     */
    public Optional<Message> getReferencedMessage() {
        return Possible.flatOpt(data.referencedMessage())
                .map(data -> new Message(gateway, data));
    }

    /**
     * Gets the interaction data, if the message is a response to an {@link discord4j.rest.interaction.Interactions}.
     *
     * @return The interaction data, if the message is a response to an {@link discord4j.rest.interaction.Interactions}.
     */
    public Optional<MessageInteraction> getInteraction() {
        return data.interaction().toOptional()
                .map(data -> new MessageInteraction(gateway, data));
    }

    /**
     * Gets the components on the message.
     *
     * @return The components on the message.
     */
    public /*~~>*/List<LayoutComponent> getComponents() {
        return data.components().toOptional()
                .map(components -> components.stream()
                        .map(MessageComponent::fromData)
                        // top level message components should only be LayoutComponents
                        .filter(component -> component instanceof LayoutComponent)
                        .map(component -> (LayoutComponent) component)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * Requests to edit this message.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyMessageEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Message}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #edit(MessageEditSpec)}  or {@link #edit()} which offer an immutable approach to build
     * specs
     */
    @Deprecated
    public Mono<Message> edit(final Consumer<? super LegacyMessageEditSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyMessageEditSpec mutatedSpec = new LegacyMessageEditSpec();
                    getClient().getRestClient().getRestResources()
                            .getAllowedMentions()
                            .ifPresent(mutatedSpec::setAllowedMentions);
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getChannelService()
                            .editMessage(getChannelId().asLong(), getId().asLong(),
                                    MultipartRequest.ofRequest(mutatedSpec.asRequest()));
                })
                .map(data -> new Message(gateway, data));
    }

    /**
     * Requests to edit this message.
     *
     * @param spec an immutable object that specifies how to edit the message
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Message}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> edit(MessageEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> {
                    MessageEditSpec actualSpec = getClient().getRestClient().getRestResources()
                            .getAllowedMentions()
                            .filter(allowedMentions -> !spec.isAllowedMentionsPresent())
                            .map(spec::withAllowedMentionsOrNull)
                            .orElse(spec);
                    return gateway.getRestClient().getChannelService()
                            .editMessage(getChannelId().asLong(), getId().asLong(), actualSpec.asRequest());
                })
                .map(data -> new Message(gateway, data));
    }

    /**
     * Requests to edit this message. Properties specifying how to edit this message can be set via the {@code
     * withXxx} methods of the returned {@link MessageEditMono}.
     *
     * @return A {@link MessageEditMono} where, upon successful completion, emits the edited {@link Message}. If an
     * error is received, it is emitted through the {@code MessageEditMono}.
     * @see #edit(MessageEditSpec)
     */
    public MessageEditMono edit() {
        return MessageEditMono.of(this);
    }

    /**
     * Requests to delete this message.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the message has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return delete(null);
    }

    /**
     * Requests to delete this message while optionally specifying a reason.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the message has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(@Nullable final String reason) {
        return gateway.getRestClient().getChannelService()
                .deleteMessage(getChannelId().asLong(), getId().asLong(), reason);
    }

    /**
     * Requests to suppress all embeds in this message. If the message have the embeds suppressed then this action
     * can undo the suppressed embeds.
     *
     * @param suppress Determine if you need suppress or not the embeds.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the process has been
     * completed. If an error is received, it is emitted through the {@code Mono}.
     */
    @Experimental
    public Mono<Void> suppressEmbeds(final boolean suppress) {
        return gateway.getRestClient().getChannelService()
                .suppressEmbeds(getChannelId().asLong(), getId().asLong(),
                        SuppressEmbedsRequest.builder().suppress(suppress).build());
    }

    /**
     * Requests to add a reaction on this message.
     *
     * @param emoji The reaction to add on this message.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the reaction was added on
     * this message. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> addReaction(final ReactionEmoji emoji) {
        return gateway.getRestClient().getChannelService()
                .createReaction(getChannelId().asLong(), getId().asLong(), EntityUtil.getEmojiString(emoji));
    }

    /**
     * Requests to remove a reaction from a specified user on this message.
     *
     * @param emoji The reaction to remove on this message.
     * @param userId The user to remove the reaction on this message.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the reaction from the
     * specified user was removed on this message. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> removeReaction(final ReactionEmoji emoji, final Snowflake userId) {
        return gateway.getRestClient().getChannelService()
                .deleteReaction(getChannelId().asLong(), getId().asLong(), EntityUtil.getEmojiString(emoji),
                        userId.asLong());
    }

    /**
     * Requests to remove all reactions of a specific emoji on this message.
     *
     * @param emoji The reaction to remove on this message
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the reaction from the
     * specified user was removed on this message. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> removeReactions(final ReactionEmoji emoji) {
        return gateway.getRestClient().getChannelService()
                .deleteReactions(getChannelId().asLong(), getId().asLong(), EntityUtil.getEmojiString(emoji));
    }

    /**
     * Requests to remove a reaction from the current user on this message.
     *
     * @param emoji The reaction to remove on this message.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the reaction from the current
     * user was removed on this message. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> removeSelfReaction(final ReactionEmoji emoji) {
        return gateway.getRestClient().getChannelService()
                .deleteOwnReaction(getChannelId().asLong(), getId().asLong(), EntityUtil.getEmojiString(emoji));
    }

    /**
     * Requests to remove all the reactions on this message.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating all the reactions on this
     * message were removed. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> removeAllReactions() {
        return gateway.getRestClient().getChannelService()
                .deleteAllReactions(getChannelId().asLong(), getId().asLong());
    }

    /**
     * Requests to pin this message.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the messaged was pinned. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> pin() {
        return gateway.getRestClient().getChannelService()
                .addPinnedMessage(getChannelId().asLong(), getId().asLong());
    }

    /**
     * Requests to unpin this message.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the message was unpinned. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> unpin() {
        return gateway.getRestClient().getChannelService()
                .deletePinnedMessage(getChannelId().asLong(), getId().asLong());
    }

    /**
     * Requests to publish (crosspost) this message if the {@code channel} is of type 'news'.
     * Requires 'SEND_MESSAGES' permission if the current user sent the message, or additionally the
     * 'MANAGE_MESSAGES' permission, for all other messages, to be present for the current user.
     *
     * @return A {@link Mono} where, upon successful completion, emits the published {@link Message} in the guilds.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> publish() {
        return gateway.getRestClient().getChannelService()
                .publishMessage(getChannelId().asLong(), getId().asLong())
                .map(data -> new Message(gateway, data));
    }

    /**
     * Creates a new thread from an existing message.
     *
     * @param spec an immutable object that specifies how to create the thread
     * @return A {@link Mono} where, upon successful completion, emits the created {@link ThreadChannel}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<ThreadChannel> startThread(StartThreadSpec spec) {
        return gateway.getRestClient().getChannelService()
                .startThreadWithMessage(getChannelId().asLong(), getId().asLong(), spec.asRequest())
                .map(data -> new ThreadChannel(gateway, data));
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return EntityUtil.hashCode(this);
    }

    /**
     * Describes extra features of a message.
     */
    public enum Flag {

        /** This message has been published to subscribed channels (via Channel Following). */
        CROSSPOSTED(0),

        /** This message originated from a message in another channel (via Channel Following). */
        IS_CROSSPOST(1),

        /** Do not include any embeds when serializing this message. */
        SUPPRESS_EMBEDS(2),

        /** The source message for this crosspost has been deleted (via Channel Following). */
        SOURCE_MESSAGE_DELETED(3),

        /** This message came from the urgent message system. */
        URGENT(4),

        /** This message has an associated thread, with the same id as the message. */
        HAS_THREAD(5),

        /** This message is an ephemeral interaction response. */
        EPHEMERAL(6),

        /** This message is an Interaction Response and the bot is "thinking". */
        LOADING(7);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * The flag value as represented by Discord.
         */
        private final int flag;

        /**
         * Constructs a {@code Message.Flag}.
         */
        Flag(final int value) {
            this.value = value;
            this.flag = 1 << value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the flag value as represented by Discord.
         *
         * @return The flag value as represented by Discord.
         */
        public int getFlag() {
            return flag;
        }

        /**
         * Gets the flags of message. It is guaranteed that invoking {@link #getValue()} from the returned enum will be
         * equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The flags value as represented by Discord.
         * @return The {@link EnumSet} of flags.
         */
        public static EnumSet<Flag> of(final int value) {
            final EnumSet<Flag> messageFlags = EnumSet.noneOf(Flag.class);
            for (Flag flag : Flag.values()) {
                long flagValue = flag.getFlag();
                if ((flagValue & value) == flagValue) {
                    messageFlags.add(flag);
                }
            }
            return messageFlags;
        }
    }

    /**
     * Represents the various types of messages.
     */
    public enum Type {

        /**
         * Unknown type.
         */
        UNKNOWN(-1),

        /**
         * A message created by a user.
         */
        DEFAULT(0),

        /**
         * A message created when a recipient was added to a DM.
         */
        RECIPIENT_ADD(1),

        /**
         * A message created when a recipient left a DM.
         */
        RECIPIENT_REMOVE(2),

        /**
         * A message created when a call was started.
         */
        CALL(3),

        /**
         * A message created when a channel's name changed.
         */
        CHANNEL_NAME_CHANGE(4),

        /**
         * A message created when a channel's icon changed.
         */
        CHANNEL_ICON_CHANGE(5),

        /**
         * A message created when a message was pinned.
         */
        CHANNEL_PINNED_MESSAGE(6),

        /**
         * A message created when an user joins a guild.
         */
        GUILD_MEMBER_JOIN(7),

        /**
         * A message created when an user boost a guild.
         */
        USER_PREMIUM_GUILD_SUBSCRIPTION(8),

        /**
         * A message created when an user boost a guild and the guild reach the tier 1.
         */
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_1(9),

        /**
         * A message created when an user boost a guild and the guild reach the tier 2.
         */
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_2(10),

        /**
         * A message created when an user boost a guild and the guild reach the tier 3.
         */
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_3(11),

        /**
         * A message created when a user follows a channel from another guild into specific channel (
         * <a href="https://support.discord.com/hc/en-us/articles/360028384531-Server-Following-FAQ">Server Following</a>).
         */
        CHANNEL_FOLLOW_ADD(12),

        /** A message created when the Guild is disqualified for Discovery Feature **/
        GUILD_DISCOVERY_DISQUALIFIED(14),

        /** A message created when the Guild is requalified for Discovery Feature **/
        GUILD_DISCOVERY_REQUALIFIED(15),

        /** A message created for warning about the grace period of Guild Discovery **/
        GUILD_DISCOVERY_GRACE_PERIOD_INITIAL_WARNING(16),

        /** A message created for last warning about the grace period of Guild Discovery **/
        GUILD_DISCOVERY_GRACE_PERIOD_FINAL_WARNING(17),

        /**
         * A message created when a Thread is started ( <a href="https://support.discord.com/hc/es/articles/4403205878423-Threads">Threads</a> )
         */
        THREAD_CREATED(18),

        /** A message created with a reply */
        REPLY(19),

        /** A message created using an application (like slash commands) **/
        APPLICATION_COMMAND(20),

        /**
         * The first message in a thread pointing to a related message in the parent channel from which the thread was started
         * <br>
         * <b>Note: </b> Only supported from v9 of API
        **/
        THREAD_STARTER_MESSAGE(21),

        /** A message created for notice the servers owners about invite new users (only in new servers) **/
        GUILD_INVITE_REMINDER(22),

        CONTEXT_MENU_COMMAND(23);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * Constructs a {@code Message.Type}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Type(final int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the type of message. It is guaranteed that invoking {@link #getValue()} from the returned enum will be
         * equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of message.
         */
        public static Type of(final int value) {
            switch (value) {
                case 0: return DEFAULT;
                case 1: return RECIPIENT_ADD;
                case 2: return RECIPIENT_REMOVE;
                case 3: return CALL;
                case 4: return CHANNEL_NAME_CHANGE;
                case 5: return CHANNEL_ICON_CHANGE;
                case 6: return CHANNEL_PINNED_MESSAGE;
                case 7: return GUILD_MEMBER_JOIN;
                case 8: return USER_PREMIUM_GUILD_SUBSCRIPTION;
                case 9: return USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_1;
                case 10: return USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_2;
                case 11: return USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_3;
                case 12: return CHANNEL_FOLLOW_ADD;
                case 14: return GUILD_DISCOVERY_DISQUALIFIED;
                case 15: return GUILD_DISCOVERY_REQUALIFIED;
                case 16: return GUILD_DISCOVERY_GRACE_PERIOD_INITIAL_WARNING;
                case 17: return GUILD_DISCOVERY_GRACE_PERIOD_FINAL_WARNING;
                case 18: return THREAD_CREATED;
                case 19: return REPLY;
                case 20: return APPLICATION_COMMAND;
                case 21: return THREAD_STARTER_MESSAGE;
                case 22: return GUILD_INVITE_REMINDER;
                case 23: return CONTEXT_MENU_COMMAND;
                default: return UNKNOWN;
            }
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "data=" + data +
                '}';
    }
}
