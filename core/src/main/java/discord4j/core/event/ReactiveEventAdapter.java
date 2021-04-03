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
import discord4j.core.event.domain.command.ApplicationCommandCreateEvent;
import discord4j.core.event.domain.command.ApplicationCommandDeleteEvent;
import discord4j.core.event.domain.command.ApplicationCommandUpdateEvent;
import discord4j.core.event.domain.guild.*;
import discord4j.core.event.domain.lifecycle.*;
import discord4j.core.event.domain.message.*;
import discord4j.core.event.domain.role.RoleCreateEvent;
import discord4j.core.event.domain.role.RoleDeleteEvent;
import discord4j.core.event.domain.role.RoleUpdateEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * An adapter class to attach reactive listeners to each event type. Create a new instance to subclass it and
 * override one or more event methods. See {@link Event} class for more details.
 */
public abstract class ReactiveEventAdapter {

    // ================= Gateway lifecycle events ================= //

    /**
     * Invoked as Discord has established a fresh Gateway session. This event can be used to track the bot connection
     * details and contains the initial state required to operate with the real-time Gateway. See {@link ReadyEvent}
     * and Discord documentation for more details about this event.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onReady(ReadyEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when the gateway connection is successfully resumed.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onResume(ResumeEvent event) {
        return Mono.empty();
    }

    // ================= Message related events ================= //

    /**
     * Invoked when a message is sent in a message channel.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onMessageCreate(MessageCreateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a message is deleted.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onMessageDelete(MessageDeleteEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a message is updated. This event includes both normal message editing as well as the following
     * behavior regarding embeds: When a message with a link is sent, it does not initially contain its embed. When
     * Discord creates the embed, this event is fired with it added to the embeds list.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onMessageUpdate(MessageUpdateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when multiple messages are deleted at once.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onMessageBulkDelete(MessageBulkDeleteEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a reaction is added to a message. Guild ID might be missing if this event fires for a DM channel.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onReactionAdd(ReactionAddEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a reaction is removed from a message. Guild ID might be missing if this event fires for a DM
     * channel.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onReactionRemove(ReactionRemoveEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when the reactions for one emoji are removed from a message. Guild ID might be missing if this event
     * fires for a DM channel.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onReactionRemoveEmoji(ReactionRemoveEmojiEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when all of the reactions on a message are removed. Guild ID might be missing if this event fires for
     * a DM channel.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onReactionRemoveAll(ReactionRemoveAllEvent event) {
        return Mono.empty();
    }

    // ========== Application Command related events ========== //

    /**
     * Invoked when an application command relevant to the current user is created. Guild ID might be missing
     * if this event fires for a DM channel.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onApplicationCommandCreate(ApplicationCommandCreateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when an application command relevant to the current user is updated. Guild ID might be missing
     * if this event fires for a DM channel.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onApplicationCommandUpdate(ApplicationCommandUpdateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when an application command relevant to the current user is deleted. Guild ID might be missing
     * if this event fires for a DM channel.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onApplicationCommandDelete(ApplicationCommandDeleteEvent event) {
        return Mono.empty();
    }

    // ================= Guild related events ================= //

    /**
     * Invoked when the bot receives initial information on startup, after it joins a guild, or after an outage is
     * resolved.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onGuildCreate(GuildCreateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when the bot leaves or is kicked from a guild, or if a guild has become unavailable due to an outage.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onGuildDelete(GuildDeleteEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild is updated.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onGuildUpdate(GuildUpdateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a user joins a guild.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onMemberJoin(MemberJoinEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a user leaves a guild, or is kicked from it.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onMemberLeave(MemberLeaveEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a user's nickname or roles change in a guild.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onMemberUpdate(MemberUpdateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when members are streamed to the client from Discord.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onMemberChunk(MemberChunkEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when an emoji is added, deleted or edited in a guild. The emojis set includes ALL emojis of the guild.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onEmojisUpdate(EmojisUpdateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a user is banned from a guild.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onBan(BanEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a user is unbanned from a guild.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onUnban(UnbanEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when guild integrations are updated.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onIntegrationsUpdate(IntegrationsUpdateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a webhook is updated in a guild. Discord does not send any information about what was
     * actually updated. This is simply a notification of some update.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onWebhooksUpdate(WebhooksUpdateEvent event) {
        return Mono.empty();
    }

    // ================= Channel related events ================= //

    /**
     * Invoked when a guild text channel is created.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onTextChannelCreate(TextChannelCreateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild text channel is deleted.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onTextChannelDelete(TextChannelDeleteEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild text channel is updated.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onTextChannelUpdate(TextChannelUpdateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild voice channel is created.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onVoiceChannelCreate(VoiceChannelCreateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild voice channel is deleted.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild voice channel is updated.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onVoiceChannelUpdate(VoiceChannelUpdateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild channel category is created.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onCategoryCreate(CategoryCreateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild channel category is deleted.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onCategoryDelete(CategoryDeleteEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild channel category is updated.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onCategoryUpdate(CategoryUpdateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild news channel is created.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onNewsChannelCreate(NewsChannelCreateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild news channel is deleted.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onNewsChannelDelete(NewsChannelDeleteEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild news channel is updated.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onNewsChannelUpdate(NewsChannelUpdateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild store channel is created.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onStoreChannelCreate(StoreChannelCreateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild store channel is deleted.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onStoreChannelDelete(StoreChannelDeleteEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild store channel is updated.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onStoreChannelUpdate(StoreChannelUpdateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a user has started typing a message.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onTypingStart(TypingStartEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a channel's pinned messages are updated.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onPinsUpdate(PinsUpdateEvent event) {
        return Mono.empty();
    }

    // ================= Role related events ================= //

    /**
     * Invoked when a role is created in a guild.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onRoleCreate(RoleCreateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a role is deleted from a guild.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onRoleDelete(RoleDeleteEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when one or more role's properties are updated in a guild.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onRoleUpdate(RoleUpdateEvent event) {
        return Mono.empty();
    }

    // ================= Invite related events ================= //

    /**
     * Invoked when an invite to a channel is created.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onInviteCreate(InviteCreateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when an invite to a channel has expired.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onInviteDelete(InviteDeleteEvent event) {
        return Mono.empty();
    }

    // ================= User related events ================= //

    /**
     * Invoked when one or more user's properties were updated.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onUserUpdate(UserUpdateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a user's presence or status has changed.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onPresenceUpdate(PresenceUpdateEvent event) {
        return Mono.empty();
    }

    // ================= Voice connections related events ================= //

    /**
     * Invoked when a user's connected voice channel and status, was requested or has updated.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onVoiceStateUpdate(VoiceStateUpdateEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a guild's voice server is requested or is updated.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onVoiceServerUpdate(VoiceServerUpdateEvent event) {
        return Mono.empty();
    }

    // ================= Connection lifecycle events ================= //

    /**
     * Invoked when connecting to the Gateway for the first time only.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onConnect(ConnectEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when subsequent reconnections to the Gateway, either through resumption or full retry.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onReconnect(ReconnectEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when the bot has disconnected from the Gateway.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onDisconnect(DisconnectEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a resumption or reconnection attempt has begun.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onReconnectStart(ReconnectStartEvent event) {
        return Mono.empty();
    }

    /**
     * Invoked when a resumption or reconnection attempt has failed but can be retried.
     *
     * @param event the event instance
     * @return a {@link Publisher} that completes when this listener has done processing the event, for example,
     * returning any {@link Mono}, {@link Flux} or synchronous code using {@link Mono#fromRunnable(Runnable)}.
     */
    public Publisher<?> onReconnectFail(ReconnectFailEvent event) {
        return Mono.empty();
    }

    // ================= Interactions events ================= //

    public Publisher<?> onInteractionCreate(InteractionCreateEvent event) {
        return Mono.empty();
    }


    // ================= Core methods ================= //

    /**
     * Create a composite {@link ReactiveEventAdapter} from multiple adapters.
     *
     * @param adapters an array of adapters to combine
     * @return a composite adapter
     */
    public static ReactiveEventAdapter from(ReactiveEventAdapter... adapters) {
        return new CompositeReactiveEventAdapter(adapters);
    }

    public Publisher<?> hookOnEvent(Event event) {
        // @formatter:off
        if (event instanceof ReadyEvent) return onReady((ReadyEvent) event);
        else if (event instanceof ResumeEvent) return onResume((ResumeEvent) event);
        else if (event instanceof MessageCreateEvent) return onMessageCreate((MessageCreateEvent) event);
        else if (event instanceof MessageDeleteEvent) return onMessageDelete((MessageDeleteEvent) event);
        else if (event instanceof MessageUpdateEvent) return onMessageUpdate((MessageUpdateEvent) event);
        else if (event instanceof MessageBulkDeleteEvent) return onMessageBulkDelete((MessageBulkDeleteEvent) event);
        else if (event instanceof ReactionAddEvent) return onReactionAdd((ReactionAddEvent) event);
        else if (event instanceof ReactionRemoveEvent) return onReactionRemove((ReactionRemoveEvent) event);
        else if (event instanceof ReactionRemoveEmojiEvent) return onReactionRemoveEmoji((ReactionRemoveEmojiEvent) event);
        else if (event instanceof ReactionRemoveAllEvent) return onReactionRemoveAll((ReactionRemoveAllEvent) event);
        else if (event instanceof GuildCreateEvent) return onGuildCreate((GuildCreateEvent) event);
        else if (event instanceof GuildDeleteEvent) return onGuildDelete((GuildDeleteEvent) event);
        else if (event instanceof GuildUpdateEvent) return onGuildUpdate((GuildUpdateEvent) event);
        else if (event instanceof MemberJoinEvent) return onMemberJoin((MemberJoinEvent) event);
        else if (event instanceof MemberLeaveEvent) return onMemberLeave((MemberLeaveEvent) event);
        else if (event instanceof MemberUpdateEvent) return onMemberUpdate((MemberUpdateEvent) event);
        else if (event instanceof MemberChunkEvent) return onMemberChunk((MemberChunkEvent) event);
        else if (event instanceof EmojisUpdateEvent) return onEmojisUpdate((EmojisUpdateEvent) event);
        else if (event instanceof BanEvent) return onBan((BanEvent) event);
        else if (event instanceof UnbanEvent) return onUnban((UnbanEvent) event);
        else if (event instanceof IntegrationsUpdateEvent) return onIntegrationsUpdate((IntegrationsUpdateEvent) event);
        else if (event instanceof WebhooksUpdateEvent) return onWebhooksUpdate((WebhooksUpdateEvent) event);
        else if (event instanceof TextChannelCreateEvent) return onTextChannelCreate((TextChannelCreateEvent) event);
        else if (event instanceof TextChannelDeleteEvent) return onTextChannelDelete((TextChannelDeleteEvent) event);
        else if (event instanceof TextChannelUpdateEvent) return onTextChannelUpdate((TextChannelUpdateEvent) event);
        else if (event instanceof VoiceChannelCreateEvent) return onVoiceChannelCreate((VoiceChannelCreateEvent) event);
        else if (event instanceof VoiceChannelDeleteEvent) return onVoiceChannelDelete((VoiceChannelDeleteEvent) event);
        else if (event instanceof VoiceChannelUpdateEvent) return onVoiceChannelUpdate((VoiceChannelUpdateEvent) event);
        else if (event instanceof CategoryCreateEvent) return onCategoryCreate((CategoryCreateEvent) event);
        else if (event instanceof CategoryDeleteEvent) return onCategoryDelete((CategoryDeleteEvent) event);
        else if (event instanceof CategoryUpdateEvent) return onCategoryUpdate((CategoryUpdateEvent) event);
        else if (event instanceof NewsChannelCreateEvent) return onNewsChannelCreate((NewsChannelCreateEvent) event);
        else if (event instanceof NewsChannelDeleteEvent) return onNewsChannelDelete((NewsChannelDeleteEvent) event);
        else if (event instanceof NewsChannelUpdateEvent) return onNewsChannelUpdate((NewsChannelUpdateEvent) event);
        else if (event instanceof StoreChannelCreateEvent) return onStoreChannelCreate((StoreChannelCreateEvent) event);
        else if (event instanceof StoreChannelDeleteEvent) return onStoreChannelDelete((StoreChannelDeleteEvent) event);
        else if (event instanceof StoreChannelUpdateEvent) return onStoreChannelUpdate((StoreChannelUpdateEvent) event);
        else if (event instanceof TypingStartEvent) return onTypingStart((TypingStartEvent) event);
        else if (event instanceof PinsUpdateEvent) return onPinsUpdate((PinsUpdateEvent) event);
        else if (event instanceof RoleCreateEvent) return onRoleCreate((RoleCreateEvent) event);
        else if (event instanceof RoleDeleteEvent) return onRoleDelete((RoleDeleteEvent) event);
        else if (event instanceof RoleUpdateEvent) return onRoleUpdate((RoleUpdateEvent) event);
        else if (event instanceof InviteCreateEvent) return onInviteCreate((InviteCreateEvent) event);
        else if (event instanceof InviteDeleteEvent) return onInviteDelete((InviteDeleteEvent) event);
        else if (event instanceof UserUpdateEvent) return onUserUpdate((UserUpdateEvent) event);
        else if (event instanceof PresenceUpdateEvent) return onPresenceUpdate((PresenceUpdateEvent) event);
        else if (event instanceof VoiceStateUpdateEvent) return onVoiceStateUpdate((VoiceStateUpdateEvent) event);
        else if (event instanceof VoiceServerUpdateEvent) return onVoiceServerUpdate((VoiceServerUpdateEvent) event);
        else if (event instanceof ConnectEvent) return onConnect((ConnectEvent) event);
        else if (event instanceof ReconnectEvent) return onReconnect((ReconnectEvent) event);
        else if (event instanceof DisconnectEvent) return onDisconnect((DisconnectEvent) event);
        else if (event instanceof ReconnectStartEvent) return onReconnectStart((ReconnectStartEvent) event);
        else if (event instanceof ReconnectFailEvent) return onReconnectFail((ReconnectFailEvent) event);
        else if (event instanceof InteractionCreateEvent) return onInteractionCreate((InteractionCreateEvent) event);
        else if (event instanceof ApplicationCommandCreateEvent) return onApplicationCommandCreate((ApplicationCommandCreateEvent) event);
        else if (event instanceof ApplicationCommandUpdateEvent) return onApplicationCommandUpdate((ApplicationCommandUpdateEvent) event);
        else if (event instanceof ApplicationCommandDeleteEvent) return onApplicationCommandDelete((ApplicationCommandDeleteEvent) event);
        // @formatter:on

        return Mono.empty();
    }

    private static class CompositeReactiveEventAdapter extends ReactiveEventAdapter {

        private final ReactiveEventAdapter[] adapters;

        public CompositeReactiveEventAdapter(ReactiveEventAdapter... adapters) {
            this.adapters = adapters;
        }

        @Override
        public Publisher<?> hookOnEvent(Event event) {
            return Flux.fromArray(adapters).flatMap(it -> it.hookOnEvent(event));
        }
    }
}
