package discord4j.core.newstoresapi;

import discord4j.discordjson.json.*;
import discord4j.discordjson.json.gateway.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EntityPatcher {

    // If a method returns something else than Mono<Void> : it OPTIONALLY returns OLD state (implementation may choose
    // to always return empty to save performance)

    Mono<Void> addChannelIdToGuild(ChannelCreate create);

    Mono<Void> removeChannelIdFromGuild(ChannelDelete delete);

    Mono<ChannelData> updateChannel(ChannelUpdate update);

    Mono<Void> updateEmojiIdsOnGuild(GuildEmojisUpdate update);

    Mono<Void> addMemberIdToGuild(GuildMemberAdd add);

    Mono<Void> addMemberIdsToGuild(GuildMembersChunk chunk);

    Mono<Void> removeMemberIdFromGuild(GuildMemberRemove remove);

    Mono<MemberData> updateMember(GuildMemberUpdate update);

    Mono<Void> addRoleIdToGuild(GuildRoleCreate create);

    Mono<Void> removeRoleIdFromGuildAndMembers(GuildRoleDelete delete);

    Mono<RoleData> updateRole(GuildRoleUpdate update);

    Mono<GuildData> updateGuild(GuildUpdate update);

    Mono<Void> editLastMessageId(MessageCreate create);

    Mono<MessageData> deleteMessage(MessageDelete delete); // Not using Store#delete to offer the ability to return old state

    Flux<MessageData> deleteMessageBulk(MessageDeleteBulk deleteBulk);

    Mono<Void> addReactionToMessage(MessageReactionAdd reactionAdd);

    Mono<Void> removeReactionFromMessage(MessageReactionRemove reactionRemove);

    Mono<Void> removeEmojiReactionFromMessage(MessageReactionRemoveEmoji reactionRemoveEmoji);

    Mono<Void> removeAllReactionsFromMessage(MessageReactionRemoveAll reactionRemoveAll);

    Mono<MessageData> updateMessage(MessageUpdate update);
}
