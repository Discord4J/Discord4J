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

import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.StateHolder;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.channel.*;
import discord4j.core.object.data.stored.ChannelBean;
import discord4j.core.object.data.stored.GuildBean;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.*;
import discord4j.core.util.ArrayUtil;
import discord4j.gateway.json.dispatch.ChannelCreate;
import discord4j.gateway.json.dispatch.ChannelDelete;
import discord4j.gateway.json.dispatch.ChannelPinsUpdate;
import discord4j.gateway.json.dispatch.ChannelUpdate;
import discord4j.gateway.json.response.GatewayChannelResponse;
import discord4j.store.api.primitive.LongObjStore;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

class ChannelDispatchHandlers {

    static Mono<? extends Event> channelCreate(DispatchContext<ChannelCreate> context) {
        Channel.Type type = Channel.Type.of(context.getDispatch().getChannel().getType());

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
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getClient();
        GatewayChannelResponse channel = context.getDispatch().getChannel();
        long guildId = context.getDispatch().getGuildId();
        ChannelBean bean = new ChannelBean(channel, guildId);

        Mono<TextChannelCreateEvent> saveChannel = serviceMediator.getStateHolder().getChannelStore()
                .save(bean.getId(), bean)
                .thenReturn(new TextChannelCreateEvent(client, new TextChannel(serviceMediator, bean)));

        return addChannelToGuild(serviceMediator.getStateHolder().getGuildStore(), channel, guildId)
                .then(saveChannel);
    }

    private static Mono<PrivateChannelCreateEvent> privateChannelCreate(DispatchContext<ChannelCreate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getClient();
        ChannelBean bean = new ChannelBean(context.getDispatch().getChannel());

        return Mono.just(new PrivateChannelCreateEvent(client, new PrivateChannel(serviceMediator, bean)));
    }

    private static Mono<VoiceChannelCreateEvent> voiceChannelCreate(DispatchContext<ChannelCreate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getClient();
        GatewayChannelResponse channel = context.getDispatch().getChannel();
        long guildId = context.getDispatch().getGuildId();
        ChannelBean bean = new ChannelBean(channel, guildId);

        Mono<VoiceChannelCreateEvent> saveChannel = serviceMediator.getStateHolder().getChannelStore()
                .save(bean.getId(), bean)
                .thenReturn(new VoiceChannelCreateEvent(client, new VoiceChannel(serviceMediator, bean)));

        return addChannelToGuild(serviceMediator.getStateHolder().getGuildStore(), channel, guildId)
                .then(saveChannel);
    }

    private static Mono<CategoryCreateEvent> categoryCreate(DispatchContext<ChannelCreate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getClient();
        GatewayChannelResponse channel = context.getDispatch().getChannel();
        long guildId = context.getDispatch().getGuildId();
        ChannelBean bean = new ChannelBean(channel, guildId);

        Mono<CategoryCreateEvent> saveChannel = serviceMediator.getStateHolder().getChannelStore()
                .save(bean.getId(), bean)
                .thenReturn(new CategoryCreateEvent(client, new Category(serviceMediator, bean)));

        return addChannelToGuild(serviceMediator.getStateHolder().getGuildStore(), channel, guildId)
                .then(saveChannel);
    }

    private static Mono<NewsChannelCreateEvent> newsChannelCreate(DispatchContext<ChannelCreate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getClient();
        GatewayChannelResponse channel = context.getDispatch().getChannel();
        long guildId = context.getDispatch().getGuildId();
        ChannelBean bean = new ChannelBean(channel, guildId);

        Mono<NewsChannelCreateEvent> saveChannel = serviceMediator.getStateHolder().getChannelStore()
                .save(bean.getId(), bean)
                .thenReturn(new NewsChannelCreateEvent(client, new NewsChannel(serviceMediator, bean)));

        return addChannelToGuild(serviceMediator.getStateHolder().getGuildStore(), channel, guildId)
                .then(saveChannel);
    }

    private static Mono<StoreChannelCreateEvent> storeChannelCreate(DispatchContext<ChannelCreate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getClient();
        GatewayChannelResponse channel = context.getDispatch().getChannel();
        long guildId = context.getDispatch().getGuildId();
        ChannelBean bean = new ChannelBean(channel, guildId);

        Mono<StoreChannelCreateEvent> saveChannel = serviceMediator.getStateHolder().getChannelStore()
                .save(bean.getId(), bean)
                .thenReturn(new StoreChannelCreateEvent(client, new StoreChannel(serviceMediator, bean)));

        return addChannelToGuild(serviceMediator.getStateHolder().getGuildStore(), channel, guildId)
                .then(saveChannel);
    }

    private static Mono<Void> addChannelToGuild(LongObjStore<GuildBean> guildStore, GatewayChannelResponse channel,
                                                long guildId) {
        return guildStore
                .find(guildId)
                .map(GuildBean::new)
                .doOnNext(guild -> guild.setChannels(ArrayUtil.add(guild.getChannels(), channel.getId())))
                .flatMap(guild -> guildStore.save(guild.getId(), guild));
    }

    static Mono<? extends Event> channelDelete(DispatchContext<ChannelDelete> context) {
        Channel.Type type = Channel.Type.of(context.getDispatch().getChannel().getType());

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
        StateHolder stateHolder = context.getServiceMediator().getStateHolder();
        DiscordClient client = context.getServiceMediator().getClient();
        GatewayChannelResponse channel = context.getDispatch().getChannel();
        long guildId = context.getDispatch().getGuildId();
        ChannelBean bean = new ChannelBean(channel, guildId);

        Mono<TextChannelDeleteEvent> deleteChannel = stateHolder.getChannelStore()
                .delete(bean.getId())
                .thenReturn(new TextChannelDeleteEvent(client, new TextChannel(context.getServiceMediator(), bean)));

        return removeChannelFromGuild(stateHolder.getGuildStore(), channel, guildId)
                .then(deleteChannel);
    }

    private static Mono<PrivateChannelDeleteEvent> privateChannelDelete(DispatchContext<ChannelDelete> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = context.getServiceMediator().getClient();
        ChannelBean bean = new ChannelBean(context.getDispatch().getChannel());

        return Mono.just(new PrivateChannelDeleteEvent(client, new PrivateChannel(serviceMediator, bean)));

    }

    private static Mono<VoiceChannelDeleteEvent> voiceChannelDelete(DispatchContext<ChannelDelete> context) {
        StateHolder stateHolder = context.getServiceMediator().getStateHolder();
        DiscordClient client = context.getServiceMediator().getClient();
        GatewayChannelResponse channel = context.getDispatch().getChannel();
        long guildId = context.getDispatch().getGuildId();
        ChannelBean bean = new ChannelBean(channel, guildId);

        Mono<VoiceChannelDeleteEvent> deleteChannel = stateHolder.getChannelStore()
                .delete(bean.getId())
                .thenReturn(new VoiceChannelDeleteEvent(client, new VoiceChannel(context.getServiceMediator(), bean)));

        return removeChannelFromGuild(stateHolder.getGuildStore(), channel, guildId)
                .then(deleteChannel);
    }

    private static Mono<CategoryDeleteEvent> categoryDelete(DispatchContext<ChannelDelete> context) {
        StateHolder stateHolder = context.getServiceMediator().getStateHolder();
        DiscordClient client = context.getServiceMediator().getClient();
        GatewayChannelResponse channel = context.getDispatch().getChannel();
        long guildId = context.getDispatch().getGuildId();
        ChannelBean bean = new ChannelBean(channel, guildId);

        Mono<CategoryDeleteEvent> deleteChannel = stateHolder.getChannelStore()
                .delete(bean.getId())
                .thenReturn(new CategoryDeleteEvent(client, new Category(context.getServiceMediator(), bean)));

        return removeChannelFromGuild(stateHolder.getGuildStore(), channel, guildId)
                .then(deleteChannel);
    }

    private static Mono<NewsChannelDeleteEvent> newsChannelDelete(DispatchContext<ChannelDelete> context) {
        StateHolder stateHolder = context.getServiceMediator().getStateHolder();
        DiscordClient client = context.getServiceMediator().getClient();
        GatewayChannelResponse channel = context.getDispatch().getChannel();
        long guildId = context.getDispatch().getGuildId();
        ChannelBean bean = new ChannelBean(channel, guildId);

        Mono<NewsChannelDeleteEvent> deleteChannel = stateHolder.getChannelStore()
                .delete(bean.getId())
                .thenReturn(new NewsChannelDeleteEvent(client, new NewsChannel(context.getServiceMediator(), bean)));

        return removeChannelFromGuild(stateHolder.getGuildStore(), channel, guildId)
                .then(deleteChannel);
    }

    private static Mono<StoreChannelDeleteEvent> storeChannelDelete(DispatchContext<ChannelDelete> context) {
        StateHolder stateHolder = context.getServiceMediator().getStateHolder();
        DiscordClient client = context.getServiceMediator().getClient();
        GatewayChannelResponse channel = context.getDispatch().getChannel();
        long guildId = context.getDispatch().getGuildId();
        ChannelBean bean = new ChannelBean(channel, guildId);

        Mono<StoreChannelDeleteEvent> deleteChannel = stateHolder.getChannelStore()
                .delete(bean.getId())
                .thenReturn(new StoreChannelDeleteEvent(client, new StoreChannel(context.getServiceMediator(), bean)));

        return removeChannelFromGuild(stateHolder.getGuildStore(), channel, guildId)
                .then(deleteChannel);
    }

    private static Mono<Void> removeChannelFromGuild(LongObjStore<GuildBean> guildStore, GatewayChannelResponse channel,
                                                     long guildId) {
        return guildStore
                .find(guildId)
                .map(GuildBean::new)
                .doOnNext(guild -> guild.setChannels(ArrayUtil.remove(guild.getChannels(), channel.getId())))
                .flatMap(guild -> guildStore.save(guild.getId(), guild));
    }

    static Mono<PinsUpdateEvent> channelPinsUpdate(DispatchContext<ChannelPinsUpdate> context) {
        long channelId = context.getDispatch().getChannelId();
        Instant timestamp = context.getDispatch().getLastPinTimestamp() == null ? null :
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(context.getDispatch().getLastPinTimestamp(),
                        Instant::from);

        return Mono.just(new PinsUpdateEvent(context.getServiceMediator().getClient(), channelId, timestamp));
    }

    static Mono<? extends Event> channelUpdate(DispatchContext<ChannelUpdate> context) {
        Channel.Type type = Channel.Type.of(context.getDispatch().getChannel().getType());

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
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getClient();
        GatewayChannelResponse channel = context.getDispatch().getChannel();
        long guildId = context.getDispatch().getGuildId();
        ChannelBean bean = new ChannelBean(channel, guildId);
        GuildMessageChannel current = getConvertibleChannel(serviceMediator, bean);

        Mono<Void> saveNew = serviceMediator.getStateHolder().getChannelStore().save(bean.getId(), bean);

        return serviceMediator.getStateHolder().getChannelStore()
                .find(bean.getId())
                .flatMap(saveNew::thenReturn)
                .map(old -> new TextChannelUpdateEvent(client, current, new TextChannel(serviceMediator, old)))
                .switchIfEmpty(saveNew.thenReturn(new TextChannelUpdateEvent(client, current, null)));
    }

    private static Mono<VoiceChannelUpdateEvent> voiceChannelUpdate(DispatchContext<ChannelUpdate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getClient();
        GatewayChannelResponse channel = context.getDispatch().getChannel();
        long guildId = context.getDispatch().getGuildId();
        ChannelBean bean = new ChannelBean(channel, guildId);
        VoiceChannel current = new VoiceChannel(context.getServiceMediator(), bean);

        Mono<Void> saveNew = serviceMediator.getStateHolder().getChannelStore().save(bean.getId(), bean);

        return serviceMediator.getStateHolder().getChannelStore()
                .find(bean.getId())
                .flatMap(saveNew::thenReturn)
                .map(old -> new VoiceChannelUpdateEvent(client, current, new VoiceChannel(serviceMediator, old)))
                .switchIfEmpty(saveNew.thenReturn(new VoiceChannelUpdateEvent(client, current, null)));
    }

    private static Mono<CategoryUpdateEvent> categoryUpdate(DispatchContext<ChannelUpdate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getClient();
        GatewayChannelResponse channel = context.getDispatch().getChannel();
        long guildId = context.getDispatch().getGuildId();
        ChannelBean bean = new ChannelBean(channel, guildId);
        Category current = new Category(context.getServiceMediator(), bean);

        Mono<Void> saveNew = serviceMediator.getStateHolder().getChannelStore().save(bean.getId(), bean);

        return serviceMediator.getStateHolder().getChannelStore()
                .find(bean.getId())
                .flatMap(saveNew::thenReturn)
                .map(old -> new CategoryUpdateEvent(client, current, new Category(serviceMediator, old)))
                .switchIfEmpty(saveNew.thenReturn(new CategoryUpdateEvent(client, current, null)));
    }

    private static Mono<NewsChannelUpdateEvent> newsChannelUpdate(DispatchContext<ChannelUpdate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getClient();
        GatewayChannelResponse channel = context.getDispatch().getChannel();
        long guildId = context.getDispatch().getGuildId();
        ChannelBean bean = new ChannelBean(channel, guildId);
        GuildMessageChannel current = getConvertibleChannel(serviceMediator, bean);

        Mono<Void> saveNew = serviceMediator.getStateHolder().getChannelStore().save(bean.getId(), bean);

        return serviceMediator.getStateHolder().getChannelStore()
                .find(bean.getId())
                .flatMap(saveNew::thenReturn)
                .map(old -> new NewsChannelUpdateEvent(client, current, new NewsChannel(serviceMediator, old)))
                .switchIfEmpty(saveNew.thenReturn(new NewsChannelUpdateEvent(client, current, null)));
    }

    private static Mono<StoreChannelUpdateEvent> storeChannelUpdate(DispatchContext<ChannelUpdate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getClient();
        GatewayChannelResponse channel = context.getDispatch().getChannel();
        long guildId = context.getDispatch().getGuildId();
        ChannelBean bean = new ChannelBean(channel, guildId);
        StoreChannel current = new StoreChannel(serviceMediator, bean);

        Mono<Void> saveNew = serviceMediator.getStateHolder().getChannelStore().save(bean.getId(), bean);

        return serviceMediator.getStateHolder().getChannelStore()
                .find(bean.getId())
                .flatMap(saveNew::thenReturn)
                .map(old -> new StoreChannelUpdateEvent(client, current, new StoreChannel(serviceMediator, old)))
                .switchIfEmpty(saveNew.thenReturn(new StoreChannelUpdateEvent(client, current, null)));
    }

    private static GuildMessageChannel getConvertibleChannel(ServiceMediator serviceMediator, ChannelBean bean) {
        switch (Channel.Type.of(bean.getType())) {
            case GUILD_NEWS: return new NewsChannel(serviceMediator, bean);
            case GUILD_TEXT: return new TextChannel(serviceMediator, bean);
            default: throw new AssertionError();
        }
    }
}
