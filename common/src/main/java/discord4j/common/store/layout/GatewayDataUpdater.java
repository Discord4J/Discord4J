package discord4j.common.store.layout;

import discord4j.common.store.layout.action.gateway.*;
import discord4j.common.store.util.PresenceAndUserData;
import discord4j.discordjson.json.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GatewayDataUpdater {

    Mono<Void> onChannelCreate(ChannelCreateAction action);

    Mono<ChannelData> onChannelDelete(ChannelDeleteAction action);

    Mono<ChannelData> onChannelUpdate(ChannelUpdateAction action);

    Mono<Void> onGuildCreate(GuildCreateAction action);

    Mono<GuildData> onGuildDelete(GuildDeleteAction action);

    Mono<List<EmojiData>> onGuildEmojisUpdate(GuildEmojisUpdateAction action);

    Mono<Void> onGuildMemberAdd(GuildMemberAddAction action);

    Mono<MemberData> onGuildMemberRemove(GuildMemberRemoveAction action);

    Mono<Void> onGuildMembersChunk(GuildMembersChunkAction action);

    Mono<MemberData> onGuildMemberUpdate(GuildMemberUpdateAction action);

    Mono<Void> onGuildRoleCreate(GuildRoleCreateAction action);

    Mono<RoleData> onGuildRoleDelete(GuildRoleDeleteAction action);

    Mono<RoleData> onGuildRoleUpdate(GuildRoleUpdateAction action);

    Mono<GuildData> onGuildUpdate(GuildUpdateAction action);

    Mono<Void> onInvalidateShard(InvalidateShardAction action);

    Mono<Void> onMessageCreate(MessageCreateAction action);

    Mono<MessageData> onMessageDelete(MessageDeleteAction action);

    Mono<List<MessageData>> onMessageDeleteBulk(MessageDeleteBulkAction action);

    Mono<Void> onMessageReactionAdd(MessageReactionAddAction action);

    Mono<Void> onMessageReactionRemove(MessageReactionRemoveAction action);

    Mono<Void> onMessageReactionRemoveAll(MessageReactionRemoveAllAction action);

    Mono<Void> onMessageReactionRemoveEmoji(MessageReactionRemoveEmojiAction action);

    Mono<MessageData> onMessageUpdate(MessageUpdateAction action);

    Mono<PresenceAndUserData> onPresenceUpdate(PresenceUpdateAction action);

    Mono<Void> onReady(ReadyAction action);

    Mono<UserData> onUserUpdate(UserUpdateAction action);

    Mono<VoiceStateData> onVoiceStateUpdateDispatch(VoiceStateUpdateDispatchAction action);
}
