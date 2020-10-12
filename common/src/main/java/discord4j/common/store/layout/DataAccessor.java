package discord4j.common.store.layout;

import discord4j.discordjson.json.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    Flux<VoiceStateData> getChannelVoiceStates(long channelId);

    Mono<GuildData> getGuildById(long guildId);

    Flux<ChannelData> getGuildChannels(long guildId, boolean requireComplete);

    Mono<EmojiData> getGuildEmojiById(long guildId, long emojiId);

    Flux<EmojiData> getGuildEmojis(long guildId, boolean requireComplete);

    Flux<MemberData> getGuildMembers(long guildId, boolean requireComplete);

    Flux<PresenceData> getGuildPresences(long guildId, boolean requireComplete);

    Flux<RoleData> getGuildRoles(long guildId, boolean requireComplete);

    Flux<GuildData> getGuilds(boolean requireComplete);

    Flux<VoiceStateData> getGuildVoiceStates(long guildId);

    Mono<MemberData> getMemberById(long guildId, long userId);

    Mono<MessageData> getMessageById(long channelId, long messageId);

    Mono<PresenceData> getPresenceById(long guildId, long userId);

    Mono<RoleData> getRoleById(long guildId, long roleId);

    Mono<UserData> getUserById(long userId);

    Flux<UserData> getUsers();

    Mono<VoiceStateData> getVoiceStateById(long guildId, long userId);
}
