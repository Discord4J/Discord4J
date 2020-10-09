package discord4j.common.store.layout;

import discord4j.common.store.util.PossiblyIncompleteList;
import discord4j.discordjson.json.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DataAccessor {

    Mono<Long> countChannels();

    Mono<Long> countChannelsInGuild(long guildId);

    Mono<Long> countEmojis();

    Mono<Long> countEmojisInGuild(long guildId);

    Mono<Long> countGuilds();

    Mono<Long> countMembers();

    Mono<Long> countMembersInGuild(long guildId);

    Mono<Long> countMessages();

    Mono<Long> countMessagesInChannel(long channelId);

    Mono<Long> countPresences();

    Mono<Long> countPresencesInGuild(long guildId);

    Mono<Long> countRoles();

    Mono<Long> countRolesInGuild(long guildId);

    Mono<Long> countUsers();

    Mono<Long> countVoiceStates();

    Mono<Long> countVoiceStatesInGuild(long guildId);

    Mono<Long> countVoiceStatesInChannel(long channelId);

    Mono<ChannelData> getChannelById(long channelId);

    Mono<PossiblyIncompleteList<VoiceStateData>> getChannelVoiceStates(long channelId);

    Mono<GuildData> getGuildById(long guildId);

    Mono<PossiblyIncompleteList<ChannelData>> getGuildChannels(long guildId);

    Mono<EmojiData> getGuildEmojiById(long guildId, long emojiId);

    Mono<PossiblyIncompleteList<EmojiData>> getGuildEmojis(long guildId);

    Mono<PossiblyIncompleteList<MemberData>> getGuildMembers(long guildId);

    Mono<PossiblyIncompleteList<PresenceData>> getGuildPresences(long guildId);

    Mono<PossiblyIncompleteList<RoleData>> getGuildRoles(long guildId);

    Mono<PossiblyIncompleteList<GuildData>> getGuilds();

    Mono<PossiblyIncompleteList<VoiceStateData>> getGuildVoiceStates(long guildId);

    Mono<MemberData> getMemberById(long guildId, long userId);

    Mono<MessageData> getMessageById(long channelId, long messageId);

    Mono<PresenceData> getPresenceById(long guildId, long userId);

    Mono<RoleData> getRoleById(long guildId, long roleId);

    Mono<UserData> getUserById(long userId);

    Mono<List<UserData>> getUsers();

    Mono<VoiceStateData> getVoiceStateById(long guildId, long userId);
}
