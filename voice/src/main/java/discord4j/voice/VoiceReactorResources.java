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

package discord4j.voice;

import discord4j.common.ReactorResources;
import reactor.core.scheduler.Scheduler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.udp.UdpClient;

/**
 * Provides an extra level of configuration for {@link ReactorResources}, tailored for the Voice Gateway operations.
 * <p>
 * Allows customizing the {@link Scheduler} used to send and receive voice packets and also the {@link UdpClient}
 * template for establishing UDP connections.
 */
public class VoiceReactorResources extends ReactorResources {

    private final UdpClient udpClient;
    private final Scheduler sendTaskScheduler;
    private final Scheduler receiveTaskScheduler;

    public VoiceReactorResources(ReactorResources parent) {
        super(parent.getHttpClient(), parent.getTimerTaskScheduler(), parent.getBlockingTaskScheduler());
        this.udpClient = UdpClient.create();
        this.sendTaskScheduler = parent.getTimerTaskScheduler();
        this.receiveTaskScheduler = parent.getTimerTaskScheduler();
    }

    public VoiceReactorResources(ReactorResources parent, UdpClient udpClient, Scheduler sendTaskScheduler,
                                 Scheduler receiveTaskScheduler) {
        super(parent.getHttpClient(), parent.getTimerTaskScheduler(), parent.getBlockingTaskScheduler());
        this.udpClient = udpClient;
        this.sendTaskScheduler = sendTaskScheduler;
        this.receiveTaskScheduler = receiveTaskScheduler;
    }

    public VoiceReactorResources(HttpClient httpClient, Scheduler timerTaskScheduler, Scheduler blockingTaskScheduler,
                                 UdpClient udpClient, Scheduler sendTaskScheduler, Scheduler receiveTaskScheduler) {
        super(httpClient, timerTaskScheduler, blockingTaskScheduler);
        this.udpClient = udpClient;
        this.sendTaskScheduler = sendTaskScheduler;
        this.receiveTaskScheduler = receiveTaskScheduler;
    }

    public UdpClient getUdpClient() {
        return udpClient;
    }

    public Scheduler getSendTaskScheduler() {
        return sendTaskScheduler;
    }

    public Scheduler getReceiveTaskScheduler() {
        return receiveTaskScheduler;
    }
}
