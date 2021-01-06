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
import discord4j.discordjson.json.gateway.PresenceUpdate;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.store.api.Store;
import discord4j.store.api.noop.NoOpLongObjStore;
import discord4j.store.api.noop.NoOpStore;
import discord4j.store.api.primitive.LongObjStore;
import discord4j.store.api.service.StoreService;
import discord4j.store.api.util.LongLongTuple2;
import discord4j.store.api.util.StoreContext;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Optional;

/**
 * Holder for various pieces of state for use in caching.
 * <p>
 * In addition to saving the current bot user ID, the following stores are kept in this class:
 * <ul>
 * <li>Channel store: {@code long} keys and {@link ChannelData} values.</li>
 * <li>Guild store: {@code long} keys and {@link GuildData} values.</li>
 * <li>Guild emoji store: {@code long} keys and {@link EmojiData} values.</li>
 * <li>Member store: {@code long} pair keys and {@link MemberData} values.</li>
 * <li>Message store: {@code long} keys and {@link MessageData} values.</li>
 * <li>Presence store: {@code long} pair keys and {@link PresenceUpdate} values.</li>
 * <li>Role store: {@code long} keys and {@link RoleData} values.</li>
 * <li>User store: {@code long} keys and {@link UserData} values.</li>
 * <li>Voice state store: {@code long} pair keys and {@link VoiceStateData} values.</li>
 * </ul>
 */
public final class StateHolder {

    private static final Logger log = Loggers.getLogger(StateHolder.class);

    private final StoreService storeService;
    private final LongObjStore<ChannelData> channelStore;
    private final LongObjStore<GuildData> guildStore;
    private final LongObjStore<EmojiData> guildEmojiStore;
    private final Store<LongLongTuple2, MemberData> memberStore;
    private final LongObjStore<MessageData> messageStore;
    private final Store<LongLongTuple2, PresenceData> presenceStore;
    private final LongObjStore<RoleData> roleStore;
    private final LongObjStore<UserData> userStore;
    private final Store<String, TemplateData> templateStore;
    private final Store<LongLongTuple2, VoiceStateData> voiceStateStore;

    public StateHolder(final StoreService service, final StoreContext context, final Possible<IntentSet> intentSet) {
        storeService = service;

        service.init(context);

        Optional<IntentSet> intents = intentSet.toOptional();

        channelStore = service.provideLongObjStore(ChannelData.class);
        log.debug("Channel storage     : {}", channelStore);

        guildStore = service.provideLongObjStore(GuildData.class);
        log.debug("Guild storage       : {}", guildStore);

        if (intents.map(set -> !set.contains(Intent.GUILD_EMOJIS)).orElse(false)) {
            guildEmojiStore = new NoOpLongObjStore<>();
            log.debug("Guild emoji storage : Disabled due to missing GUILD_EMOJIS intent");
        } else {
            guildEmojiStore = service.provideLongObjStore(EmojiData.class);
            log.debug("Guild emoji storage : {}", guildEmojiStore);
        }

        memberStore = service.provideGenericStore(LongLongTuple2.class, MemberData.class);
        log.debug("Member storage      : {}", memberStore);

        messageStore = service.provideLongObjStore(MessageData.class);
        log.debug("Message storage     : {}", messageStore);

        if (intents.map(set -> !set.contains(Intent.GUILD_PRESENCES)).orElse(false)) {
            presenceStore = new NoOpStore<>();
            log.debug("Presence storage    : Disabled due to missing GUILD_PRESENCES intent");
        } else {
            presenceStore = service.provideGenericStore(LongLongTuple2.class, PresenceData.class);
            log.debug("Presence storage    : {}", presenceStore);
        }

        roleStore = service.provideLongObjStore(RoleData.class);
        log.debug("Role storage        : {}", roleStore);

        userStore = service.provideLongObjStore(UserData.class);
        log.debug("User storage        : {}", userStore);

        templateStore = service.provideGenericStore(String.class, TemplateData.class);
        log.debug("Template storage        : {}", templateStore);

        if (intents.map(set -> !set.contains(Intent.GUILD_VOICE_STATES)).orElse(false)) {
            voiceStateStore = new NoOpStore<>();
            log.debug("Voice state storage : Disabled due to missing GUILD_VOICE_STATES intent");
        } else {
            voiceStateStore = service.provideGenericStore(LongLongTuple2.class, VoiceStateData.class);
            log.debug("Voice state storage : {}", voiceStateStore);
        }
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

    public LongObjStore<UserData> getUserStore() {
        return userStore;
    }

    public Store<String, TemplateData> getTemplateStore() {
        return templateStore;
    }

    public Store<LongLongTuple2, VoiceStateData> getVoiceStateStore() {
        return voiceStateStore;
    }

    public Mono<Void> invalidateStores() {
        return channelStore.invalidate()
                .and(guildStore.invalidate())
                .and(guildEmojiStore.invalidate())
                .and(memberStore.invalidate())
                .and(messageStore.invalidate())
                .and(presenceStore.invalidate())
                .and(roleStore.invalidate())
                .and(userStore.invalidate())
                .and(templateStore.invalidate())
                .and(voiceStateStore.invalidate());
    }
}
