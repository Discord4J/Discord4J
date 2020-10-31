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

package discord4j.common.store.impl;

import discord4j.discordjson.json.*;
import discord4j.discordjson.possible.Possible;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.stream.Collectors;

import static discord4j.common.store.impl.ImplUtils.*;

/**
 * Message data with snowflakes stored as long, with atomically mutable reactions, and with author data taken from an
 * existing user reference.
 */
class StoredMessageData {
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<StoredMessageData, ConcurrentMap> REACTIONS_LAZY_INITIALIZER =
            AtomicReferenceFieldUpdater.newUpdater(StoredMessageData.class, ConcurrentMap.class, "reactions");

    private final long id;
    private final long channelId;
    private final long guildId_value;
    private final boolean guildId_absent;
    private final AtomicReference<StoredUserData> author;
    private final String content;
    private final String timestamp;
    private final String editedTimestamp;
    private final boolean tts;
    private final boolean mentionEveryone;
    private final List<UserWithMemberData> mentions;
    private final List<String> mentionRoles;
    private final List<ChannelMentionData> mentionChannels_value;
    private final boolean mentionChannels_absent;
    private final List<AttachmentData> attachments;
    private final List<EmbedData> embeds;
    private final Object nonce_value;
    private final boolean nonce_absent;
    private final boolean pinned;
    private final long webhookId_value;
    private final boolean webhookId_absent;
    private final int type;
    private final MessageActivityData activity_value;
    private final boolean activity_absent;
    private final MessageApplicationData application_value;
    private final boolean application_absent;
    private final MessageReferenceData messageReference_value;
    private final boolean messageReference_absent;
    private final int flags_value;
    private final boolean flags_absent;
    private volatile ConcurrentMap<EmojiKey, StoredReactionData> reactions;
    
    StoredMessageData(MessageData original, AtomicReference<StoredUserData> userRef) {
        this.id = toLongId(original.id());
        this.channelId = toLongId(original.channelId());
        this.guildId_value = idFromPossibleString(original.guildId()).orElse(-1L);
        this.guildId_absent = original.guildId().isAbsent();
        this.author = userRef;
        this.content = original.content();
        this.timestamp = original.timestamp();
        this.editedTimestamp = original.editedTimestamp().orElse(null);
        this.tts = original.tts();
        this.mentionEveryone = original.mentionEveryone();
        this.mentions = original.mentions();
        this.mentionRoles = original.mentionRoles();
        this.mentionChannels_value = original.mentionChannels().toOptional().orElse(null);
        this.mentionChannels_absent = original.mentionChannels().isAbsent();
        this.attachments = original.attachments();
        this.embeds = original.embeds();
        this.nonce_value = original.nonce().toOptional().orElse(null);
        this.nonce_absent = original.nonce().isAbsent();
        this.pinned = original.pinned();
        this.webhookId_value = idFromPossibleString(original.webhookId()).orElse(-1L);
        this.webhookId_absent = original.webhookId().isAbsent();
        this.type = original.type();
        this.activity_value = original.activity().toOptional().orElse(null);
        this.activity_absent = original.activity().isAbsent();
        this.application_value = original.application().toOptional().orElse(null);
        this.application_absent = original.application().isAbsent();
        this.messageReference_value = original.messageReference().toOptional().orElse(null);
        this.messageReference_absent = original.messageReference().isAbsent();
        this.flags_value = original.flags().toOptional().orElse(-1);
        this.flags_absent = original.flags().isAbsent();
        this.reactions = original.reactions().isAbsent()
                ? null
                : original.reactions().get().stream()
                        .map(StoredReactionData::new)
                        .collect(Collectors.toConcurrentMap(r -> new EmojiKey(r.emoji), Function.identity()));
    }

    StoredMessageData(StoredMessageData current, PartialMessageData update) {
        this.id = current.id;
        this.channelId = toLongId(update.channelId());
        this.guildId_value = idFromPossibleString(update.guildId()).orElse(current.guildId_value);
        this.guildId_absent = current.guildId_absent && update.guildId().isAbsent();
        this.author = current.author;
        this.content = update.content().toOptional().orElse(current.content);
        this.timestamp = update.timestamp().toOptional().orElse(current.timestamp);
        this.editedTimestamp = update.editedTimestamp().orElse(null);
        this.tts = update.tts().toOptional().orElse(current.tts);
        this.mentionEveryone = update.mentionEveryone().toOptional().orElse(current.mentionEveryone);
        this.mentions = update.mentions();
        this.mentionRoles = update.mentionRoles();
        this.mentionChannels_value = update.mentionChannels().toOptional().orElse(current.mentionChannels_value);
        this.mentionChannels_absent = current.mentionChannels_absent && update.mentionChannels().isAbsent();
        this.attachments = update.attachments();
        this.embeds = update.embeds();
        this.nonce_value = update.nonce().toOptional().orElse(current.nonce_value);
        this.nonce_absent = current.nonce_absent && update.nonce().isAbsent();
        this.pinned = update.pinned().toOptional().orElse(current.pinned);
        this.webhookId_value = idFromPossibleString(update.webhookId()).orElse(current.webhookId_value);
        this.webhookId_absent = current.webhookId_absent && update.webhookId().isAbsent();
        this.type = update.type().toOptional().orElse(current.type);
        this.activity_value = update.activity().toOptional().orElse(current.activity_value);
        this.activity_absent = current.activity_absent && update.activity().isAbsent();
        this.application_value = update.application().toOptional().orElse(current.application_value);
        this.application_absent = current.application_absent && update.application().isAbsent();
        this.messageReference_value = update.messageReference().toOptional().orElse(current.messageReference_value);
        this.messageReference_absent = current.messageReference_absent && update.messageReference().isAbsent();
        this.flags_value = update.flags().toOptional().orElse(current.flags_value);
        this.flags_absent = current.flags_absent && update.flags().isAbsent();
        this.reactions = update.reactions().isAbsent()
                ? current.reactions
                : update.reactions().get().stream()
                        .map(StoredReactionData::new)
                        .collect(Collectors.toConcurrentMap(r -> new EmojiKey(r.emoji), Function.identity()));
    }

    long id() {
        return id;
    }

    long guildId() {
        return guildId_value;
    }

    private ConcurrentMap<EmojiKey, StoredReactionData> lazyGetReactions() {
        for (;;) {
            ConcurrentMap<EmojiKey, StoredReactionData> current = reactions;
            if (current != null) {
                return current;
            }
            ConcurrentMap<EmojiKey, StoredReactionData> newMap = new ConcurrentHashMap<>();
            if (!REACTIONS_LAZY_INITIALIZER.compareAndSet(this, null, newMap)) {
                continue;
            }
            return newMap;
        }
    }

    void addReaction(EmojiData emoji, boolean me) {
        ConcurrentMap<EmojiKey, StoredReactionData> reactions = lazyGetReactions();
        EmojiKey key = new EmojiKey(emoji);
        for (;;) {
            StoredReactionData existing = reactions.get(key);
            if (existing == null) {
                if (reactions.putIfAbsent(key, new StoredReactionData(1, me, emoji)) != null) {
                    continue;
                }
            } else {
                existing.increment(me);
            }
            return;
        }
    }

    void removeReaction(EmojiData emoji, boolean me) {
        ifNonNullDo(reactions, reactions -> {
            EmojiKey key = new EmojiKey(emoji);
            ifNonNullDo(reactions.get(key), existing -> {
                boolean shouldRemove = existing.decrement(me);
                if (shouldRemove) {
                    reactions.remove(key);
                }
            });
        });
    }

    void removeReactionEmoji(EmojiData emoji) {
        ifNonNullDo(reactions, reactions -> reactions.remove(new EmojiKey(emoji)));
    }

    void removeAllReactions() {
        ifNonNullDo(reactions, ConcurrentMap::clear);
    }

    MessageData toImmutable(@Nullable StoredMemberData member) {
        ConcurrentMap<EmojiKey, StoredReactionData> reactions = this.reactions;
        return MessageData.builder()
                .id("" + id)
                .channelId("" + channelId)
                .guildId(toPossibleStringId(guildId_value, guildId_absent))
                .author(author.get().toImmutable())
                .member(member == null ? Possible.absent() : Possible.of(member.toPartialImmutable()))
                .content(content)
                .timestamp(timestamp)
                .editedTimestamp(Optional.ofNullable(editedTimestamp))
                .tts(tts)
                .mentionEveryone(mentionEveryone)
                .mentions(mentions)
                .mentionRoles(mentionRoles)
                .mentionChannels(toPossible(mentionChannels_value, mentionChannels_absent))
                .attachments(attachments)
                .embeds(embeds)
                .reactions(reactions == null
                        ? Possible.absent()
                        : Possible.of(reactions.values().stream()
                        .map(StoredReactionData::toImmutable)
                        .collect(Collectors.toList())))
                .nonce(toPossible(nonce_value, nonce_absent))
                .pinned(pinned)
                .webhookId(toPossibleStringId(webhookId_value, webhookId_absent))
                .type(type)
                .activity(toPossible(activity_value, activity_absent))
                .application(toPossible(application_value, application_absent))
                .messageReference(toPossible(messageReference_value, messageReference_absent))
                .flags(toPossible(flags_value, flags_absent))
                .build();
    }

    private static class StoredReactionData {

        private static final AtomicReferenceFieldUpdater<StoredReactionData, UpdatableFields> REACTION_UPDATER =
                AtomicReferenceFieldUpdater.newUpdater(StoredReactionData.class, UpdatableFields.class,
                        "updatableFields");
        private final EmojiData emoji;
        private volatile UpdatableFields updatableFields;

        public StoredReactionData(ReactionData original) {
            this(original.count(), original.me(), original.emoji());
        }

        public StoredReactionData(int count, boolean me, EmojiData emoji) {
            this.emoji = emoji;
            this.updatableFields = new UpdatableFields(count, me);
        }

        void increment(boolean me) {
            for (;;) {
                UpdatableFields current = updatableFields;
                UpdatableFields next = new UpdatableFields(current.count + 1, me || current.me);
                if (!REACTION_UPDATER.compareAndSet(this, current, next)) {
                    continue;
                }
                return;
            }
        }

        boolean decrement(boolean me) {
            for (;;) {
                UpdatableFields current = updatableFields;
                UpdatableFields next = new UpdatableFields(Math.max(0, current.count - 1), !me && current.me);
                if (!REACTION_UPDATER.compareAndSet(this, current, next)) {
                    continue;
                }
                return next.count == 0;
            }
        }

        ReactionData toImmutable() {
            UpdatableFields updatableFields = this.updatableFields;
            return ReactionData.builder()
                    .emoji(emoji)
                    .count(updatableFields.count)
                    .me(updatableFields.me)
                    .build();
        }

        private static class UpdatableFields {
            private final int count;
            private final boolean me;

            private UpdatableFields(int count, boolean me) {
                this.count = count;
                this.me = me;
            }
        }
    }

    private static class EmojiKey {

        private final long id;
        private final String name;

        private EmojiKey(EmojiData emoji) {
            this.id = emoji.id().map(ImplUtils::toLongId).orElse(-1L);
            this.name = emoji.name().orElse(null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EmojiKey emojiKey = (EmojiKey) o;
            return id != -1 ? id == emojiKey.id : Objects.equals(name, emojiKey.name);
        }

        @Override
        public int hashCode() {
            return id != -1 ? Long.hashCode(id) : Objects.hash(name);
        }
    }
}
