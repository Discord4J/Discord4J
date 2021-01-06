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

import discord4j.discordjson.json.*;
import discord4j.store.api.service.StoreService;
import discord4j.store.api.util.LongLongTuple2;
import discord4j.store.api.view.LongObjStoreView;
import discord4j.store.api.view.StoreView;

/**
 * Read-only view for various pieces of state for use in caching.
 * <p>
 * In addition to saving the current bot user ID, the following stores are kept in this class:
 * <ul>
 * <li>Channel store: {@code long} keys and {@link ChannelData} values.</li>
 * <li>Guild store: {@code long} keys and {@link GuildData} values.</li>
 * <li>Guild emoji store: {@code long} keys and {@link EmojiData} values.</li>
 * <li>Member store: {@code long} pair keys and {@link MemberData} values.</li>
 * <li>Message store: {@code long} keys and {@link MessageData} values.</li>
 * <li>Presence store: {@code long} pair keys and {@link PresenceData} values.</li>
 * <li>Role store: {@code long} keys and {@link RoleData} values.</li>
 * <li>User store: {@code long} keys and {@link UserData} values.</li>
 * <li>Voice state store: {@code long} pair keys and {@link VoiceStateData} values.</li>
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

    public LongObjStoreView<ChannelData> getChannelStore() {
        return new LongObjStoreView<>(stateHolder.getChannelStore());
    }

    public LongObjStoreView<GuildData> getGuildStore() {
        return new LongObjStoreView<>(stateHolder.getGuildStore());
    }

    public LongObjStoreView<EmojiData> getGuildEmojiStore() {
        return new LongObjStoreView<>(stateHolder.getGuildEmojiStore());
    }

    public StoreView<LongLongTuple2, MemberData> getMemberStore() {
        return new StoreView<>(stateHolder.getMemberStore());
    }

    public LongObjStoreView<MessageData> getMessageStore() {
        return new LongObjStoreView<>(stateHolder.getMessageStore());
    }

    public StoreView<LongLongTuple2, PresenceData> getPresenceStore() {
        return new StoreView<>(stateHolder.getPresenceStore());
    }

    public LongObjStoreView<RoleData> getRoleStore() {
        return new LongObjStoreView<>(stateHolder.getRoleStore());
    }

    public LongObjStoreView<UserData> getUserStore() {
        return new LongObjStoreView<>(stateHolder.getUserStore());
    }

    public StoreView<String, TemplateData> getTemplateStore() {
        return new StoreView<>(stateHolder.getTemplateStore());
    }

    public StoreView<LongLongTuple2, VoiceStateData> getVoiceStateStore() {
        return new StoreView<>(stateHolder.getVoiceStateStore());
    }

}
