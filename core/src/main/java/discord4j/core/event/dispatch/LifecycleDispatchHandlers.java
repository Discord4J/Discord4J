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

import discord4j.core.event.domain.lifecycle.*;
import discord4j.core.object.data.stored.UserBean;
import discord4j.core.object.entity.User;
import discord4j.gateway.json.dispatch.Ready;
import discord4j.gateway.json.dispatch.Resumed;
import discord4j.gateway.retry.GatewayStateChange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

class LifecycleDispatchHandlers {

    static Mono<ReadyEvent> ready(DispatchContext<Ready> context) {
        Ready dispatch = context.getDispatch();
        UserBean userBean = new UserBean(dispatch.getUser());

        User self = new User(context.getServiceMediator(), userBean);
        Set<ReadyEvent.Guild> guilds = Arrays.stream(dispatch.getGuilds())
                .map(g -> new ReadyEvent.Guild(g.getId(), g.isUnavailable()))
                .collect(Collectors.toSet());

        //FIXME: This should be placed on disconnects, not on ready
        Mono<Void> invalidateStores = context.getServiceMediator().getStateHolder().invalidateStores();

        Mono<Void> saveUser = context.getServiceMediator().getStateHolder().getUserStore()
                .save(context.getDispatch().getUser().getId(), userBean);

        Mono<Void> saveSelfId = Mono.fromRunnable(() ->
                context.getServiceMediator().getStateHolder().getSelfId().set(userBean.getId()));

        return invalidateStores
                .then(saveUser)
                .then(saveSelfId)
                .thenReturn(new ReadyEvent(context.getServiceMediator().getClient(), dispatch.getVersion(), self,
                        guilds, dispatch.getSessionId(), dispatch.getTrace()));
    }

    static Mono<ResumeEvent> resumed(DispatchContext<Resumed> context) {
        return Mono.just(new ResumeEvent(context.getServiceMediator().getClient(), context.getDispatch().getTrace()));
    }

    static Mono<? extends GatewayLifecycleEvent> gatewayStateChanged(DispatchContext<GatewayStateChange> context) {
        GatewayStateChange dispatch = context.getDispatch();
        switch (dispatch.getState()) {
            case CONNECTED:
                return Mono.just(new ConnectEvent(context.getServiceMediator().getClient()));
            case RETRY_STARTED:
                return Mono.just(new ReconnectStartEvent(context.getServiceMediator().getClient()));
            case RETRY_FAILED:
                return Mono.just(new ReconnectFailEvent(context.getServiceMediator().getClient(),
                        dispatch.getCurrentAttempt()));
            case RETRY_SUCCEEDED:
                return Mono.just(new ReconnectEvent(context.getServiceMediator().getClient(),
                        dispatch.getCurrentAttempt()));
            case DISCONNECTED:
                return Mono.just(new DisconnectEvent(context.getServiceMediator().getClient()));
        }
        return Mono.empty();
    }

}
