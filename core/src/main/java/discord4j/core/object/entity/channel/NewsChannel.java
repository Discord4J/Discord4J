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
package discord4j.core.object.entity.channel;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.FollowedChannel;
import discord4j.core.spec.NewsChannelEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.NewsChannelFollowRequest;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/** A Discord news channel. */
public final class NewsChannel extends BaseGuildMessageChannel {

    /**
     * Constructs an {@code NewsChannel} with an associated ServiceMediator and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public NewsChannel(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
    }

    /**
     * Requests to edit this news channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link NewsChannelEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link NewsChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<NewsChannel> edit(final Consumer<? super NewsChannelEditSpec> spec) {
        return Mono.defer(
                () -> {
                    NewsChannelEditSpec mutatedSpec = new NewsChannelEditSpec();
                    spec.accept(mutatedSpec);
                    return getClient().getRestClient().getChannelService()
                            .modifyChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> EntityUtil.getChannel(getClient(), data))
                .cast(NewsChannel.class);
    }

    /**
     * Requests to follow this news channel. Following will create a webhook in the channel which ID is specified.
     * Requires 'MANAGE_WEBHOOKS' permission.
     *
     * @param targetChannelId the ID of the channel where to create the follow webhook
     * @return a {@link FollowedChannel} object containing a reference to this news channel and allowing to retrieve the
     * webhook created.
     */
    public Mono<FollowedChannel> follow(Snowflake targetChannelId) {
        return getClient().getRestClient().getChannelService()
                .followNewsChannel(getId().asLong(), NewsChannelFollowRequest.builder()
                        .webhookChannelId(targetChannelId.asString())
                        .build())
                .map(data -> new FollowedChannel(getClient(), data));
    }

    @Override
    public String toString() {
        return "NewsChannel{} " + super.toString();
    }
}
