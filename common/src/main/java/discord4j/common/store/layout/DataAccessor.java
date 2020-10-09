package discord4j.common.store.layout;

import discord4j.common.store.layout.action.read.*;
import discord4j.common.store.util.PossiblyIncompleteList;
import discord4j.discordjson.json.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DataAccessor {
    
    Mono<Long> count(CountAction action);

    Mono<ChannelData> getChannelById(GetChannelByIdAction action);

    Mono<PossiblyIncompleteList<VoiceStateData>> getChannelVoiceStates(GetChannelVoiceStatesAction action);

    Mono<GuildData> getGuildById(GetGuildByIdAction action);

    Mono<PossiblyIncompleteList<ChannelData>> getGuildChannels(GetGuildChannelsAction action);

    Mono<EmojiData> getGuildEmojiById(GetGuildEmojiByIdAction action);

    Mono<PossiblyIncompleteList<EmojiData>> getGuildEmojis(GetGuildEmojisAction action);

    Mono<PossiblyIncompleteList<MemberData>> getGuildMembers(GetGuildMembersAction action);

    Mono<PossiblyIncompleteList<PresenceData>> getGuildPresences(GetGuildPresencesAction action);

    Mono<PossiblyIncompleteList<RoleData>> getGuildRoles(GetGuildRolesAction action);

    Mono<PossiblyIncompleteList<GuildData>> getGuilds(GetGuildsAction action);

    Mono<PossiblyIncompleteList<VoiceStateData>> getGuildVoiceStates(GetGuildVoiceStatesAction action);

    Mono<MemberData> getMemberById(GetMemberByIdAction action);

    Mono<MessageData> getMessageById(GetMessageByIdAction action);

    Mono<PresenceData> getPresenceById(GetPresenceByIdAction action);

    Mono<RoleData> getRoleById(GetRoleByIdAction action);

    Mono<UserData> getUserById(GetUserByIdAction action);

    Mono<List<UserData>> getUsers(GetUsersAction action);

    Mono<VoiceStateData> getVoiceStateById(GetVoiceStateByIdAction action);
}
