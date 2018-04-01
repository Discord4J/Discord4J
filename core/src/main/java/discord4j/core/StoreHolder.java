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

import discord4j.core.object.entity.bean.*;
import discord4j.store.Store;
import discord4j.store.primitive.LongObjStore;
import discord4j.store.service.StoreService;
import discord4j.store.util.LongLongTuple2;

/** Holder for {@link Store} instances for use in caching. */
public final class StoreHolder {

    private final LongObjStore<AttachmentBean> attachmentStore;
    private final LongObjStore<CategoryBean> categoryStore;
    private final LongObjStore<GuildBean> guildStore;
    private final LongObjStore<GuildChannelBean> guildChannelStore;
    private final LongObjStore<GuildEmojiBean> guildEmojiStore;
    private final Store<LongLongTuple2, MemberBean> memberStore;
    private final LongObjStore<MessageBean> messageStore;
    private final LongObjStore<PrivateChannelBean> privateChannelStore;
    private final LongObjStore<RoleBean> roleStore;
    private final LongObjStore<TextChannelBean> textChannelStore;
    private final LongObjStore<UserBean> userStore;
    private final LongObjStore<VoiceChannelBean> voiceChannelStore;

    StoreHolder(final StoreService service) {
        attachmentStore = service.provideLongObjStore(AttachmentBean.class);
        categoryStore = service.provideLongObjStore(CategoryBean.class);
        guildStore = service.provideLongObjStore(GuildBean.class);
        guildChannelStore = service.provideLongObjStore(GuildChannelBean.class);
        guildEmojiStore = service.provideLongObjStore(GuildEmojiBean.class);
        memberStore = service.provideGenericStore(LongLongTuple2.class, MemberBean.class);
        messageStore = service.provideLongObjStore(MessageBean.class);
        privateChannelStore = service.provideLongObjStore(PrivateChannelBean.class);
        roleStore = service.provideLongObjStore(RoleBean.class);
        textChannelStore = service.provideLongObjStore(TextChannelBean.class);
        userStore = service.provideLongObjStore(UserBean.class);
        voiceChannelStore = service.provideLongObjStore(VoiceChannelBean.class);
    }

    public LongObjStore<AttachmentBean> getAttachmentStore() {
        return attachmentStore;
    }

    public LongObjStore<CategoryBean> getCategoryStore() {
        return categoryStore;
    }

    public LongObjStore<GuildBean> getGuildStore() {
        return guildStore;
    }

    public LongObjStore<GuildChannelBean> getGuildChannelStore() {
        return guildChannelStore;
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

    public LongObjStore<PrivateChannelBean> getPrivateChannelStore() {
        return privateChannelStore;
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
}
