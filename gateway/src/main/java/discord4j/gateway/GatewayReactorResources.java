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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.gateway;

import discord4j.common.ReactorResources;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class GatewayReactorResources extends ReactorResources {

    private final Scheduler payloadSenderScheduler;

    public GatewayReactorResources(ReactorResources parent) {
        super(parent.getHttpClient(), parent.getTimerTaskScheduler(), parent.getBlockingTaskScheduler());
        this.payloadSenderScheduler = Schedulers.newSingle("d4j-gateway", true);
    }

    public GatewayReactorResources(ReactorResources parent, Scheduler payloadSenderScheduler) {
        super(parent.getHttpClient(), parent.getTimerTaskScheduler(), parent.getBlockingTaskScheduler());
        this.payloadSenderScheduler = payloadSenderScheduler;
    }

    public static GatewayReactorResources create() {
        return new GatewayReactorResources(new ReactorResources());
    }

    public Scheduler getPayloadSenderScheduler() {
        return payloadSenderScheduler;
    }
}
