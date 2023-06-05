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

package discord4j.common.store.legacy;

import discord4j.discordjson.Id;
import discord4j.discordjson.json.*;
import discord4j.discordjson.json.gateway.PresenceUpdate;
import discord4j.store.api.Store;
import discord4j.store.api.primitive.LongObjStore;
import discord4j.store.api.service.StoreService;
import discord4j.store.api.util.LongLongTuple2;
import discord4j.store.api.util.StoreContext;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Holder for various pieces of state for use in caching.
 * <p>
 * In addition to saving the current bot user ID, the following stores are kept in this class:
 * <ul>
 * <li>Channel store: {@code long} keys and {@link ChannelData} values.</li>
 * <li>Guild store: {@code long} keys and {@link GuildData} values.</li>
 * <li>Guild scheduled event store: {@code long} pair keys and {@link GuildScheduledEventData} values.</li>
 * <li>Guild scheduled event users store: {@code long} pair keys and {@link Id} values.</li>
 * <li>Guild emoji store: {@code long} keys and {@link EmojiData} values.</li>
 * <li>Member store: {@code long} pair keys and {@link MemberData} values.</li>
 * <li>Message store: {@code long} keys and {@link MessageData} values.</li>
 * <li>Presence store: {@code long} pair keys and {@link PresenceUpdate} values.</li>
 * <li>Role store: {@code long} keys and {@link RoleData} values.</li>
 * <li>Stage instance store: {@code long} keys and {@link StageInstanceData} values.</li>
 * <li>User store: {@code long} keys and {@link UserData} values.</li>
 * <li>Voice state store: {@code long} pair keys and {@link VoiceStateData} values.</li>
 * <li>Thread member store: {@code long} pair keys and {@link ThreadMemberData} values.</li>
 * </ul>
 */
@SuppressWarnings("rawtypes")
public final class StateHolder {

    private static final Logger log = Loggers.getLogger(StateHolder.class);

    private final StoreService storeService;
    private final LongObjStore<ChannelData> channelStore;
    private final LongObjStore<GuildData> guildStore;
    private final Store<LongLongTuple2, GuildScheduledEventData> guildEventsStore;

    private final Store<LongLongTuple2, Set> guildEventsUsersStore;

    private final LongObjStore<EmojiData> guildEmojiStore;
    private final LongObjStore<StickerData> guildStickerStore;
    private final Store<LongLongTuple2, MemberData> memberStore;
    private final LongObjStore<MessageData> messageStore;
    private final Store<LongLongTuple2, PresenceData> presenceStore;
    private final LongObjStore<RoleData> roleStore;
    private final LongObjStore<StageInstanceData> stageInstanceStore;
    private final LongObjStore<UserData> userStore;
    private final Store<LongLongTuple2, VoiceStateData> voiceStateStore;
    private final Store<LongLongTuple2, ThreadMemberData> threadMemberStore;

    public StateHolder(final StoreService service) {
        storeService = service;

        service.init(new StoreContext(Collections.singletonMap("messageClass", MessageData.class)));

        channelStore = service.provideLongObjStore(ChannelData.class);
        log.debug("Channel storage           : {}", channelStore);

        guildStore = service.provideLongObjStore(GuildData.class);
        log.debug("Guild storage             : {}", guildStore);

        guildStickerStore = service.provideLongObjStore(StickerData.class);
        log.debug("Guild sticker storage     : {}", guildStickerStore);

        guildEventsStore = service.provideGenericStore(LongLongTuple2.class, GuildScheduledEventData.class);
        log.debug("Guild event storage       : {}", guildEventsStore);

        guildEventsUsersStore = service.provideGenericStore(LongLongTuple2.class, Set.class);
        log.debug("Guild event users storage : {}", guildEventsUsersStore);

        guildEmojiStore = service.provideLongObjStore(EmojiData.class);
        log.debug("Guild emoji storage       : {}", guildEmojiStore);

        memberStore = service.provideGenericStore(LongLongTuple2.class, MemberData.class);
        log.debug("Member storage            : {}", memberStore);

        messageStore = service.provideLongObjStore(MessageData.class);
        log.debug("Message storage           : {}", messageStore);

        presenceStore = service.provideGenericStore(LongLongTuple2.class, PresenceData.class);
        log.debug("Presence storage          : {}", presenceStore);

        roleStore = service.provideLongObjStore(RoleData.class);
        log.debug("Role storage              : {}", roleStore);

        stageInstanceStore = service.provideLongObjStore(StageInstanceData.class);
        log.debug("StageInstance storage     : {}", stageInstanceStore);

        userStore = service.provideLongObjStore(UserData.class);
        log.debug("User storage              : {}", userStore);

        voiceStateStore = service.provideGenericStore(LongLongTuple2.class, VoiceStateData.class);
        log.debug("Voice state storage       : {}", voiceStateStore);

        threadMemberStore = service.provideGenericStore(LongLongTuple2.class, ThreadMemberData.class);
        log.debug("Thread member storage     : {}", voiceStateStore);
    }

    public StoreService getStoreService() {
        return storeService;
    }

    public LongObjStore<ChannelData> getChannelStore() {
        return channelStore;
    }

    public LongObjStore<GuildData> getGuildStore() {
        return guildStore;
    }

    public LongObjStore<StickerData> getGuildStickerStore() {
        return guildStickerStore;
    }

    public Store<LongLongTuple2, GuildScheduledEventData> getGuildEventsStore() {
        return guildEventsStore;
    }

    public Store<LongLongTuple2, Set> getGuildEventsUsersStore() {
        return guildEventsUsersStore;
    }

    public LongObjStore<EmojiData> getGuildEmojiStore() {
        return guildEmojiStore;
    }

    public Store<LongLongTuple2, MemberData> getMemberStore() {
        return memberStore;
    }

    public LongObjStore<MessageData> getMessageStore() {
        return messageStore;
    }

    public Store<LongLongTuple2, PresenceData> getPresenceStore() {
        return presenceStore;
    }

    public LongObjStore<RoleData> getRoleStore() {
        return roleStore;
    }

    public LongObjStore<StageInstanceData> getStageInstanceStore() {
        return this.stageInstanceStore;
    }

    public LongObjStore<UserData> getUserStore() {
        return userStore;
    }

    public Store<LongLongTuple2, VoiceStateData> getVoiceStateStore() {
        return voiceStateStore;
    }

    public Store<LongLongTuple2, ThreadMemberData> getThreadMemberStore() {
        return threadMemberStore;
    }

    public Mono<Void> invalidateStores() {
        return channelStore.invalidate()
                .and(guildStore.invalidate())
                .and(guildEmojiStore.invalidate())
                .and(guildEventsStore.invalidate())
                .and(memberStore.invalidate())
                .and(messageStore.invalidate())
                .and(presenceStore.invalidate())
                .and(roleStore.invalidate())
                .and(stageInstanceStore.invalidate())
                .and(userStore.invalidate())
                .and(voiceStateStore.invalidate())
                .and(threadMemberStore.invalidate());
    }
}
