package discord4j.common.store.layout;

import discord4j.common.store.util.PresenceAndUserData;
import discord4j.discordjson.json.*;
import discord4j.discordjson.json.gateway.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GatewayDataUpdater {

    Mono<Void> onChannelCreate(int shardIndex, ChannelCreate dispatch);

    Mono<ChannelData> onChannelDelete(int shardIndex, ChannelDelete dispatch);

    Mono<ChannelData> onChannelUpdate(int shardIndex, ChannelUpdate dispatch);

    Mono<Void> onGuildCreate(int shardIndex, GuildCreate dispatch);

    Mono<GuildData> onGuildDelete(int shardIndex, GuildDelete dispatch);

    Flux<EmojiData> onGuildEmojisUpdate(int shardIndex, GuildEmojisUpdate dispatch);

    Mono<Void> onGuildMemberAdd(int shardIndex, GuildMemberAdd dispatch);

    Mono<MemberData> onGuildMemberRemove(int shardIndex, GuildMemberRemove dispatch);

    Mono<Void> onGuildMembersChunk(int shardIndex, GuildMembersChunk dispatch);

    Mono<MemberData> onGuildMemberUpdate(int shardIndex, GuildMemberUpdate dispatch);

    Mono<Void> onGuildRoleCreate(int shardIndex, GuildRoleCreate dispatch);

    Mono<RoleData> onGuildRoleDelete(int shardIndex, GuildRoleDelete dispatch);

    Mono<RoleData> onGuildRoleUpdate(int shardIndex, GuildRoleUpdate dispatch);

    Mono<GuildData> onGuildUpdate(int shardIndex, GuildUpdate dispatch);

    Mono<Void> onInvalidateShard(int shardIndex, InvalidationCause cause);

    Mono<Void> onMessageCreate(int shardIndex, MessageCreate dispatch);

    Mono<MessageData> onMessageDelete(int shardIndex, MessageDelete dispatch);

    Flux<MessageData> onMessageDeleteBulk(int shardIndex, MessageDeleteBulk dispatch);

    Mono<Void> onMessageReactionAdd(int shardIndex, MessageReactionAdd dispatch);

    Mono<Void> onMessageReactionRemove(int shardIndex, MessageReactionRemove dispatch);

    Mono<Void> onMessageReactionRemoveAll(int shardIndex, MessageReactionRemoveAll dispatch);

    Mono<Void> onMessageReactionRemoveEmoji(int shardIndex, MessageReactionRemoveEmoji dispatch);

    Mono<MessageData> onMessageUpdate(int shardIndex, MessageUpdate dispatch);

    Mono<PresenceAndUserData> onPresenceUpdate(int shardIndex, PresenceUpdate dispatch);

    Mono<Void> onReady(int shardIndex, Ready dispatch);

    Mono<UserData> onUserUpdate(int shardIndex, UserUpdate dispatch);

    Mono<VoiceStateData> onVoiceStateUpdateDispatch(int shardIndex, VoiceStateUpdateDispatch dispatch);
}
