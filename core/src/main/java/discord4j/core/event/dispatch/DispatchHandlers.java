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
package discord4j.core.event.dispatch;

import discord4j.common.store.api.object.PresenceAndUserData;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.*;
import discord4j.core.event.domain.automod.AutoModActionExecutedEvent;
import discord4j.core.event.domain.automod.AutoModRuleCreateEvent;
import discord4j.core.event.domain.automod.AutoModRuleDeleteEvent;
import discord4j.core.event.domain.automod.AutoModRuleUpdateEvent;
import discord4j.core.event.domain.channel.TypingStartEvent;
import discord4j.core.event.domain.integration.IntegrationCreateEvent;
import discord4j.core.event.domain.integration.IntegrationDeleteEvent;
import discord4j.core.event.domain.integration.IntegrationUpdateEvent;
import discord4j.core.event.domain.interaction.*;
import discord4j.core.object.VoiceState;
import discord4j.core.object.audit.AuditLogEntry;
import discord4j.core.object.automod.AutoModRule;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.component.MessageComponent;
import discord4j.core.object.entity.Integration;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Presence;
import discord4j.discordjson.json.PartialUserData;
import discord4j.discordjson.json.PresenceData;
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.json.VoiceStateData;
import discord4j.discordjson.json.gateway.*;
import discord4j.gateway.retry.GatewayStateChange;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for {@link Dispatch} to {@link Event} mapping operations.
 */
public class DispatchHandlers implements DispatchEventMapper {

    private static final Map<Class<?>, DispatchHandler<?, ?, ?>> handlerMap = new HashMap<>();

    static {
        addHandler(ChannelCreate.class, ChannelDispatchHandlers::channelCreate);
        addHandler(ChannelDelete.class, ChannelDispatchHandlers::channelDelete);
        addHandler(ChannelPinsUpdate.class, ChannelDispatchHandlers::channelPinsUpdate);
        addHandler(ChannelUpdate.class, ChannelDispatchHandlers::channelUpdate);
        addHandler(AuditLogEntryCreate.class, DispatchHandlers::auditLogEntryCreate);
        addHandler(GuildBanAdd.class, GuildDispatchHandlers::guildBanAdd);
        addHandler(GuildBanRemove.class, GuildDispatchHandlers::guildBanRemove);
        addHandler(GuildCreate.class, GuildDispatchHandlers::guildCreate);
        addHandler(GuildDelete.class, GuildDispatchHandlers::guildDelete);
        addHandler(GuildEmojisUpdate.class, GuildDispatchHandlers::guildEmojisUpdate);
        addHandler(GuildIntegrationsUpdate.class, GuildDispatchHandlers::guildIntegrationsUpdate);
        addHandler(GuildMemberAdd.class, GuildDispatchHandlers::guildMemberAdd);
        addHandler(GuildMemberRemove.class, GuildDispatchHandlers::guildMemberRemove);
        addHandler(GuildMembersChunk.class, GuildDispatchHandlers::guildMembersChunk);
        addHandler(GuildMemberUpdate.class, GuildDispatchHandlers::guildMemberUpdate);
        addHandler(GuildRoleCreate.class, GuildDispatchHandlers::guildRoleCreate);
        addHandler(GuildRoleDelete.class, GuildDispatchHandlers::guildRoleDelete);
        addHandler(GuildRoleUpdate.class, GuildDispatchHandlers::guildRoleUpdate);
        addHandler(GuildScheduledEventCreate.class, GuildDispatchHandlers::scheduledEventCreate);
        addHandler(GuildScheduledEventUpdate.class, GuildDispatchHandlers::scheduledEventUpdate);
        addHandler(GuildScheduledEventDelete.class, GuildDispatchHandlers::scheduledEventDelete);
        addHandler(GuildScheduledEventUserAdd.class, GuildDispatchHandlers::scheduledEventUserAdd);
        addHandler(GuildScheduledEventUserRemove.class, GuildDispatchHandlers::scheduledEventUserRemove);
        addHandler(GuildUpdate.class, GuildDispatchHandlers::guildUpdate);
        addHandler(MessageCreate.class, MessageDispatchHandlers::messageCreate);
        addHandler(MessageDelete.class, MessageDispatchHandlers::messageDelete);
        addHandler(MessageDeleteBulk.class, MessageDispatchHandlers::messageDeleteBulk);
        addHandler(MessageReactionAdd.class, MessageDispatchHandlers::messageReactionAdd);
        addHandler(MessageReactionRemove.class, MessageDispatchHandlers::messageReactionRemove);
        addHandler(MessageReactionRemoveEmoji.class, MessageDispatchHandlers::messageReactionRemoveEmoji);
        addHandler(MessageReactionRemoveAll.class, MessageDispatchHandlers::messageReactionRemoveAll);
        addHandler(MessageUpdate.class, MessageDispatchHandlers::messageUpdate);
        addHandler(PresenceUpdate.class, DispatchHandlers::presenceUpdate);
        addHandler(Ready.class, LifecycleDispatchHandlers::ready);
        addHandler(Resumed.class, LifecycleDispatchHandlers::resumed);
        addHandler(TypingStart.class, DispatchHandlers::typingStart);
        addHandler(UserUpdate.class, DispatchHandlers::userUpdate);
        addHandler(VoiceServerUpdate.class, DispatchHandlers::voiceServerUpdate);
        addHandler(VoiceStateUpdateDispatch.class, DispatchHandlers::voiceStateUpdateDispatch);
        addHandler(WebhooksUpdate.class, DispatchHandlers::webhooksUpdate);
        addHandler(InviteCreate.class, DispatchHandlers::inviteCreate);
        addHandler(InviteDelete.class, DispatchHandlers::inviteDelete);
        addHandler(InteractionCreate.class, DispatchHandlers::interactionCreate);
        addHandler(ApplicationCommandCreate.class, ApplicationCommandDispatchHandlers::applicationCommandCreate);
        addHandler(ApplicationCommandUpdate.class, ApplicationCommandDispatchHandlers::applicationCommandUpdate);
        addHandler(ApplicationCommandDelete.class, ApplicationCommandDispatchHandlers::applicationCommandDelete);
        addHandler(ApplicationCommandPermissionUpdate.class, ApplicationCommandDispatchHandlers::applicationCommandPermissionUpdate);
        addHandler(IntegrationCreate.class, DispatchHandlers::integrationCreate);
        addHandler(IntegrationUpdate.class, DispatchHandlers::integrationUpdate);
        addHandler(IntegrationDelete.class, DispatchHandlers::integrationDelete);
        addHandler(ThreadCreate.class, ThreadDispatchHandlers::threadCreate);
        addHandler(ThreadUpdate.class, ThreadDispatchHandlers::threadUpdate);
        addHandler(ThreadDelete.class, ThreadDispatchHandlers::threadDelete);
        addHandler(ThreadListSync.class, ThreadDispatchHandlers::threadListSync);
        addHandler(ThreadMemberUpdate.class, ThreadDispatchHandlers::threadMemberUpdate);
        addHandler(ThreadMembersUpdate.class, ThreadDispatchHandlers::threadMembersUpdate);
        addHandler(StageInstanceCreate.class, StageInstanceDispatchHandlers::stageInstanceCreate);
        addHandler(StageInstanceUpdate.class, StageInstanceDispatchHandlers::stageInstanceUpdate);
        addHandler(StageInstanceDelete.class, StageInstanceDispatchHandlers::stageInstanceDelete);
        addHandler(AutoModRuleCreate.class, DispatchHandlers::autoModRuleCreate);
        addHandler(AutoModRuleUpdate.class, DispatchHandlers::autoModRuleUpdate);
        addHandler(AutoModRuleDelete.class, DispatchHandlers::autoModRuleDelete);
        addHandler(AutoModActionExecution.class, DispatchHandlers::autoModActionExecute);
        addHandler(PollVoteAdd.class, PollDispatchHandlers::pollVoteAddHandler);
        addHandler(PollVoteRemove.class, PollDispatchHandlers::pollVoteRemoveHandler);
        addHandler(EntitlementCreate.class, MonetizationDispatchHandlers::entitlementCreate);
        addHandler(EntitlementUpdate.class, MonetizationDispatchHandlers::entitlementUpdate);
        addHandler(EntitlementDelete.class, MonetizationDispatchHandlers::entitlementDelete);

        addHandler(GatewayStateChange.class, LifecycleDispatchHandlers::gatewayStateChanged);

        addHandler(UnavailableGuildCreate.class, context -> Mono.empty());
    }

    private static <D, S, E extends Event> void addHandler(Class<D> dispatchType,
                                                           DispatchHandler<D, S, E> dispatchHandler) {
        handlerMap.put(dispatchType, dispatchHandler);
    }

    private static final Logger log = Loggers.getLogger(DispatchHandlers.class);

    /**
     * Process a {@link Dispatch} object wrapped with its context to potentially obtain an {@link Event}.
     *
     * @param context the DispatchContext used with this Dispatch object
     * @param <D> the Dispatch type
     * @param <S> the old state type, if applicable
     * @param <E> the resulting Event type
     * @return an Event mapped from the given Dispatch object, or null if no Event is produced.
     */
    @SuppressWarnings("unchecked")
    public <D, S, E extends Event> Mono<E> handle(DispatchContext<D, S> context) {
        DispatchHandler<D, S, E> handler = (DispatchHandler<D, S, E>) handlerMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().isAssignableFrom(context.getDispatch().getClass()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        if (handler == null) {
            log.warn("Handler not found from: {}", context.getDispatch().getClass());
            return Mono.empty();
        }
        return Mono.defer(() -> handler.handle(context))
                .checkpoint("Dispatch handled for " + context.getDispatch().getClass());
    }

    private static Mono<PresenceUpdateEvent> presenceUpdate(DispatchContext<PresenceUpdate, PresenceAndUserData> context) {
        GatewayDiscordClient gateway = context.getGateway();

        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        PartialUserData userData = context.getDispatch().user();
        PresenceData presenceData = createPresence(context.getDispatch());
        Presence current = new Presence(presenceData);
        Presence oldPresence = context.getOldState()
                .flatMap(PresenceAndUserData::getPresenceData)
                .map(Presence::new)
                .orElse(null);
        User oldUser = context.getOldState()
                .flatMap(PresenceAndUserData::getUserData)
                .map(old -> new User(gateway, old))
                .orElse(null);

        return Mono.just(new PresenceUpdateEvent(gateway, context.getShardInfo(), guildId, oldUser, userData, current, oldPresence));
    }

    private static PresenceData createPresence(PresenceUpdate update) {
        return PresenceData.builder()
                .user(update.user())
                .status(update.status())
                .activities(update.activities())
                .clientStatus(update.clientStatus())
                .build();
    }

    private static Mono<TypingStartEvent> typingStart(DispatchContext<TypingStart, Void> context) {
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        Long guildId = context.getDispatch().guildId().toOptional().map(Snowflake::asLong).orElse(null);
        long userId = Snowflake.asLong(context.getDispatch().userId());
        Instant startTime = Instant.ofEpochSecond(context.getDispatch().timestamp());

        Member member = context.getDispatch().member().toOptional()
                .filter(__ -> guildId != null)
                .map(memberData -> new Member(context.getGateway(), memberData, guildId))
                .orElse(null);

        return Mono.just(new TypingStartEvent(context.getGateway(), context.getShardInfo(), channelId, guildId,
                userId, startTime, member));
    }

    private static Mono<UserUpdateEvent> userUpdate(DispatchContext<UserUpdate, UserData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        UserData userData = context.getDispatch().user();
        User current = new User(gateway, userData);

        return Mono.just(new UserUpdateEvent(gateway, context.getShardInfo(), current, context.getOldState()
                                .map(old -> new User(gateway, old)).orElse(null)));
    }

    private static Mono<Event> voiceServerUpdate(DispatchContext<VoiceServerUpdate, Void> context) {
        String token = context.getDispatch().token();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        String endpoint = context.getDispatch().endpoint();

        return Mono.just(new VoiceServerUpdateEvent(context.getGateway(), context.getShardInfo(), token, guildId,
                endpoint));
    }

    private static Mono<VoiceStateUpdateEvent> voiceStateUpdateDispatch(DispatchContext<VoiceStateUpdateDispatch, VoiceStateData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        Optional<VoiceStateData> oldVoiceStateData = context.getOldState();
        VoiceStateUpdateDispatch voiceStateUpdate = context.getDispatch();
        VoiceStateData voiceStateData = voiceStateUpdate.voiceState();
        VoiceState current = new VoiceState(gateway, voiceStateData);

        if (oldVoiceStateData.isPresent()
                && voiceStateData.channelId().isPresent()
                && !voiceStateData.guildId().isAbsent()
                && voiceStateData.suppress()
                && voiceStateData.requestToSpeakTimestamp().isPresent()
                && !oldVoiceStateData.flatMap(VoiceStateData::requestToSpeakTimestamp).isPresent()) {
            return Mono.just(new StageRequestToSpeakEvent(gateway, context.getShardInfo(), current,
                    oldVoiceStateData.map(old -> new VoiceState(gateway, old)).orElse(null)));
        } else {
            return Mono.just(new VoiceStateUpdateEvent(gateway, context.getShardInfo(), current, context.getOldState()
                    .map(old -> new VoiceState(gateway, old)).orElse(null)));
        }
    }

    private static Mono<Event> webhooksUpdate(DispatchContext<WebhooksUpdate, Void> context) {
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        long channelId = Snowflake.asLong(context.getDispatch().channelId());

        return Mono.just(new WebhooksUpdateEvent(context.getGateway(), context.getShardInfo(), guildId, channelId));
    }

    private static Mono<AuditLogEntryCreateEvent> auditLogEntryCreate(DispatchContext<AuditLogEntryCreate, Void> context) {
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        AuditLogEntry auditLogEntry = new AuditLogEntry(context.getGateway(), context.getDispatch());

        return Mono.just(new AuditLogEntryCreateEvent(context.getGateway(), context.getShardInfo(), guildId, auditLogEntry));
    }

    private static Mono<InviteCreateEvent> inviteCreate(DispatchContext<InviteCreate, Void> context) {
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        String code = context.getDispatch().code();
        Instant createdAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                .parse(context.getDispatch().createdAt(), Instant::from);
        int uses = context.getDispatch().uses();
        int maxUses = context.getDispatch().maxUses();
        int maxAge = context.getDispatch().maxAge();
        boolean temporary = context.getDispatch().temporary();

        User current = context.getDispatch().inviter().toOptional()
                .map(userData -> new User(context.getGateway(), userData))
                .orElse(null);

        return Mono.just(new InviteCreateEvent(context.getGateway(), context.getShardInfo(), guildId, channelId, code,
                current, createdAt, uses, maxUses, maxAge, temporary));
    }

    private static Mono<InviteDeleteEvent> inviteDelete(DispatchContext<InviteDelete, Void> context) {
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        String code = context.getDispatch().code();

        return Mono.just(new InviteDeleteEvent(context.getGateway(), context.getShardInfo(), guildId, channelId, code));
    }

    private static Mono<InteractionCreateEvent> interactionCreate(DispatchContext<InteractionCreate, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        Interaction interaction = new Interaction(gateway, context.getDispatch().interaction());

        switch (interaction.getType()) {
            case APPLICATION_COMMAND:
                ApplicationCommand.Type commandType = interaction.getCommandInteraction()
                        .flatMap(ApplicationCommandInteraction::getApplicationCommandType)
                        .orElseThrow(IllegalStateException::new); // command type must be present.

                switch (commandType) {
                    case CHAT_INPUT:
                        return Mono.just(new ChatInputInteractionEvent(gateway, context.getShardInfo(), interaction));
                    case MESSAGE:
                        return Mono.just(new MessageInteractionEvent(gateway, context.getShardInfo(), interaction));
                    case USER:
                        return Mono.just(new UserInteractionEvent(gateway, context.getShardInfo(), interaction));
                    default:
                        return Mono.just(
                                new ApplicationCommandInteractionEvent(gateway, context.getShardInfo(), interaction)
                        );
                }
            case MESSAGE_COMPONENT:
                MessageComponent.Type componentType = interaction.getCommandInteraction()
                        .flatMap(ApplicationCommandInteraction::getComponentType)
                        .orElseThrow(IllegalStateException::new); // component type must be present

                switch (componentType) {
                    case BUTTON:
                        return Mono.just(new ButtonInteractionEvent(gateway, context.getShardInfo(), interaction));
                    case SELECT_MENU:
                    case SELECT_MENU_ROLE:
                    case SELECT_MENU_USER:
                    case SELECT_MENU_MENTIONABLE:
                    case SELECT_MENU_CHANNEL:
                        return Mono.just(new SelectMenuInteractionEvent(gateway, context.getShardInfo(), interaction));
                    default:
                        return Mono.just(new ComponentInteractionEvent(gateway, context.getShardInfo(), interaction));
                }

            case APPLICATION_COMMAND_AUTOCOMPLETE:
                return Mono.just(new ChatInputAutoCompleteEvent(gateway, context.getShardInfo(), interaction));
            case MODAL_SUBMIT:
                return Mono.just(new ModalSubmitInteractionEvent(gateway, context.getShardInfo(), interaction));
            default:
                return Mono.just(new InteractionCreateEvent(gateway, context.getShardInfo(), interaction));
        }

    }

    private static Mono<IntegrationDeleteEvent> integrationDelete(DispatchContext<IntegrationDelete, Void> context) {
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        long id = Snowflake.asLong(context.getDispatch().id());
        Long applicationId = context.getDispatch().applicationId().toOptional().map(Snowflake::asLong).orElse(null);

        return Mono.just(new IntegrationDeleteEvent(context.getGateway(), context.getShardInfo(), id, guildId,
                applicationId));
    }

    private static Mono<IntegrationUpdateEvent> integrationUpdate(DispatchContext<IntegrationUpdate, Void> context) {
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        Integration integration = new Integration(context.getGateway(), context.getDispatch().integration(), guildId);

        return Mono.just(new IntegrationUpdateEvent(context.getGateway(), context.getShardInfo(), guildId,
                integration));
    }

    private static Mono<IntegrationCreateEvent> integrationCreate(DispatchContext<IntegrationCreate, Void> context) {
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        Integration integration = new Integration(context.getGateway(), context.getDispatch().integration(), guildId);

        return Mono.just(new IntegrationCreateEvent(context.getGateway(), context.getShardInfo(), guildId, integration));
    }

    private static Mono<AutoModRuleCreateEvent> autoModRuleCreate(DispatchContext<AutoModRuleCreate,Void> context) {
        AutoModRule autoModRule = new AutoModRule(context.getGateway(), context.getDispatch().automodrule());

        return Mono.just(new AutoModRuleCreateEvent(context.getGateway(), context.getShardInfo(), autoModRule));
    }

    private static Mono<AutoModRuleUpdateEvent> autoModRuleUpdate(DispatchContext<AutoModRuleUpdate, Void> context) {
        AutoModRule autoModRule = new AutoModRule(context.getGateway(), context.getDispatch().automodrule());

        return Mono.just(new AutoModRuleUpdateEvent(context.getGateway(), context.getShardInfo(), autoModRule));
    }

    private static Mono<AutoModRuleDeleteEvent> autoModRuleDelete(DispatchContext<AutoModRuleDelete, Void> context) {
        AutoModRule autoModRule = new AutoModRule(context.getGateway(), context.getDispatch().automodrule());

        return Mono.just(new AutoModRuleDeleteEvent(context.getGateway(), context.getShardInfo(), autoModRule));
    }

    private static Mono<AutoModActionExecutedEvent> autoModActionExecute(DispatchContext<AutoModActionExecution, Void> context) {
        return Mono.just(new AutoModActionExecutedEvent(context.getGateway(), context.getShardInfo(), context.getDispatch()));
    }
}
