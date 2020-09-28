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

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.channel.*;
import discord4j.core.object.entity.channel.*;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.gateway.ChannelCreate;
import discord4j.discordjson.json.gateway.ChannelDelete;
import discord4j.discordjson.json.gateway.ChannelPinsUpdate;
import discord4j.discordjson.json.gateway.ChannelUpdate;
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static discord4j.core.newstoresapi.EntityMetadata.channel;

class ChannelDispatchHandlers {

    static Mono<? extends Event> channelCreate(DispatchContext<ChannelCreate> context) {
        Channel.Type type = Channel.Type.of(context.getDispatch().channel().type());

        switch (type) {
            case GUILD_TEXT: return textChannelCreate(context);
            case DM: return privateChannelCreate(context);
            case GUILD_VOICE: return voiceChannelCreate(context);
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

        Mono<TextChannelCreateEvent> saveChannel = context.getStore()
                .save(channel(Snowflake.of(channel.id()), context.getShardTag()), channel)
                .thenReturn(new TextChannelCreateEvent(gateway, context.getShardInfo(), new TextChannel(gateway, channel)));

        return context.getStore().getEntityPatcher()
                .addChannelIdToGuild(context.getDispatch())
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

        Mono<VoiceChannelCreateEvent> saveChannel = context.getStore()
                .save(channel(Snowflake.of(channel.id()), context.getShardTag()), channel)
                .thenReturn(new VoiceChannelCreateEvent(gateway, context.getShardInfo(), new VoiceChannel(gateway, channel)));

        return context.getStore().getEntityPatcher()
                .addChannelIdToGuild(context.getDispatch())
                .then(saveChannel);
    }

    private static Mono<CategoryCreateEvent> categoryCreate(DispatchContext<ChannelCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        Mono<CategoryCreateEvent> saveChannel = context.getStore()
                .save(channel(Snowflake.of(channel.id()), context.getShardTag()), channel)
                .thenReturn(new CategoryCreateEvent(gateway, context.getShardInfo(), new Category(gateway, channel)));

        return context.getStore().getEntityPatcher()
                .addChannelIdToGuild(context.getDispatch())
                .then(saveChannel);
    }

    private static Mono<NewsChannelCreateEvent> newsChannelCreate(DispatchContext<ChannelCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        Mono<NewsChannelCreateEvent> saveChannel = context.getStore()
                .save(channel(Snowflake.of(channel.id()), context.getShardTag()), channel)
                .thenReturn(new NewsChannelCreateEvent(gateway, context.getShardInfo(), new NewsChannel(gateway, channel)));

        return context.getStore().getEntityPatcher()
                .addChannelIdToGuild(context.getDispatch())
                .then(saveChannel);
    }

    private static Mono<StoreChannelCreateEvent> storeChannelCreate(DispatchContext<ChannelCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        Mono<StoreChannelCreateEvent> saveChannel = context.getStore()
                .save(channel(Snowflake.of(channel.id()), context.getShardTag()), channel)
                .thenReturn(new StoreChannelCreateEvent(gateway, context.getShardInfo(), new StoreChannel(gateway, channel)));

        return context.getStore().getEntityPatcher()
                .addChannelIdToGuild(context.getDispatch())
                .then(saveChannel);
    }

    static Mono<? extends Event> channelDelete(DispatchContext<ChannelDelete> context) {
        Channel.Type type = Channel.Type.of(context.getDispatch().channel().type());

        switch (type) {
            case GUILD_TEXT: return textChannelDelete(context);
            case DM: return privateChannelDelete(context);
            case GUILD_VOICE: return voiceChannelDelete(context);
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
        ChannelData channel = context.getDispatch().channel();

        Mono<TextChannelDeleteEvent> deleteChannel = context.getStore()
                .delete(channel(Snowflake.of(channel.id()), context.getShardTag()))
                .thenReturn(new TextChannelDeleteEvent(gateway, context.getShardInfo(), new TextChannel(gateway, channel)));

        return context.getStore().getEntityPatcher()
                .removeChannelIdFromGuild(context.getDispatch())
                .then(deleteChannel);
    }

    private static Mono<PrivateChannelDeleteEvent> privateChannelDelete(DispatchContext<ChannelDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        return Mono.just(new PrivateChannelDeleteEvent(gateway, context.getShardInfo(), new PrivateChannel(gateway, channel)));

    }

    private static Mono<VoiceChannelDeleteEvent> voiceChannelDelete(DispatchContext<ChannelDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        Mono<VoiceChannelDeleteEvent> deleteChannel = context.getStore()
                .delete(channel(Snowflake.of(channel.id()), context.getShardTag()))
                .thenReturn(new VoiceChannelDeleteEvent(gateway, context.getShardInfo(), new VoiceChannel(gateway, channel)));

        return context.getStore().getEntityPatcher()
                .removeChannelIdFromGuild(context.getDispatch())
                .then(deleteChannel);
    }

    private static Mono<CategoryDeleteEvent> categoryDelete(DispatchContext<ChannelDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        Mono<CategoryDeleteEvent> deleteChannel = context.getStore()
                .delete(channel(Snowflake.of(channel.id()), context.getShardTag()))
                .thenReturn(new CategoryDeleteEvent(gateway, context.getShardInfo(), new Category(gateway, channel)));

        return context.getStore().getEntityPatcher()
                .removeChannelIdFromGuild(context.getDispatch())
                .then(deleteChannel);
    }

    private static Mono<NewsChannelDeleteEvent> newsChannelDelete(DispatchContext<ChannelDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        Mono<NewsChannelDeleteEvent> deleteChannel = context.getStore()
                .delete(channel(Snowflake.of(channel.id()), context.getShardTag()))
                .thenReturn(new NewsChannelDeleteEvent(gateway, context.getShardInfo(), new NewsChannel(gateway, channel)));

        return context.getStore().getEntityPatcher()
                .removeChannelIdFromGuild(context.getDispatch())
                .then(deleteChannel);
    }

    private static Mono<StoreChannelDeleteEvent> storeChannelDelete(DispatchContext<ChannelDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        Mono<StoreChannelDeleteEvent> deleteChannel = context.getStore()
                .delete(channel(Snowflake.of(channel.id()), context.getShardTag()))
                .thenReturn(new StoreChannelDeleteEvent(gateway, context.getShardInfo(), new StoreChannel(gateway, channel)));

        return context.getStore().getEntityPatcher()
                .removeChannelIdFromGuild(context.getDispatch())
                .then(deleteChannel);
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
            case GUILD_VOICE: return voiceChannelUpdate(context);
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

        return context.getStore().getEntityPatcher()
                .updateChannel(context.getDispatch())
                .map(old -> new TextChannelUpdateEvent(gateway, context.getShardInfo(), current, new TextChannel(gateway, old)))
                .defaultIfEmpty(new TextChannelUpdateEvent(gateway, context.getShardInfo(), current, null));
    }

    private static Mono<VoiceChannelUpdateEvent> voiceChannelUpdate(DispatchContext<ChannelUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();
        VoiceChannel current = new VoiceChannel(gateway, channel);

        return context.getStore().getEntityPatcher()
                .updateChannel(context.getDispatch())
                .map(old -> new VoiceChannelUpdateEvent(gateway, context.getShardInfo(), current, new VoiceChannel(gateway, old)))
                .defaultIfEmpty(new VoiceChannelUpdateEvent(gateway, context.getShardInfo(), current, null));
    }

    private static Mono<CategoryUpdateEvent> categoryUpdate(DispatchContext<ChannelUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();
        Category current = new Category(gateway, channel);

        return context.getStore().getEntityPatcher()
                .updateChannel(context.getDispatch())
                .map(old -> new CategoryUpdateEvent(gateway, context.getShardInfo(), current, new Category(gateway, old)))
                .defaultIfEmpty(new CategoryUpdateEvent(gateway, context.getShardInfo(), current, null));
    }

    private static Mono<NewsChannelUpdateEvent> newsChannelUpdate(DispatchContext<ChannelUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();
        GuildMessageChannel current = getConvertibleChannel(gateway, channel);

        return context.getStore().getEntityPatcher()
                .updateChannel(context.getDispatch())
                .map(old -> new NewsChannelUpdateEvent(gateway, context.getShardInfo(), current, new NewsChannel(gateway, old)))
                .defaultIfEmpty(new NewsChannelUpdateEvent(gateway, context.getShardInfo(), current, null));
    }

    private static Mono<StoreChannelUpdateEvent> storeChannelUpdate(DispatchContext<ChannelUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();
        StoreChannel current = new StoreChannel(gateway, channel);

        return context.getStore().getEntityPatcher()
                .updateChannel(context.getDispatch())
                .map(old -> new StoreChannelUpdateEvent(gateway, context.getShardInfo(), current, new StoreChannel(gateway, old)))
                .defaultIfEmpty(new StoreChannelUpdateEvent(gateway, context.getShardInfo(), current, null));
    }

    private static GuildMessageChannel getConvertibleChannel(GatewayDiscordClient gateway, ChannelData channel) {
        switch (Channel.Type.of(channel.type())) {
            case GUILD_NEWS: return new NewsChannel(gateway, channel);
            case GUILD_TEXT: return new TextChannel(gateway, channel);
            default: throw new AssertionError();
        }
    }
}
