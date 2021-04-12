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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.event.dispatch;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.channel.*;
import discord4j.core.object.entity.channel.*;
import discord4j.core.state.StateHolder;
import discord4j.core.util.ListUtil;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.GuildData;
import discord4j.discordjson.json.gateway.ChannelCreate;
import discord4j.discordjson.json.gateway.ChannelDelete;
import discord4j.discordjson.json.gateway.ChannelPinsUpdate;
import discord4j.discordjson.json.gateway.ChannelUpdate;
import discord4j.discordjson.possible.Possible;
import discord4j.common.util.Snowflake;
import discord4j.store.api.primitive.LongObjStore;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

class ChannelDispatchHandlers {

    static Mono<? extends Event> channelCreate(DispatchContext<ChannelCreate> context) {
        Channel.Type type = Channel.Type.of(context.getDispatch().channel().type());

        switch (type) {
            case GUILD_TEXT: return textChannelCreate(context);
            case DM: return privateChannelCreate(context);
            case GUILD_VOICE:
            case GUILD_STAGE_VOICE:
                return voiceChannelCreate(context);
            case GROUP_DM:
                throw new UnsupportedOperationException("Received channel_create for group on a bot account!");
            case GUILD_CATEGORY: return categoryCreate(context);
            case GUILD_NEWS: return newsChannelCreate(context);
            case GUILD_STORE: return storeChannelCreate(context);
            default: throw new AssertionError();
        }
    }

    private static Mono<TextChannelCreateEvent> textChannelCreate(DispatchContext<ChannelCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        Mono<TextChannelCreateEvent> saveChannel = context.getStateHolder().getChannelStore()
                .save(Snowflake.asLong(channel.id()), channel)
                .thenReturn(new TextChannelCreateEvent(gateway, context.getShardInfo(), new TextChannel(gateway, channel)));

        return addChannelToGuild(context.getStateHolder().getGuildStore(), channel)
                .then(saveChannel);
    }

    private static Mono<PrivateChannelCreateEvent> privateChannelCreate(DispatchContext<ChannelCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        return Mono.just(new PrivateChannelCreateEvent(gateway, context.getShardInfo(), new PrivateChannel(gateway, channel)));
    }

    private static Mono<VoiceChannelCreateEvent> voiceChannelCreate(DispatchContext<ChannelCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        Mono<VoiceChannelCreateEvent> saveChannel = context.getStateHolder().getChannelStore()
                .save(Snowflake.asLong(channel.id()), channel)
                .thenReturn(new VoiceChannelCreateEvent(gateway, context.getShardInfo(), new VoiceChannel(gateway, channel)));

        return addChannelToGuild(context.getStateHolder().getGuildStore(), channel)
                .then(saveChannel);
    }

    private static Mono<CategoryCreateEvent> categoryCreate(DispatchContext<ChannelCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        Mono<CategoryCreateEvent> saveChannel = context.getStateHolder().getChannelStore()
                .save(Snowflake.asLong(channel.id()), channel)
                .thenReturn(new CategoryCreateEvent(gateway, context.getShardInfo(), new Category(gateway, channel)));

        return addChannelToGuild(context.getStateHolder().getGuildStore(), channel)
                .then(saveChannel);
    }

    private static Mono<NewsChannelCreateEvent> newsChannelCreate(DispatchContext<ChannelCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        Mono<NewsChannelCreateEvent> saveChannel = context.getStateHolder().getChannelStore()
                .save(Snowflake.asLong(channel.id()), channel)
                .thenReturn(new NewsChannelCreateEvent(gateway, context.getShardInfo(), new NewsChannel(gateway, channel)));

        return addChannelToGuild(context.getStateHolder().getGuildStore(), channel)
                .then(saveChannel);
    }

    private static Mono<StoreChannelCreateEvent> storeChannelCreate(DispatchContext<ChannelCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        Mono<StoreChannelCreateEvent> saveChannel = context.getStateHolder().getChannelStore()
                .save(Snowflake.asLong(channel.id()), channel)
                .thenReturn(new StoreChannelCreateEvent(gateway, context.getShardInfo(), new StoreChannel(gateway, channel)));

        return addChannelToGuild(context.getStateHolder().getGuildStore(), channel)
                .then(saveChannel);
    }

    private static Mono<Void> addChannelToGuild(LongObjStore<GuildData> guildStore, ChannelData channel) {
        return guildStore.find(Snowflake.asLong(channel.guildId().get()))
                .map(guildData -> GuildData.builder()
                        .from(guildData)
                        .channels(ListUtil.add(guildData.channels(), channel.id()))
                        .build())
                .flatMap(guild -> guildStore.save(Snowflake.asLong(guild.id()), guild));
    }

    static Mono<? extends Event> channelDelete(DispatchContext<ChannelDelete> context) {
        Channel.Type type = Channel.Type.of(context.getDispatch().channel().type());

        switch (type) {
            case GUILD_TEXT: return textChannelDelete(context);
            case DM: return privateChannelDelete(context);
            case GUILD_VOICE:
            case GUILD_STAGE_VOICE:
                return voiceChannelDelete(context);
            case GROUP_DM:
                throw new UnsupportedOperationException("Received channel_delete for a group on a bot account!");
            case GUILD_CATEGORY: return categoryDelete(context);
            case GUILD_NEWS: return newsChannelDelete(context);
            case GUILD_STORE: return storeChannelDelete(context);
            default: throw new AssertionError();
        }
    }

    private static Mono<TextChannelDeleteEvent> textChannelDelete(DispatchContext<ChannelDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        StateHolder stateHolder = context.getStateHolder();
        ChannelData channel = context.getDispatch().channel();

        Mono<TextChannelDeleteEvent> deleteChannel = stateHolder.getChannelStore()
                .delete(Snowflake.asLong(channel.id()))
                .thenReturn(new TextChannelDeleteEvent(gateway, context.getShardInfo(), new TextChannel(gateway, channel)));

        return removeChannelFromGuild(stateHolder.getGuildStore(), channel)
                .then(deleteChannel);
    }

    private static Mono<PrivateChannelDeleteEvent> privateChannelDelete(DispatchContext<ChannelDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        return Mono.just(new PrivateChannelDeleteEvent(gateway, context.getShardInfo(), new PrivateChannel(gateway, channel)));

    }

    private static Mono<VoiceChannelDeleteEvent> voiceChannelDelete(DispatchContext<ChannelDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        StateHolder stateHolder = context.getStateHolder();
        ChannelData channel = context.getDispatch().channel();

        Mono<VoiceChannelDeleteEvent> deleteChannel = stateHolder.getChannelStore()
                .delete(Snowflake.asLong(channel.id()))
                .thenReturn(new VoiceChannelDeleteEvent(gateway, context.getShardInfo(), new VoiceChannel(gateway, channel)));

        return removeChannelFromGuild(stateHolder.getGuildStore(), channel)
                .then(deleteChannel);
    }

    private static Mono<CategoryDeleteEvent> categoryDelete(DispatchContext<ChannelDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        StateHolder stateHolder = context.getStateHolder();
        ChannelData channel = context.getDispatch().channel();

        Mono<CategoryDeleteEvent> deleteChannel = stateHolder.getChannelStore()
                .delete(Snowflake.asLong(channel.id()))
                .thenReturn(new CategoryDeleteEvent(gateway, context.getShardInfo(), new Category(gateway, channel)));

        return removeChannelFromGuild(stateHolder.getGuildStore(), channel)
                .then(deleteChannel);
    }

    private static Mono<NewsChannelDeleteEvent> newsChannelDelete(DispatchContext<ChannelDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        StateHolder stateHolder = context.getStateHolder();
        ChannelData channel = context.getDispatch().channel();

        Mono<NewsChannelDeleteEvent> deleteChannel = stateHolder.getChannelStore()
                .delete(Snowflake.asLong(channel.id()))
                .thenReturn(new NewsChannelDeleteEvent(gateway, context.getShardInfo(), new NewsChannel(gateway, channel)));

        return removeChannelFromGuild(stateHolder.getGuildStore(), channel)
                .then(deleteChannel);
    }

    private static Mono<StoreChannelDeleteEvent> storeChannelDelete(DispatchContext<ChannelDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        StateHolder stateHolder = context.getStateHolder();
        ChannelData channel = context.getDispatch().channel();

        Mono<StoreChannelDeleteEvent> deleteChannel = stateHolder.getChannelStore()
                .delete(Snowflake.asLong(channel.id()))
                .thenReturn(new StoreChannelDeleteEvent(gateway, context.getShardInfo(), new StoreChannel(gateway, channel)));

        return removeChannelFromGuild(stateHolder.getGuildStore(), channel)
                .then(deleteChannel);
    }

    private static Mono<Void> removeChannelFromGuild(LongObjStore<GuildData> guildStore, ChannelData channel) {
        return guildStore.find(Snowflake.asLong(channel.guildId().get()))
                .map(guildData -> GuildData.builder()
                        .from(guildData)
                        .channels(ListUtil.remove(guildData.channels(), ch -> channel.id().equals(ch)))
                        .build())
                .flatMap(guild -> guildStore.save(Snowflake.asLong(guild.id()), guild));
    }

    static Mono<PinsUpdateEvent> channelPinsUpdate(DispatchContext<ChannelPinsUpdate> context) {
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        Long guildId = context.getDispatch().guildId()
            .toOptional()
            .map(Snowflake::asLong)
            .orElse(null);

        Instant timestamp = Possible.flatOpt(context.getDispatch().lastPinTimestamp())
                .map(text -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(text, Instant::from))
                .orElse(null);

        return Mono.just(new PinsUpdateEvent(context.getGateway(), context.getShardInfo(), channelId, guildId, timestamp));
    }

    static Mono<? extends Event> channelUpdate(DispatchContext<ChannelUpdate> context) {
        Channel.Type type = Channel.Type.of(context.getDispatch().channel().type());

        switch (type) {
            case GUILD_TEXT: return textChannelUpdate(context);
            case DM:
                throw new UnsupportedOperationException("Received channel_update for a DM on a bot account!");
            case GUILD_VOICE:
            case GUILD_STAGE_VOICE:
                return voiceChannelUpdate(context);
            case GROUP_DM:
                throw new UnsupportedOperationException("Received channel_update for a group on a bot account!");
            case GUILD_CATEGORY: return categoryUpdate(context);
            case GUILD_NEWS: return newsChannelUpdate(context);
            case GUILD_STORE: return storeChannelUpdate(context);
            default: throw new AssertionError();
        }
    }

    private static Mono<TextChannelUpdateEvent> textChannelUpdate(DispatchContext<ChannelUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();
        GuildMessageChannel current = getConvertibleChannel(gateway, channel);

        Mono<Void> saveNew = context.getStateHolder().getChannelStore().save(Snowflake.asLong(channel.id()), channel);

        return context.getStateHolder().getChannelStore()
                .find(Snowflake.asLong(channel.id()))
                .flatMap(saveNew::thenReturn)
                .map(old -> new TextChannelUpdateEvent(gateway, context.getShardInfo(), current, new TextChannel(gateway, old)))
                .switchIfEmpty(saveNew.thenReturn(new TextChannelUpdateEvent(gateway, context.getShardInfo(), current, null)));
    }

    private static Mono<VoiceChannelUpdateEvent> voiceChannelUpdate(DispatchContext<ChannelUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();
        VoiceChannel current = new VoiceChannel(gateway, channel);
        long id = Snowflake.asLong(channel.id());

        Mono<Void> saveNew = context.getStateHolder().getChannelStore().save(id, channel);

        return context.getStateHolder().getChannelStore()
                .find(id)
                .flatMap(saveNew::thenReturn)
                .map(old -> new VoiceChannelUpdateEvent(gateway, context.getShardInfo(), current, new VoiceChannel(gateway, old)))
                .switchIfEmpty(saveNew.thenReturn(new VoiceChannelUpdateEvent(gateway, context.getShardInfo(), current, null)));
    }

    private static Mono<CategoryUpdateEvent> categoryUpdate(DispatchContext<ChannelUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();
        Category current = new Category(gateway, channel);
        long id = Snowflake.asLong(channel.id());

        Mono<Void> saveNew = context.getStateHolder().getChannelStore().save(id, channel);

        return context.getStateHolder().getChannelStore()
                .find(id)
                .flatMap(saveNew::thenReturn)
                .map(old -> new CategoryUpdateEvent(gateway, context.getShardInfo(), current, new Category(gateway, old)))
                .switchIfEmpty(saveNew.thenReturn(new CategoryUpdateEvent(gateway, context.getShardInfo(), current, null)));
    }

    private static Mono<NewsChannelUpdateEvent> newsChannelUpdate(DispatchContext<ChannelUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();
        GuildMessageChannel current = getConvertibleChannel(gateway, channel);
        long id = Snowflake.asLong(channel.id());

        Mono<Void> saveNew = context.getStateHolder().getChannelStore().save(id, channel);

        return context.getStateHolder().getChannelStore()
                .find(id)
                .flatMap(saveNew::thenReturn)
                .map(old -> new NewsChannelUpdateEvent(gateway, context.getShardInfo(), current, new NewsChannel(gateway, old)))
                .switchIfEmpty(saveNew.thenReturn(new NewsChannelUpdateEvent(gateway, context.getShardInfo(), current, null)));
    }

    private static Mono<StoreChannelUpdateEvent> storeChannelUpdate(DispatchContext<ChannelUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();
        StoreChannel current = new StoreChannel(gateway, channel);
        long id = Snowflake.asLong(channel.id());

        Mono<Void> saveNew = context.getStateHolder().getChannelStore().save(id, channel);

        return context.getStateHolder().getChannelStore()
                .find(id)
                .flatMap(saveNew::thenReturn)
                .map(old -> new StoreChannelUpdateEvent(gateway, context.getShardInfo(), current, new StoreChannel(gateway, old)))
                .switchIfEmpty(saveNew.thenReturn(new StoreChannelUpdateEvent(gateway, context.getShardInfo(), current, null)));
    }

    private static GuildMessageChannel getConvertibleChannel(GatewayDiscordClient gateway, ChannelData channel) {
        switch (Channel.Type.of(channel.type())) {
            case GUILD_NEWS: return new NewsChannel(gateway, channel);
            case GUILD_TEXT: return new TextChannel(gateway, channel);
            default: throw new AssertionError();
        }
    }
}
