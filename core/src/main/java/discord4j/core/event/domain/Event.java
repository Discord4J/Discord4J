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

package discord4j.core.event.domain;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.channel.*;
import discord4j.core.event.domain.guild.*;
import discord4j.core.event.domain.interaction.*;
import discord4j.core.event.domain.lifecycle.*;
import discord4j.core.event.domain.message.*;
import discord4j.core.event.domain.monetization.*;
import discord4j.core.event.domain.poll.*;
import discord4j.core.event.domain.role.RoleCreateEvent;
import discord4j.core.event.domain.role.RoleDeleteEvent;
import discord4j.core.event.domain.role.RoleUpdateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.intent.Intent;

/**
 * Represents a Discord real-time event used to track a client's state.
 *
 * <h2>Gateway lifecycle events</h2>
 * <p>
 * These events are specifically sent by Discord as real-time payload.
 *
 * <ul>
 *     <li>{@link ReadyEvent}: Discord has established a fresh Gateway session</li>
 *     <li>{@link ResumeEvent}: specifically sent after successful resumption</li>
 * </ul>
 *
 * <h2>Message related events</h2>
 *
 * <ul>
 *     <li>{@link MessageCreateEvent}: a guild or DM message was created</li>
 *     <li>{@link MessageDeleteEvent}: a message was deleted</li>
 *     <li>{@link MessageUpdateEvent}: a message's content was updated</li>
 *     <li>{@link MessageBulkDeleteEvent}: a batch of messages were deleted at once</li>
 *     <li>{@link ReactionAddEvent}: a reaction was added to a message</li>
 *     <li>{@link ReactionRemoveEvent}: a reaction was removed from a message</li>
 *     <li>{@link ReactionRemoveEmojiEvent}: reactions for a given emoji were removed from a message</li>
 *     <li>{@link ReactionRemoveAllEvent}: all reactions were removed from a message</li>
 * </ul>
 *
 * <h2>Interaction related events</h2>
 *
 * <ul>
 *     <li>{@link ChatInputInteractionEvent}: user interacted with a slash command</li>
 *     <li>{@link MessageInteractionEvent}: user interacted with a message context menu</li>
 *     <li>{@link UserInteractionEvent}: user interacted with a user context menu</li>
 *     <li>{@link ApplicationCommandInteractionEvent}: user interacted with an application command</li>
 *     <li>{@link SelectMenuInteractionEvent}: user interacted with a select menu component</li>
 *     <li>{@link ButtonInteractionEvent}: user interacted with a button component</li>
 *     <li>{@link ComponentInteractionEvent}: user interacted with a component</li>
 *     <li>{@link InteractionCreateEvent}: base for all Gateway interaction events</li>
 * </ul>
 *
 * <h2>Guild related events</h2>
 *
 * <ul>
 *     <li>{@link GuildCreateEvent}: a startup event providing initial {@link Guild} details, content depends on
 *     {@link Intent} configuration</li>
 *     <li>{@link GuildDeleteEvent}: after being kicked from a guild, after leaving a guild or after a guild becoming
 *     unavailable due to outage</li>
 *     <li>{@link GuildUpdateEvent}: a guild has changed one or more properties</li>
 *     <li>{@link MemberJoinEvent}: a user has joined a guild</li>
 *     <li>{@link MemberLeaveEvent}: a user has left or was kicked from a guild</li>
 *     <li>{@link MemberUpdateEvent}: a user had their nickname and/or roles change</li>
 *     <li>{@link MemberChunkEvent}: a batch of a guild's member details</li>
 *     <li>{@link EmojisUpdateEvent}: a guild's emoji list was updated</li>
 *     <li>{@link StickersUpdateEvent}: a guild's sticker list was updated</li>
 *     <li>{@link BanEvent}: a user was banned from a guild</li>
 *     <li>{@link UnbanEvent}: a user's ban was removed from a guild</li>
 *     <li>{@link IntegrationsUpdateEvent}: a guild has updated their app integrations</li>
 *     <li>{@link WebhooksUpdateEvent}: a guild's webhooks were updated</li>
 * </ul>
 *
 * <h2>Channel related events</h2>
 *
 * <ul>
 *     <li>Channel creation events: {@link TextChannelCreateEvent}, {@link VoiceChannelCreateEvent},
 *     {@link CategoryCreateEvent}, {@link NewsChannelCreateEvent}, {@link StoreChannelCreateEvent}</li>
 *     <li>Channel deletion events: {@link TextChannelDeleteEvent}, {@link VoiceChannelDeleteEvent},
 *     {@link CategoryDeleteEvent}, {@link NewsChannelDeleteEvent}, {@link StoreChannelDeleteEvent}</li>
 *     <li>Channel update events: {@link TextChannelUpdateEvent}, {@link VoiceChannelUpdateEvent},
 *     {@link CategoryUpdateEvent}, {@link NewsChannelUpdateEvent}, {@link StoreChannelUpdateEvent}</li>
 *     <li>{@link TypingStartEvent}: as a user has started typing a message</li>
 *     <li>{@link PinsUpdateEvent}: a channel's pinned messages were updated</li>
 * </ul>
 *
 * <h2>Role related events</h2>
 *
 * <ul>
 *     <li>{@link RoleCreateEvent}: a role was created in a guild</li>
 *     <li>{@link RoleDeleteEvent}: a role was deleted from a guild</li>
 *     <li>{@link RoleUpdateEvent}: one or more role's properties were updated in a guild</li>
 * </ul>
 *
 * <h2>Invite related events</h2>
 *
 * <ul>
 *     <li>{@link InviteCreateEvent}: an invite to a channel was created</li>
 *     <li>{@link InviteDeleteEvent}: an invite to a channel has expired</li>
 * </ul>
 *
 * <h2>User related events</h2>
 *
 * <ul>
 *     <li>{@link UserUpdateEvent}: one or more user's properties were updated</li>
 *     <li>{@link PresenceUpdateEvent}: a user's presence or status has changed</li>
 * </ul>
 *
 * <h2>Voice connections related events</h2>
 *
 * <ul>
 *     <li>{@link VoiceStateUpdateEvent}: a user's connected voice channel and status, was requested or has updated</li>
 *     <li>{@link VoiceServerUpdateEvent}: a guild's voice server was requested or has updated</li>
 * </ul>
 *
 * <h2>Poll related events</h2>
 *
 * <ul>
 *     <li>{@link PollVoteAddEvent}: a user added a vote to a poll</li>
 *     <li>{@link PollVoteRemoveEvent}: a user removed a vote from a poll</li>
 * </ul>
 *
 * <h2>Connection lifecycle events</h2>
 * <p>
 * These events are derived by Discord4J according to the status of the websocket lifecycle.
 *
 * <ul>
 *     <li>{@link ConnectEvent}: connected to the Gateway for the first time only</li>
 *     <li>{@link ReconnectEvent}: subsequent reconnections to the Gateway, either through resumption or full retry</li>
 *     <li>{@link DisconnectEvent}: disconnected from the Gateway</li>
 *     <li>{@link ReconnectStartEvent}: a resumption or reconnection attempt has begun</li>
 *     <li>{@link ReconnectFailEvent}: a resumption or reconnection attempt has failed but can be retried</li>
 *     <li>{@link SessionInvalidatedEvent}: a gateway session has been invalidated</li>
 * </ul>
 *
 * <h2>Monetization related events</h2>
 *
 * <ul>
 *     <li>{@link EntitlementCreateEvent}: a new entitlement was created</li>
 *     <li>{@link EntitlementDeleteEvent}: an entitlement was deleted</li>
 *     <li>{@link EntitlementUpdateEvent}: an entitlement was updated</li>
 * </ul>
 */
public abstract class Event {

    private final GatewayDiscordClient gateway;
    private final ShardInfo shardInfo;

    protected Event(GatewayDiscordClient gateway, ShardInfo shardInfo) {
        this.gateway = gateway;
        this.shardInfo = shardInfo;
    }

    /**
     * Get the {@link GatewayDiscordClient} that emitted this {@link Event}.
     *
     * @return The client emitting this event.
     */
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Retrieve the shard details from this {@link Event}.
     *
     * @return a {@link ShardInfo} object reflecting index and count for this event.
     */
    public ShardInfo getShardInfo() {
        return shardInfo;
    }
}
