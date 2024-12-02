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
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

class ChannelDispatchHandlers {

    private static final Logger log = Loggers.getLogger(ChannelDispatchHandlers.class);

    static Mono<? extends Event> channelCreate(DispatchContext<ChannelCreate, Void> context) {
        Channel.Type type = Channel.Type.of(context.getDispatch().channel().type());

        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        return Mono.fromCallable(() -> {
            switch (type) {
                case GUILD_TEXT: return new TextChannelCreateEvent(gateway, context.getShardInfo(), new TextChannel(gateway, channel));
                case GUILD_VOICE:
                case GUILD_STAGE_VOICE:
                    return new VoiceChannelCreateEvent(gateway, context.getShardInfo(), new VoiceChannel(gateway, channel));
                case GROUP_DM:
                    throw new UnsupportedOperationException("Received channel_create for group on a bot account!");
                case GUILD_CATEGORY: return new CategoryCreateEvent(gateway, context.getShardInfo(), new Category(gateway, channel));
                case GUILD_NEWS: return new NewsChannelCreateEvent(gateway, context.getShardInfo(), new NewsChannel(gateway, channel));
                case GUILD_STORE: return new StoreChannelCreateEvent(gateway, context.getShardInfo(), new StoreChannel(gateway, channel));
                case GUILD_FORUM: return new ForumChannelCreateEvent(gateway, context.getShardInfo(), new ForumChannel(gateway, channel));
                default:
                    log.info("Received unknown channel create: {}", channel);
                    return new UnknownChannelCreateEvent(gateway, context.getShardInfo(), new UnknownChannel(gateway, channel));
            }
        });
    }

    static Mono<? extends Event> channelDelete(DispatchContext<ChannelDelete, ChannelData> context) {
        Channel.Type type = Channel.Type.of(context.getDispatch().channel().type());
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();

        return Mono.fromCallable(() -> {
            switch (type) {
                case GUILD_TEXT: return new TextChannelDeleteEvent(gateway, context.getShardInfo(), new TextChannel(gateway, channel));
                case DM: return new PrivateChannelDeleteEvent(gateway, context.getShardInfo(), new PrivateChannel(gateway, channel));
                case GUILD_VOICE:
                case GUILD_STAGE_VOICE:
                    return new VoiceChannelDeleteEvent(gateway, context.getShardInfo(), new VoiceChannel(gateway, channel));
                case GROUP_DM:
                    throw new UnsupportedOperationException("Received channel_delete for a group on a bot account!");
                case GUILD_CATEGORY: return new CategoryDeleteEvent(gateway, context.getShardInfo(), new Category(gateway, channel));
                case GUILD_NEWS: return new NewsChannelDeleteEvent(gateway, context.getShardInfo(), new NewsChannel(gateway, channel));
                case GUILD_STORE: return new StoreChannelDeleteEvent(gateway, context.getShardInfo(), new StoreChannel(gateway, channel));
                case GUILD_FORUM: return new ForumChannelDeleteEvent(gateway, context.getShardInfo(), new ForumChannel(gateway, channel));
                default:
                    log.info("Received unknown channel delete: {}", channel);
                    return new UnknownChannelDeleteEvent(gateway, context.getShardInfo(), new UnknownChannel(gateway, channel));
            }
        });
    }

    static Mono<PinsUpdateEvent> channelPinsUpdate(DispatchContext<ChannelPinsUpdate, Void> context) {
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

    static Mono<? extends Event> channelUpdate(DispatchContext<ChannelUpdate, ChannelData> context) {
        Channel.Type type = Channel.Type.of(context.getDispatch().channel().type());
        GatewayDiscordClient gateway = context.getGateway();
        ChannelData channel = context.getDispatch().channel();
        Optional<ChannelData> oldData = context.getOldState();

        return Mono.fromCallable(() -> {
            switch (type) {
                case GUILD_TEXT: return new TextChannelUpdateEvent(gateway, context.getShardInfo(),
                        getConvertibleChannel(gateway, channel),
                        oldData.map(old -> new TextChannel(gateway, old)).orElse(null));
                case DM:
                    throw new UnsupportedOperationException("Received channel_update for a DM on a bot account!");
                case GUILD_VOICE:
                case GUILD_STAGE_VOICE:
                    return new VoiceChannelUpdateEvent(gateway, context.getShardInfo(),
                            new VoiceChannel(gateway, channel),
                            oldData.map(old -> new VoiceChannel(gateway, old)).orElse(null));
                case GROUP_DM:
                    throw new UnsupportedOperationException("Received channel_update for a group on a bot account!");
                case GUILD_CATEGORY: return new CategoryUpdateEvent(gateway, context.getShardInfo(),
                        new Category(gateway, channel),
                        oldData.map(old -> new Category(gateway, old)).orElse(null));
                case GUILD_NEWS: return new NewsChannelUpdateEvent(gateway, context.getShardInfo(),
                        getConvertibleChannel(gateway, channel),
                        oldData.map(old -> new NewsChannel(gateway, old)).orElse(null));
                case GUILD_STORE: return new StoreChannelUpdateEvent(gateway, context.getShardInfo(),
                        new StoreChannel(gateway, channel),
                        oldData.map(old -> new StoreChannel(gateway, old)).orElse(null));
                case GUILD_FORUM: return new ForumChannelUpdateEvent(gateway, context.getShardInfo(),
                        new ForumChannel(gateway, channel),
                        oldData.map(old -> new ForumChannel(gateway, old)).orElse(null));
                default:
                    log.info("Received unknown channel update: {}", channel);
                    return new UnknownChannelUpdateEvent(gateway, context.getShardInfo(),
                        new UnknownChannel(gateway, channel),
                        oldData.map(old -> new UnknownChannel(gateway, old)).orElse(null));
            }
        });
    }

    private static GuildMessageChannel getConvertibleChannel(GatewayDiscordClient gateway, ChannelData channel) {
        switch (Channel.Type.of(channel.type())) {
            case GUILD_NEWS: return new NewsChannel(gateway, channel);
            case GUILD_TEXT: return new TextChannel(gateway, channel);
            default: throw new IllegalArgumentException("Unhandled channel type " + channel.type());
        }
    }
}
