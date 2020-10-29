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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.stream.Collectors;

import static discord4j.common.store.impl.ImplUtils.ifNonNullDo;

/**
 * MessageData with atomically mutable PartialMessageData fields, and mutable reactions
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class StoredMessageData implements MessageData {

    private static final AtomicReferenceFieldUpdater<StoredMessageData, MessageUpdateFields> UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(StoredMessageData.class, MessageUpdateFields.class,
                    "messageUpdateFields");
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<StoredMessageData, ConcurrentMap> REACTIONS_LAZY_INITIALIZER =
            AtomicReferenceFieldUpdater.newUpdater(StoredMessageData.class, ConcurrentMap.class, "reactions");

    private final String id;
    private final String channelId;
    private final Possible<String> guildId;
    private final UserData author;
    private final Possible<PartialMemberData> member;
    private final String timestamp;
    private final boolean tts;
    private final Possible<List<ChannelMentionData>> mentionChannels;
    private final List<AttachmentData> attachments;
    private final Possible<Object> nonce;
    private final boolean pinned;
    private final Possible<String> webhookId;
    private final int type;
    private final Possible<MessageActivityData> activity;
    private final Possible<MessageApplicationData> application;
    private final Possible<MessageReferenceData> messageReference;
    private final Possible<Integer> flags;
    private volatile MessageUpdateFields messageUpdateFields;
    private volatile ConcurrentMap<EmojiKey, StoredReactionData> reactions;
    
    StoredMessageData(MessageData original) {
        this.id = original.id();
        this.channelId = original.channelId();
        this.guildId = original.guildId();
        this.author = original.author();
        this.member = original.member();
        this.timestamp = original.timestamp();
        this.tts = original.tts();
        this.mentionChannels = original.mentionChannels();
        this.attachments = original.attachments();
        this.nonce = original.nonce();
        this.pinned = original.pinned();
        this.webhookId = original.webhookId();
        this.type = original.type();
        this.activity = original.activity();
        this.application = original.application();
        this.messageReference = original.messageReference();
        this.flags = original.flags();
        this.messageUpdateFields = new MessageUpdateFields(original);
        this.reactions = original.reactions().isAbsent()
                ? null
                : original.reactions().get().stream()
                .map(StoredReactionData::new)
                .collect(Collectors.toConcurrentMap(r -> new EmojiKey(r.emoji), Function.identity()));
    }

    void update(PartialMessageData partial) {
        for (;;) {
            MessageUpdateFields current = messageUpdateFields;
            if (!UPDATER.compareAndSet(this, current, new MessageUpdateFields(current, partial))) {
                continue;
            }
            return;
        }
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
                existing.add(1);
                if (me) {
                    existing.setMe(true);
                }
            }
            return;
        }
    }

    void removeReaction(EmojiData emoji, boolean me) {
        ifNonNullDo(reactions, reactions -> {
            EmojiKey key = new EmojiKey(emoji);
            ifNonNullDo(reactions.get(key), existing -> {
                int newCount = existing.add(-1);
                if (newCount < 1) {
                    reactions.remove(key);
                } else if (me) {
                    existing.setMe(false);
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

    @Override
    public String id() {
        return id;
    }

    @Override
    public String channelId() {
        return channelId;
    }

    @Override
    public Possible<String> guildId() {
        return guildId;
    }

    @Override
    public UserData author() {
        return author;
    }

    @Override
    public Possible<PartialMemberData> member() {
        return member;
    }

    @Override
    public String content() {
        return messageUpdateFields.content;
    }

    @Override
    public String timestamp() {
        return timestamp;
    }

    @Override
    public Optional<String> editedTimestamp() {
        return messageUpdateFields.editedTimestamp;
    }

    @Override
    public boolean tts() {
        return tts;
    }

    @Override
    public boolean mentionEveryone() {
        return messageUpdateFields.mentionEveryone;
    }

    @Override
    public List<UserWithMemberData> mentions() {
        return messageUpdateFields.mentions;
    }

    @Override
    public List<String> mentionRoles() {
        return messageUpdateFields.mentionRoles;
    }

    @Override
    public Possible<List<ChannelMentionData>> mentionChannels() {
        return mentionChannels;
    }

    @Override
    public List<AttachmentData> attachments() {
        return attachments;
    }

    @Override
    public List<EmbedData> embeds() {
        return messageUpdateFields.embeds;
    }

    @Override
    public Possible<List<ReactionData>> reactions() {
        return reactions == null
                ? Possible.absent()
                : Possible.of(reactions.values().stream()
                .map(ImmutableReactionData::copyOf)
                .collect(Collectors.toList()));
    }

    @Override
    public Possible<Object> nonce() {
        return nonce;
    }

    @Override
    public boolean pinned() {
        return pinned;
    }

    @Override
    public Possible<String> webhookId() {
        return webhookId;
    }

    @Override
    public int type() {
        return type;
    }

    @Override
    public Possible<MessageActivityData> activity() {
        return activity;
    }

    @Override
    public Possible<MessageApplicationData> application() {
        return application;
    }

    @Override
    public Possible<MessageReferenceData> messageReference() {
        return messageReference;
    }

    @Override
    public Possible<Integer> flags() {
        return flags;
    }

    static class MessageUpdateFields {
        private final String content;
        private final List<EmbedData> embeds;
        private final List<UserWithMemberData> mentions;
        private final List<String> mentionRoles;
        private final boolean mentionEveryone;
        private final Optional<String> editedTimestamp;

        MessageUpdateFields(MessageData original) {
            this.content = original.content();
            this.embeds = original.embeds();
            this.mentions = original.mentions();
            this.mentionRoles = original.mentionRoles();
            this.mentionEveryone = original.mentionEveryone();
            this.editedTimestamp = original.editedTimestamp();
        }

        MessageUpdateFields(MessageUpdateFields existing, PartialMessageData partial) {
            this.content = partial.content().toOptional().orElse(existing.content);
            this.embeds = partial.embeds();
            this.mentions = partial.mentions();
            this.mentionRoles = partial.mentionRoles();
            this.mentionEveryone = partial.mentionEveryone().toOptional().orElse(existing.mentionEveryone);
            this.editedTimestamp = partial.editedTimestamp();
        }
    }

    static class StoredReactionData implements ReactionData {

        private static final AtomicIntegerFieldUpdater<StoredReactionData> COUNT_UPDATER =
                AtomicIntegerFieldUpdater.newUpdater(StoredReactionData.class, "count");
        private final EmojiData emoji;
        private volatile int count;
        private volatile boolean me;

        public StoredReactionData(ReactionData original) {
            this(original.count(), original.me(), original.emoji());
        }

        public StoredReactionData(int count, boolean me, EmojiData emoji) {
            this.count = count;
            this.me = me;
            this.emoji = emoji;
        }

        int add(int amount) {
            for (;;) {
                int current = count;
                int next = current + amount;
                if (!COUNT_UPDATER.compareAndSet(this, current, next)) {
                    continue;
                }
                return next;
            }
        }

        void setMe(boolean me) {
            this.me = me;
        }

        @Override
        public int count() {
            return count;
        }

        @Override
        public boolean me() {
            return me;
        }

        @Override
        public EmojiData emoji() {
            return emoji;
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
