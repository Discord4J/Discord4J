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

import discord4j.common.json.payload.dispatch.Ready;
import discord4j.common.json.payload.dispatch.Resumed;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.lifecycle.*;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.bean.UserBean;
import discord4j.gateway.retry.GatewayStateChange;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

class LifecycleDispatchHandlers {

    static Flux<ReadyEvent> ready(DispatchContext<Ready> context) {
        Ready dispatch = context.getDispatch();
        User self = new User(context.getServiceMediator(), new UserBean(dispatch.getUser()));
        Set<ReadyEvent.Guild> guilds = Arrays.stream(dispatch.getGuilds())
                .map(g -> new ReadyEvent.Guild(g.getId(), g.isUnavailable()))
                .collect(Collectors.toSet());

        return Flux.just(new ReadyEvent(context.getServiceMediator().getDiscordClient(), dispatch.getVersion(), self,
                guilds, dispatch.getSessionId(), dispatch.getTrace()));
    }

    static Flux<ResumeEvent> resumed(DispatchContext<Resumed> context) {
        return Flux.just(new ResumeEvent(context.getServiceMediator().getDiscordClient(),
                context.getDispatch().getTrace()));
    }

    static Flux<? extends GatewayLifecycleEvent> gatewayStateChanged(DispatchContext<GatewayStateChange> context) {
        GatewayStateChange dispatch = context.getDispatch();
        switch (dispatch.getState()) {
            case CONNECTED:
                return Flux.just(new ConnectEvent(context.getServiceMediator().getDiscordClient()));
            case RETRY_STARTED:
                return Flux.just(new ReconnectStartEvent(context.getServiceMediator().getDiscordClient()));
            case RETRY_FAILED:
                return Flux.just(new ReconnectFailEvent(context.getServiceMediator().getDiscordClient(),
                        dispatch.getCurrentAttempt()));
            case RETRY_SUCCEEDED:
                return Flux.just(new ReconnectEvent(context.getServiceMediator().getDiscordClient(),
                        dispatch.getCurrentAttempt()));
            case DISCONNECTED:
                return Flux.just(new DisconnectEvent(context.getServiceMediator().getDiscordClient()));
        }
        return Flux.empty();
    }

}
