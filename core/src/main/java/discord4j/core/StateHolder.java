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
package discord4j.core;

import discord4j.core.object.data.stored.*;
import discord4j.store.api.Store;
import discord4j.store.api.primitive.LongObjStore;
import discord4j.store.api.service.StoreService;
import discord4j.store.api.util.LongLongTuple2;
import discord4j.store.api.util.StoreContext;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.concurrent.atomic.AtomicLong;

/** Holder for various pieces of state for use in caching. */
public final class StateHolder {

    private static final Logger log = Loggers.getLogger(StateHolder.class);

    private final LongObjStore<CategoryBean> categoryStore;
    private final LongObjStore<GuildBean> guildStore;
    private final LongObjStore<GuildEmojiBean> guildEmojiStore;
    private final Store<LongLongTuple2, MemberBean> memberStore;
    private final LongObjStore<MessageBean> messageStore;
    private final Store<LongLongTuple2, PresenceBean> presenceStore;
    private final LongObjStore<RoleBean> roleStore;
    private final LongObjStore<TextChannelBean> textChannelStore;
    private final LongObjStore<UserBean> userStore;
    private final LongObjStore<VoiceChannelBean> voiceChannelStore;
    private final Store<LongLongTuple2, VoiceStateBean> voiceStateStore;
    private final AtomicLong selfId;

    StateHolder(final StoreService service, final StoreContext context) {
        service.init(context);

        categoryStore = service.provideLongObjStore(CategoryBean.class);
        log.debug("Using a {} backend for category storage.", categoryStore);

        guildStore = service.provideLongObjStore(GuildBean.class);
        log.debug("Using a {} backend for guild storage.", guildStore);

        guildEmojiStore = service.provideLongObjStore(GuildEmojiBean.class);
        log.debug("Using a {} backend for guild emoji storage.", guildEmojiStore);

        memberStore = service.provideGenericStore(LongLongTuple2.class, MemberBean.class);
        log.debug("Using a {} backend for member storage.", memberStore);

        messageStore = service.provideLongObjStore(MessageBean.class);
        log.debug("Using a {} backend for message storage.", messageStore);

        presenceStore = service.provideGenericStore(LongLongTuple2.class, PresenceBean.class);
        log.debug("Using a {} backend for presence storage.", presenceStore);

        roleStore = service.provideLongObjStore(RoleBean.class);
        log.debug("Using a {} backend for role storage.", roleStore);

        textChannelStore = service.provideLongObjStore(TextChannelBean.class);
        log.debug("Using a {} backend for text channel storage.", textChannelStore);

        userStore = service.provideLongObjStore(UserBean.class);
        log.debug("Using a {} backend for user storage.", userStore);

        voiceChannelStore = service.provideLongObjStore(VoiceChannelBean.class);
        log.debug("Using a {} backend for voice channel storage.", voiceChannelStore);

        voiceStateStore = service.provideGenericStore(LongLongTuple2.class, VoiceStateBean.class);
        log.debug("Using a {} backend for voice state storage.", voiceStateStore);
        selfId = new AtomicLong();
    }

    public LongObjStore<CategoryBean> getCategoryStore() {
        return categoryStore;
    }

    public LongObjStore<GuildBean> getGuildStore() {
        return guildStore;
    }

    public LongObjStore<GuildEmojiBean> getGuildEmojiStore() {
        return guildEmojiStore;
    }

    public Store<LongLongTuple2, MemberBean> getMemberStore() {
        return memberStore;
    }

    public LongObjStore<MessageBean> getMessageStore() {
        return messageStore;
    }

    public Store<LongLongTuple2, PresenceBean> getPresenceStore() {
        return presenceStore;
    }

    public LongObjStore<RoleBean> getRoleStore() {
        return roleStore;
    }

    public LongObjStore<TextChannelBean> getTextChannelStore() {
        return textChannelStore;
    }

    public LongObjStore<UserBean> getUserStore() {
        return userStore;
    }

    public LongObjStore<VoiceChannelBean> getVoiceChannelStore() {
        return voiceChannelStore;
    }

    public Store<LongLongTuple2, VoiceStateBean> getVoiceStateStore() {
        return voiceStateStore;
    }

    public AtomicLong getSelfId() {
        return selfId;
    }

    public Mono<Void> invalidateStores() {
        return categoryStore.invalidate()
                .and(guildStore.invalidate())
                .and(guildEmojiStore.invalidate())
                .and(memberStore.invalidate())
                .and(messageStore.invalidate())
                .and(presenceStore.invalidate())
                .and(roleStore.invalidate())
                .and(textChannelStore.invalidate())
                .and(userStore.invalidate())
                .and(voiceChannelStore.invalidate())
                .and(voiceStateStore.invalidate());
    }
}
