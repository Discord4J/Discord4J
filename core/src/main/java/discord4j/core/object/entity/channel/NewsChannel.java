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

import discord4j.core.ServiceMediator;
import discord4j.core.object.data.stored.ChannelBean;
import discord4j.core.spec.NewsChannelEditSpec;
import discord4j.core.util.EntityUtil;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/** A Discord news channel. */
public final class NewsChannel extends BaseGuildMessageChannel {

    /**
     * Constructs an {@code NewsChannel} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public NewsChannel(ServiceMediator serviceMediator, ChannelBean data) {
        super(serviceMediator, data);
    }

    /**
     * Requests to edit this news channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link NewsChannelEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link NewsChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<NewsChannel> edit(final Consumer<? super NewsChannelEditSpec> spec) {
        final NewsChannelEditSpec mutatedSpec = new NewsChannelEditSpec();
        spec.accept(mutatedSpec);

        return getServiceMediator().getRestClient().getChannelService()
                .modifyChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason())
                .map(ChannelBean::new)
                .map(bean -> EntityUtil.getChannel(getServiceMediator(), bean))
                .cast(NewsChannel.class)
                .subscriberContext(ctx -> ctx.put("shard", getServiceMediator().getClientConfig().getShardIndex()));
    }

    @Override
    public String toString() {
        return "NewsChannel{} " + super.toString();
    }
}
