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

package discord4j.core.event;

import discord4j.core.event.domain.*;
import discord4j.core.event.domain.channel.*;
import discord4j.core.event.domain.guild.*;
import discord4j.core.event.domain.lifecycle.*;
import discord4j.core.event.domain.message.*;
import discord4j.core.event.domain.role.RoleCreateEvent;
import discord4j.core.event.domain.role.RoleDeleteEvent;
import discord4j.core.event.domain.role.RoleUpdateEvent;
import reactor.core.publisher.BaseSubscriber;

public class EventSubscriberAdapter extends BaseSubscriber<Event> {

    public void onPresenceUpdate(PresenceUpdateEvent event) {

    }

    public void onUserUpdate(UserUpdateEvent event) {

    }

    public void onVoiceServerUpdate(VoiceServerUpdateEvent event) {

    }

    public void onVoiceStateUpdate(VoiceStateUpdateEvent event) {

    }

    public void onWebhooksUpdate(WebhooksUpdateEvent event) {

    }

    public void onCategoryCreate(CategoryCreateEvent event) {

    }

    public void onCategoryDelete(CategoryDeleteEvent event) {

    }

    public void onCategoryUpdate(CategoryUpdateEvent event) {

    }

    public void onNewsChannelCreate(NewsChannelCreateEvent event) {

    }

    public void onNewsChannelDelete(NewsChannelDeleteEvent event) {

    }

    public void onNewsChannelUpdate(NewsChannelUpdateEvent event) {

    }

    public void onPinsUpdate(PinsUpdateEvent event) {

    }

    public void onPrivateChannelCreate(PrivateChannelCreateEvent event) {

    }

    public void onPrivateChannelDelete(PrivateChannelDeleteEvent event) {

    }

    public void onStoreChannelCreate(StoreChannelCreateEvent event) {

    }

    public void onStoreChannelDelete(StoreChannelDeleteEvent event) {

    }

    public void onStoreChannelUpdate(StoreChannelUpdateEvent event) {

    }

    public void onTextChannelCreate(TextChannelCreateEvent event) {

    }

    public void onTextChannelDelete(TextChannelDeleteEvent event) {

    }

    public void onTextChannelUpdate(TextChannelUpdateEvent event) {

    }

    public void onTypingStart(TypingStartEvent event) {

    }

    public void onVoiceChannelCreate(VoiceChannelCreateEvent event) {

    }

    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {

    }

    public void onVoiceChannelUpdate(VoiceChannelUpdateEvent event) {

    }

    public void onBan(BanEvent event) {

    }

    public void onEmojisUpdate(EmojisUpdateEvent event) {

    }

    public void onGuildCreate(GuildCreateEvent event) {

    }

    public void onGuildDelete(GuildDeleteEvent event) {

    }

    public void onGuildUpdate(GuildUpdateEvent event) {

    }

    public void onIntegrationsUpdate(IntegrationsUpdateEvent event) {

    }

    public void onMemberChunk(MemberChunkEvent event) {

    }

    public void onMemberJoin(MemberJoinEvent event) {

    }

    public void onMemberLeave(MemberLeaveEvent event) {

    }

    public void onMemberUpdate(MemberUpdateEvent event) {

    }

    public void onUnban(UnbanEvent event) {

    }

    public void onConnect(ConnectEvent event) {

    }

    public void onDisconnect(DisconnectEvent event) {

    }

    public void onReady(ReadyEvent event) {

    }

    public void onReconnect(ReconnectEvent event) {

    }

    public void onReconnectFail(ReconnectFailEvent event) {

    }

    public void onReconnectStart(ReconnectStartEvent event) {

    }

    public void onResume(ResumeEvent event) {

    }

    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {

    }

    public void onMessageCreate(MessageCreateEvent event) {

    }

    public void onMessageDelete(MessageDeleteEvent event) {

    }

    public void onMessageUpdate(MessageUpdateEvent event) {

    }

    public void onReactionAdd(ReactionAddEvent event) {

    }

    public void onReactionRemoveAll(ReactionRemoveAllEvent event) {

    }

    public void onReactionRemove(ReactionRemoveEvent event) {

    }

    public void onRoleCreate(RoleCreateEvent event) {

    }

    public void onRoleDelete(RoleDeleteEvent event) {

    }

    public void onRoleUpdate(RoleUpdateEvent event) {

    }

    public final void hookOnEvent(Event event) {
        if (event instanceof PresenceUpdateEvent) onPresenceUpdate((PresenceUpdateEvent) event);
        else if (event instanceof UserUpdateEvent) onUserUpdate((UserUpdateEvent) event);
        else if (event instanceof VoiceServerUpdateEvent) onVoiceServerUpdate((VoiceServerUpdateEvent) event);
        else if (event instanceof VoiceStateUpdateEvent) onVoiceStateUpdate((VoiceStateUpdateEvent) event);
        else if (event instanceof WebhooksUpdateEvent) onWebhooksUpdate((WebhooksUpdateEvent) event);
        else if (event instanceof CategoryCreateEvent) onCategoryCreate((CategoryCreateEvent) event);
        else if (event instanceof CategoryDeleteEvent) onCategoryDelete((CategoryDeleteEvent) event);
        else if (event instanceof CategoryUpdateEvent) onCategoryUpdate((CategoryUpdateEvent) event);
        else if (event instanceof NewsChannelCreateEvent) onNewsChannelCreate((NewsChannelCreateEvent) event);
        else if (event instanceof NewsChannelDeleteEvent) onNewsChannelDelete((NewsChannelDeleteEvent) event);
        else if (event instanceof NewsChannelUpdateEvent) onNewsChannelUpdate((NewsChannelUpdateEvent) event);
        else if (event instanceof PinsUpdateEvent) onPinsUpdate((PinsUpdateEvent) event);
        else if (event instanceof PrivateChannelCreateEvent) onPrivateChannelCreate((PrivateChannelCreateEvent) event);
        else if (event instanceof PrivateChannelDeleteEvent) onPrivateChannelDelete((PrivateChannelDeleteEvent) event);
        else if (event instanceof StoreChannelCreateEvent) onStoreChannelCreate((StoreChannelCreateEvent) event);
        else if (event instanceof StoreChannelDeleteEvent) onStoreChannelDelete((StoreChannelDeleteEvent) event);
        else if (event instanceof StoreChannelUpdateEvent) onStoreChannelUpdate((StoreChannelUpdateEvent) event);
        else if (event instanceof TextChannelCreateEvent) onTextChannelCreate((TextChannelCreateEvent) event);
        else if (event instanceof TextChannelDeleteEvent) onTextChannelDelete((TextChannelDeleteEvent) event);
        else if (event instanceof TextChannelUpdateEvent) onTextChannelUpdate((TextChannelUpdateEvent) event);
        else if (event instanceof TypingStartEvent) onTypingStart((TypingStartEvent) event);
        else if (event instanceof VoiceChannelCreateEvent) onVoiceChannelCreate((VoiceChannelCreateEvent) event);
        else if (event instanceof VoiceChannelDeleteEvent) onVoiceChannelDelete((VoiceChannelDeleteEvent) event);
        else if (event instanceof VoiceChannelUpdateEvent) onVoiceChannelUpdate((VoiceChannelUpdateEvent) event);
        else if (event instanceof BanEvent) onBan((BanEvent) event);
        else if (event instanceof EmojisUpdateEvent) onEmojisUpdate((EmojisUpdateEvent) event);
        else if (event instanceof GuildCreateEvent) onGuildCreate((GuildCreateEvent) event);
        else if (event instanceof GuildDeleteEvent) onGuildDelete((GuildDeleteEvent) event);
        else if (event instanceof GuildUpdateEvent) onGuildUpdate((GuildUpdateEvent) event);
        else if (event instanceof IntegrationsUpdateEvent) onIntegrationsUpdate((IntegrationsUpdateEvent) event);
        else if (event instanceof MemberChunkEvent) onMemberChunk((MemberChunkEvent) event);
        else if (event instanceof MemberJoinEvent) onMemberJoin((MemberJoinEvent) event);
        else if (event instanceof MemberLeaveEvent) onMemberLeave((MemberLeaveEvent) event);
        else if (event instanceof MemberUpdateEvent) onMemberUpdate((MemberUpdateEvent) event);
        else if (event instanceof UnbanEvent) onUnban((UnbanEvent) event);
        else if (event instanceof ConnectEvent) onConnect((ConnectEvent) event);
        else if (event instanceof DisconnectEvent) onDisconnect((DisconnectEvent) event);
        else if (event instanceof ReadyEvent) onReady((ReadyEvent) event);
        else if (event instanceof ReconnectEvent) onReconnect((ReconnectEvent) event);
        else if (event instanceof ReconnectFailEvent) onReconnectFail((ReconnectFailEvent) event);
        else if (event instanceof ReconnectStartEvent) onReconnectStart((ReconnectStartEvent) event);
        else if (event instanceof ResumeEvent) onResume((ResumeEvent) event);
        else if (event instanceof MessageBulkDeleteEvent) onMessageBulkDelete((MessageBulkDeleteEvent) event);
        else if (event instanceof MessageCreateEvent) onMessageCreate((MessageCreateEvent) event);
        else if (event instanceof MessageDeleteEvent) onMessageDelete((MessageDeleteEvent) event);
        else if (event instanceof MessageUpdateEvent) onMessageUpdate((MessageUpdateEvent) event);
        else if (event instanceof ReactionAddEvent) onReactionAdd((ReactionAddEvent) event);
        else if (event instanceof ReactionRemoveAllEvent) onReactionRemoveAll((ReactionRemoveAllEvent) event);
        else if (event instanceof ReactionRemoveEvent) onReactionRemove((ReactionRemoveEvent) event);
        else if (event instanceof RoleCreateEvent) onRoleCreate((RoleCreateEvent) event);
        else if (event instanceof RoleDeleteEvent) onRoleDelete((RoleDeleteEvent) event);
        else if (event instanceof RoleUpdateEvent) onRoleUpdate((RoleUpdateEvent) event);
    }

    @Override
    protected void hookOnNext(Event event) {
        hookOnEvent(event);
    }
}