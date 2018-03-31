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

import discord4j.common.json.payload.dispatch.*;
import discord4j.common.json.response.ChannelResponse;
import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.channel.*;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.bean.*;
import discord4j.core.store.StoreHolder;
import discord4j.core.util.ArrayUtil;
import discord4j.store.primitive.LongObjStore;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Objects;

class ChannelDispatchHandlers {

    static Flux<? extends Event> channelCreate(DispatchContext<ChannelCreate> context) {
        Channel.Type type = Channel.Type.of(context.getDispatch().getChannel().getType());

        switch (type) {
            case GUILD_TEXT: return textChannelCreate(context);
            case DM: return privateChannelCreate(context);
            case GUILD_VOICE: return voiceChannelCreate(context);
            case GROUP_DM:
                throw new UnsupportedOperationException("Received channel_create for group on a bot account!");
            case GUILD_CATEGORY: return categoryCreateEvent(context);
            default: throw new AssertionError();
        }
    }

    private static Flux<TextChannelCreateEvent> textChannelCreate(DispatchContext<ChannelCreate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getDiscordClient();
        TextChannelBean bean = new TextChannelBean(context.getDispatch().getChannel());

        Mono<TextChannelCreateEvent> saveChannel = serviceMediator.getStoreHolder().getTextChannelStore()
                .save(bean.getId(), bean)
                .thenReturn(new TextChannelCreateEvent(client, new TextChannel(serviceMediator, bean)));

        return addChannelToGuild(serviceMediator.getStoreHolder().getGuildStore(), context.getDispatch().getChannel())
                .then(saveChannel)
                .flux();
    }

    private static Flux<PrivateChannelCreateEvent> privateChannelCreate(DispatchContext<ChannelCreate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getDiscordClient();
        PrivateChannelBean bean = new PrivateChannelBean(context.getDispatch().getChannel());

        return serviceMediator.getStoreHolder().getPrivateChannelStore()
                .save(bean.getId(), bean)
                .thenReturn(new PrivateChannelCreateEvent(client, new PrivateChannel(serviceMediator, bean)))
                .flux();
    }

    private static Flux<VoiceChannelCreateEvent> voiceChannelCreate(DispatchContext<ChannelCreate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getDiscordClient();
        VoiceChannelBean bean = new VoiceChannelBean(context.getDispatch().getChannel());

        Mono<VoiceChannelCreateEvent> saveChannel = serviceMediator.getStoreHolder().getVoiceChannelStore()
                .save(bean.getId(), bean)
                .thenReturn(new VoiceChannelCreateEvent(client, new VoiceChannel(serviceMediator, bean)));

        return addChannelToGuild(serviceMediator.getStoreHolder().getGuildStore(), context.getDispatch().getChannel())
                .then(saveChannel)
                .flux();
    }

    private static Flux<CategoryCreateEvent> categoryCreateEvent(DispatchContext<ChannelCreate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getDiscordClient();
        CategoryBean bean = new CategoryBean(context.getDispatch().getChannel());

        Mono<CategoryCreateEvent> saveChannel = serviceMediator.getStoreHolder().getCategoryStore()
                .save(bean.getId(), bean)
                .thenReturn(new CategoryCreateEvent(client, new Category(serviceMediator, bean)));

        return addChannelToGuild(serviceMediator.getStoreHolder().getGuildStore(), context.getDispatch().getChannel())
                .then(saveChannel)
                .flux();
    }

    private static Mono<Void> addChannelToGuild(LongObjStore<GuildBean> guildStore, ChannelResponse channel) {
        return guildStore
                .find(Objects.requireNonNull(channel.getGuildId()))
                .doOnNext(guild -> {

                    guild.setChannels(ArrayUtil.add(guild.getChannels(), channel.getId()));
                })
                .flatMap(guild -> guildStore.save(guild.getId(), guild));
    }

    static Flux<? extends Event> channelDelete(DispatchContext<ChannelDelete> context) {
        Channel.Type type = Channel.Type.of(context.getDispatch().getChannel().getType());

        switch (type) {
            case GUILD_TEXT: return textChannelDelete(context);
            case DM: return privateChannelDelete(context);
            case GUILD_VOICE: return voiceChannelDelete(context);
            case GROUP_DM:
                throw new UnsupportedOperationException("Received channel_delete for a group on a bot account!");
            case GUILD_CATEGORY: return categoryDeleteEvent(context);
            default: throw new AssertionError();
        }
    }

    private static Flux<TextChannelDeleteEvent> textChannelDelete(DispatchContext<ChannelDelete> context) {
        StoreHolder storeHolder = context.getServiceMediator().getStoreHolder();
        DiscordClient client = context.getServiceMediator().getDiscordClient();
        TextChannelBean bean = new TextChannelBean(context.getDispatch().getChannel());

        Mono<TextChannelDeleteEvent> deleteChannel = storeHolder.getTextChannelStore()
                .delete(bean.getId())
                .thenReturn(new TextChannelDeleteEvent(client, new TextChannel(context.getServiceMediator(), bean)));

        return removeChannelFromGuild(storeHolder.getGuildStore(), context.getDispatch().getChannel())
                .then(deleteChannel)
                .flux();
    }

    private static Flux<PrivateChannelDeleteEvent> privateChannelDelete(DispatchContext<ChannelDelete> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = context.getServiceMediator().getDiscordClient();
        PrivateChannelBean bean = new PrivateChannelBean(context.getDispatch().getChannel());

        return serviceMediator.getStoreHolder().getPrivateChannelStore()
                .delete(bean.getId())
                .thenReturn(new PrivateChannelDeleteEvent(client, new PrivateChannel(serviceMediator, bean)))
                .flux();
    }

    private static Flux<VoiceChannelDeleteEvent> voiceChannelDelete(DispatchContext<ChannelDelete> context) {
        StoreHolder storeHolder = context.getServiceMediator().getStoreHolder();
        DiscordClient client = context.getServiceMediator().getDiscordClient();
        VoiceChannelBean bean = new VoiceChannelBean(context.getDispatch().getChannel());

        Mono<VoiceChannelDeleteEvent> deleteChannel = storeHolder.getVoiceChannelStore()
                .delete(bean.getId())
                .thenReturn(new VoiceChannelDeleteEvent(client, new VoiceChannel(context.getServiceMediator(), bean)));

        return removeChannelFromGuild(storeHolder.getGuildStore(), context.getDispatch().getChannel())
                .then(deleteChannel)
                .flux();
    }

    private static Flux<CategoryDeleteEvent> categoryDeleteEvent(DispatchContext<ChannelDelete> context) {
        StoreHolder storeHolder = context.getServiceMediator().getStoreHolder();
        DiscordClient client = context.getServiceMediator().getDiscordClient();
        CategoryBean bean = new CategoryBean(context.getDispatch().getChannel());

        Mono<CategoryDeleteEvent> deleteChannel = storeHolder.getCategoryStore()
                .delete(bean.getId())
                .thenReturn(new CategoryDeleteEvent(client, new Category(context.getServiceMediator(), bean)));

        return removeChannelFromGuild(storeHolder.getGuildStore(), context.getDispatch().getChannel())
                .then(deleteChannel)
                .flux();
    }

    private static Mono<Void> removeChannelFromGuild(LongObjStore<GuildBean> guildStore, ChannelResponse channel) {
        return guildStore
                .find(Objects.requireNonNull(channel.getGuildId()))
                .doOnNext(guild -> guild.setChannels(ArrayUtil.remove(guild.getChannels(), channel.getId())))
                .flatMap(guild -> guildStore.save(guild.getId(), guild));
    }

    static Flux<PinsUpdateEvent> channelPinsUpdate(DispatchContext<ChannelPinsUpdate> context) {
        long channelId = context.getDispatch().getChannelId();
        Instant timestamp = context.getDispatch().getLastPinTimestamp() == null ? null :
                Instant.parse(context.getDispatch().getLastPinTimestamp());

        return Flux.just(new PinsUpdateEvent(context.getServiceMediator().getDiscordClient(), channelId, timestamp));
    }

    static Flux<? extends Event> channelUpdate(DispatchContext<ChannelUpdate> context) {
        Channel.Type type = Channel.Type.of(context.getDispatch().getChannel().getType());

        switch (type) {
            case GUILD_TEXT: return textChannelUpdate(context);
            case DM:
                throw new UnsupportedOperationException("Received channel_update for a DM on a bot account!");
            case GUILD_VOICE: return voiceChannelUpdate(context);
            case GROUP_DM:
                throw new UnsupportedOperationException("Received channel_update for a group on a bot account!");
            case GUILD_CATEGORY: return categoryUpdateEvent(context);
            default: throw new AssertionError();
        }
    }

    private static Flux<TextChannelUpdateEvent> textChannelUpdate(DispatchContext<ChannelUpdate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getDiscordClient();
        TextChannelBean bean = new TextChannelBean(context.getDispatch().getChannel());
        TextChannel current = new TextChannel(context.getServiceMediator(), bean);

        Mono<Void> saveNew = serviceMediator.getStoreHolder().getTextChannelStore().save(bean.getId(), bean);

        return serviceMediator.getStoreHolder().getTextChannelStore()
                .find(bean.getId())
                .flatMap(saveNew::thenReturn)
                .map(old -> new TextChannelUpdateEvent(client, current, new TextChannel(serviceMediator, old)))
                .switchIfEmpty(saveNew.thenReturn(new TextChannelUpdateEvent(client, current, null)))
                .flux();
    }

    private static Flux<VoiceChannelUpdateEvent> voiceChannelUpdate(DispatchContext<ChannelUpdate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getDiscordClient();
        VoiceChannelBean bean = new VoiceChannelBean(context.getDispatch().getChannel());
        VoiceChannel current = new VoiceChannel(context.getServiceMediator(), bean);

        Mono<Void> saveNew = serviceMediator.getStoreHolder().getVoiceChannelStore().save(bean.getId(), bean);

        return serviceMediator.getStoreHolder().getVoiceChannelStore()
                .find(bean.getId())
                .flatMap(saveNew::thenReturn)
                .map(old -> new VoiceChannelUpdateEvent(client, current, new VoiceChannel(serviceMediator, old)))
                .switchIfEmpty(saveNew.thenReturn(new VoiceChannelUpdateEvent(client, current, null)))
                .flux();
    }

    private static Flux<CategoryUpdateEvent> categoryUpdateEvent(DispatchContext<ChannelUpdate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getDiscordClient();
        CategoryBean bean = new CategoryBean(context.getDispatch().getChannel());
        Category current = new Category(context.getServiceMediator(), bean);

        Mono<Void> saveNew = serviceMediator.getStoreHolder().getCategoryStore().save(bean.getId(), bean);

        return serviceMediator.getStoreHolder().getCategoryStore()
                .find(bean.getId())
                .flatMap(saveNew::thenReturn)
                .map(old -> new CategoryUpdateEvent(client, current, new Category(serviceMediator, old)))
                .switchIfEmpty(saveNew.thenReturn(new CategoryUpdateEvent(client, current, null)))
                .flux();
    }

}
