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
import discord4j.core.object.ExtendedInvite;
import discord4j.core.object.data.ExtendedInviteBean;
import discord4j.core.object.data.stored.ChannelBean;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.InviteCreateSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Consumer;

class BaseCategorizableChannel extends BaseGuildChannel implements CategorizableChannel {

    BaseCategorizableChannel(ServiceMediator serviceMediator, ChannelBean data) {
        super(serviceMediator, data);
    }

    @Override
    public Optional<Snowflake> getCategoryId() {
        return Optional.ofNullable(getData().getParentId()).map(Snowflake::of);
    }

    @Override
    public Mono<Category> getCategory() {
        return Mono.justOrEmpty(getCategoryId()).flatMap(getClient()::getChannelById).cast(Category.class);
    }

    @Override
    public Mono<ExtendedInvite> createInvite(Consumer<? super InviteCreateSpec> spec) {
        final InviteCreateSpec mutatedSpec = new InviteCreateSpec();
        spec.accept(mutatedSpec);

        return getServiceMediator().getRestClient().getChannelService()
                .createChannelInvite(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason())
                .map(ExtendedInviteBean::new)
                .map(bean -> new ExtendedInvite(getServiceMediator(), bean))
                .subscriberContext(ctx -> ctx.put("shard", getServiceMediator().getClientConfig().getShardIndex()));
    }

    @Override
    public Flux<ExtendedInvite> getInvites() {
        return getServiceMediator().getRestClient().getChannelService()
                .getChannelInvites(getId().asLong())
                .map(ExtendedInviteBean::new)
                .map(bean -> new ExtendedInvite(getServiceMediator(), bean))
                .subscriberContext(ctx -> ctx.put("shard", getServiceMediator().getClientConfig().getShardIndex()));
    }
}
