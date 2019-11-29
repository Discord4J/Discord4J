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
package discord4j.core.state;

import discord4j.core.object.data.stored.*;
import discord4j.store.api.service.StoreService;
import discord4j.store.api.util.LongLongTuple2;

/**
 * Read-only view for various pieces of state for use in caching.
 * <p>
 * In addition to saving the current bot user ID, the following stores are kept in this class:
 * <ul>
 * <li>Channel store: {@code long} keys and {@link ChannelBean} values.</li>
 * <li>Guild store: {@code long} keys and {@link GuildBean} values.</li>
 * <li>Guild emoji store: {@code long} keys and {@link GuildEmojiBean} values.</li>
 * <li>Member store: {@code long} pair keys and {@link MemberBean} values.</li>
 * <li>Message store: {@code long} keys and {@link MessageBean} values.</li>
 * <li>Presence store: {@code long} pair keys and {@link PresenceBean} values.</li>
 * <li>Role store: {@code long} keys and {@link RoleBean} values.</li>
 * <li>User store: {@code long} keys and {@link UserBean} values.</li>
 * <li>Voice state store: {@code long} pair keys and {@link VoiceStateBean} values.</li>
 * </ul>
 */
public final class StateView {

    private final StateHolder stateHolder;

    public StateView(StateHolder stateHolder) {
        this.stateHolder = stateHolder;
    }

    public StoreService getStoreService() {
        return stateHolder.getStoreService();
    }

    public LongObjStoreView<ChannelBean> getChannelStore() {
        return new LongObjStoreView<>(stateHolder.getChannelStore());
    }

    public LongObjStoreView<GuildBean> getGuildStore() {
        return new LongObjStoreView<>(stateHolder.getGuildStore());
    }

    public LongObjStoreView<GuildEmojiBean> getGuildEmojiStore() {
        return new LongObjStoreView<>(stateHolder.getGuildEmojiStore());
    }

    public StoreView<LongLongTuple2, MemberBean> getMemberStore() {
        return new StoreView<>(stateHolder.getMemberStore());
    }

    public LongObjStoreView<MessageBean> getMessageStore() {
        return new LongObjStoreView<>(stateHolder.getMessageStore());
    }

    public StoreView<LongLongTuple2, PresenceBean> getPresenceStore() {
        return new StoreView<>(stateHolder.getPresenceStore());
    }

    public LongObjStoreView<RoleBean> getRoleStore() {
        return new LongObjStoreView<>(stateHolder.getRoleStore());
    }

    public LongObjStoreView<UserBean> getUserStore() {
        return new LongObjStoreView<>(stateHolder.getUserStore());
    }

    public StoreView<LongLongTuple2, VoiceStateBean> getVoiceStateStore() {
        return new StoreView<>(stateHolder.getVoiceStateStore());
    }

    public long getSelfId() {
        return stateHolder.getSelfId().get();
    }
}
