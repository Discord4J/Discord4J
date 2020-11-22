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
import reactor.core.publisher.Mono;

public class ReactiveEventAdapter {

    public Mono<Void> onPresenceUpdate(PresenceUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onUserUpdate(UserUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onVoiceServerUpdate(VoiceServerUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onVoiceStateUpdate(VoiceStateUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onWebhooksUpdate(WebhooksUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onCategoryCreate(CategoryCreateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onCategoryDelete(CategoryDeleteEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onCategoryUpdate(CategoryUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onNewsChannelCreate(NewsChannelCreateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onNewsChannelDelete(NewsChannelDeleteEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onNewsChannelUpdate(NewsChannelUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onPinsUpdate(PinsUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onPrivateChannelCreate(PrivateChannelCreateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onPrivateChannelDelete(PrivateChannelDeleteEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onStoreChannelCreate(StoreChannelCreateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onStoreChannelDelete(StoreChannelDeleteEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onStoreChannelUpdate(StoreChannelUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onTextChannelCreate(TextChannelCreateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onTextChannelDelete(TextChannelDeleteEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onTextChannelUpdate(TextChannelUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onTypingStart(TypingStartEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onVoiceChannelCreate(VoiceChannelCreateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onVoiceChannelUpdate(VoiceChannelUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onBan(BanEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onEmojisUpdate(EmojisUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onGuildCreate(GuildCreateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onGuildDelete(GuildDeleteEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onGuildUpdate(GuildUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onIntegrationsUpdate(IntegrationsUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onMemberChunk(MemberChunkEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onMemberJoin(MemberJoinEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onMemberLeave(MemberLeaveEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onMemberUpdate(MemberUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onUnban(UnbanEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onConnect(ConnectEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onDisconnect(DisconnectEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onReady(ReadyEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onReconnect(ReconnectEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onReconnectFail(ReconnectFailEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onReconnectStart(ReconnectStartEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onResume(ResumeEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onMessageBulkDelete(MessageBulkDeleteEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onMessageCreate(MessageCreateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onMessageDelete(MessageDeleteEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onMessageUpdate(MessageUpdateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onReactionAdd(ReactionAddEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onReactionRemoveAll(ReactionRemoveAllEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onReactionRemove(ReactionRemoveEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onRoleCreate(RoleCreateEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onRoleDelete(RoleDeleteEvent event) {
        return Mono.empty();
    }

    public Mono<Void> onRoleUpdate(RoleUpdateEvent event) {
        return Mono.empty();
    }

    public final Mono<Void> hookOnEvent(Event event) {
        if (event instanceof PresenceUpdateEvent) return onPresenceUpdate((PresenceUpdateEvent) event);
        else if (event instanceof UserUpdateEvent) return onUserUpdate((UserUpdateEvent) event);
        else if (event instanceof VoiceServerUpdateEvent) return onVoiceServerUpdate((VoiceServerUpdateEvent) event);
        else if (event instanceof VoiceStateUpdateEvent) return onVoiceStateUpdate((VoiceStateUpdateEvent) event);
        else if (event instanceof WebhooksUpdateEvent) return onWebhooksUpdate((WebhooksUpdateEvent) event);
        else if (event instanceof CategoryCreateEvent) return onCategoryCreate((CategoryCreateEvent) event);
        else if (event instanceof CategoryDeleteEvent) return onCategoryDelete((CategoryDeleteEvent) event);
        else if (event instanceof CategoryUpdateEvent) return onCategoryUpdate((CategoryUpdateEvent) event);
        else if (event instanceof NewsChannelCreateEvent) return onNewsChannelCreate((NewsChannelCreateEvent) event);
        else if (event instanceof NewsChannelDeleteEvent) return onNewsChannelDelete((NewsChannelDeleteEvent) event);
        else if (event instanceof NewsChannelUpdateEvent) return onNewsChannelUpdate((NewsChannelUpdateEvent) event);
        else if (event instanceof PinsUpdateEvent) return onPinsUpdate((PinsUpdateEvent) event);
        else if (event instanceof PrivateChannelCreateEvent) return onPrivateChannelCreate((PrivateChannelCreateEvent) event);
        else if (event instanceof PrivateChannelDeleteEvent) return onPrivateChannelDelete((PrivateChannelDeleteEvent) event);
        else if (event instanceof StoreChannelCreateEvent) return onStoreChannelCreate((StoreChannelCreateEvent) event);
        else if (event instanceof StoreChannelDeleteEvent) return onStoreChannelDelete((StoreChannelDeleteEvent) event);
        else if (event instanceof StoreChannelUpdateEvent) return onStoreChannelUpdate((StoreChannelUpdateEvent) event);
        else if (event instanceof TextChannelCreateEvent) return onTextChannelCreate((TextChannelCreateEvent) event);
        else if (event instanceof TextChannelDeleteEvent) return onTextChannelDelete((TextChannelDeleteEvent) event);
        else if (event instanceof TextChannelUpdateEvent) return onTextChannelUpdate((TextChannelUpdateEvent) event);
        else if (event instanceof TypingStartEvent) return onTypingStart((TypingStartEvent) event);
        else if (event instanceof VoiceChannelCreateEvent) return onVoiceChannelCreate((VoiceChannelCreateEvent) event);
        else if (event instanceof VoiceChannelDeleteEvent) return onVoiceChannelDelete((VoiceChannelDeleteEvent) event);
        else if (event instanceof VoiceChannelUpdateEvent) return onVoiceChannelUpdate((VoiceChannelUpdateEvent) event);
        else if (event instanceof BanEvent) return onBan((BanEvent) event);
        else if (event instanceof EmojisUpdateEvent) return onEmojisUpdate((EmojisUpdateEvent) event);
        else if (event instanceof GuildCreateEvent) return onGuildCreate((GuildCreateEvent) event);
        else if (event instanceof GuildDeleteEvent) return onGuildDelete((GuildDeleteEvent) event);
        else if (event instanceof GuildUpdateEvent) return onGuildUpdate((GuildUpdateEvent) event);
        else if (event instanceof IntegrationsUpdateEvent) return onIntegrationsUpdate((IntegrationsUpdateEvent) event);
        else if (event instanceof MemberChunkEvent) return onMemberChunk((MemberChunkEvent) event);
        else if (event instanceof MemberJoinEvent) return onMemberJoin((MemberJoinEvent) event);
        else if (event instanceof MemberLeaveEvent) return onMemberLeave((MemberLeaveEvent) event);
        else if (event instanceof MemberUpdateEvent) return onMemberUpdate((MemberUpdateEvent) event);
        else if (event instanceof UnbanEvent) return onUnban((UnbanEvent) event);
        else if (event instanceof ConnectEvent) return onConnect((ConnectEvent) event);
        else if (event instanceof DisconnectEvent) return onDisconnect((DisconnectEvent) event);
        else if (event instanceof ReadyEvent) return onReady((ReadyEvent) event);
        else if (event instanceof ReconnectEvent) return onReconnect((ReconnectEvent) event);
        else if (event instanceof ReconnectFailEvent) return onReconnectFail((ReconnectFailEvent) event);
        else if (event instanceof ReconnectStartEvent) return onReconnectStart((ReconnectStartEvent) event);
        else if (event instanceof ResumeEvent) return onResume((ResumeEvent) event);
        else if (event instanceof MessageBulkDeleteEvent) return onMessageBulkDelete((MessageBulkDeleteEvent) event);
        else if (event instanceof MessageCreateEvent) return onMessageCreate((MessageCreateEvent) event);
        else if (event instanceof MessageDeleteEvent) return onMessageDelete((MessageDeleteEvent) event);
        else if (event instanceof MessageUpdateEvent) return onMessageUpdate((MessageUpdateEvent) event);
        else if (event instanceof ReactionAddEvent) return onReactionAdd((ReactionAddEvent) event);
        else if (event instanceof ReactionRemoveAllEvent) return onReactionRemoveAll((ReactionRemoveAllEvent) event);
        else if (event instanceof ReactionRemoveEvent) return onReactionRemove((ReactionRemoveEvent) event);
        else if (event instanceof RoleCreateEvent) return onRoleCreate((RoleCreateEvent) event);
        else if (event instanceof RoleDeleteEvent) return onRoleDelete((RoleDeleteEvent) event);
        else if (event instanceof RoleUpdateEvent) return onRoleUpdate((RoleUpdateEvent) event);

        return Mono.empty();
    }
}