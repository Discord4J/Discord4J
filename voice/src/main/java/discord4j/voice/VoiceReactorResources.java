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

    /**
     * Create Voice Gateway resources based off {@link ReactorResources} properties, and providing defaults for the
     * remaining properties.
     *
     * @param parent the resources instance to get properties from
     */
    public VoiceReactorResources(ReactorResources parent) {
        super(parent.getHttpClient(), parent.getTimerTaskScheduler(), parent.getBlockingTaskScheduler());
        this.udpClient = UdpClient.create();
        this.sendTaskScheduler = parent.getTimerTaskScheduler();
        this.receiveTaskScheduler = parent.getTimerTaskScheduler();
    }

    /**
     * Create Voice Gateway resources based off {@link ReactorResources} properties, and allowing customization of the
     * remaining properties.
     *
     * @param parent the resources instance to get properties from
     * @param udpClient the UDP client used to create voice protocol connections
     * @param sendTaskScheduler the scheduler used to run the voice send loop
     * @param receiveTaskScheduler the scheduler used to run the voice receive loop
     */
    public VoiceReactorResources(ReactorResources parent, UdpClient udpClient, Scheduler sendTaskScheduler,
                                 Scheduler receiveTaskScheduler) {
        super(parent.getHttpClient(), parent.getTimerTaskScheduler(), parent.getBlockingTaskScheduler());
        this.udpClient = udpClient;
        this.sendTaskScheduler = sendTaskScheduler;
        this.receiveTaskScheduler = receiveTaskScheduler;
    }

    /**
     * Create Voice Gateway resources allowing full customization of its properties.
     *
     * @param httpClient the HTTP client to use for initiating Gateway websocket connections. A default is provided
     * in {@link ReactorResources#DEFAULT_HTTP_CLIENT}
     * @param timerTaskScheduler the scheduler for timed tasks. A default can be created from
     * {@link ReactorResources#DEFAULT_TIMER_TASK_SCHEDULER}
     * @param blockingTaskScheduler the scheduler for blocking tasks. A default can be created from
     * {@link ReactorResources#DEFAULT_BLOCKING_TASK_SCHEDULER}
     * @param udpClient the UDP client used to create voice protocol connections
     * @param sendTaskScheduler the scheduler used to run the voice send loop
     * @param receiveTaskScheduler the scheduler used to run the voice receive loop
     */
    public VoiceReactorResources(HttpClient httpClient, Scheduler timerTaskScheduler, Scheduler blockingTaskScheduler,
                                 UdpClient udpClient, Scheduler sendTaskScheduler, Scheduler receiveTaskScheduler) {
        super(httpClient, timerTaskScheduler, blockingTaskScheduler);
        this.udpClient = udpClient;
        this.sendTaskScheduler = sendTaskScheduler;
        this.receiveTaskScheduler = receiveTaskScheduler;
    }

    protected VoiceReactorResources(Builder builder) {
        super(builder);

        this.udpClient = builder.udpClient == null ? UdpClient.create() : builder.udpClient;
        this.sendTaskScheduler = builder.sendTaskScheduler == null ? DEFAULT_TIMER_TASK_SCHEDULER.get() :
                builder.sendTaskScheduler;
        this.receiveTaskScheduler = builder.receiveTaskScheduler == null ? DEFAULT_TIMER_TASK_SCHEDULER.get() :
                builder.receiveTaskScheduler;
    }

    /**
     * Create a default set of Voice Gateway resources.
     *
     * @return a new {@link VoiceReactorResources} using all default properties
     */
    public static VoiceReactorResources create() {
        return new VoiceReactorResources(new ReactorResources());
    }

    /**
     * Returns a new builder to create {@link VoiceReactorResources}.
     *
     * @return a builder to create {@link VoiceReactorResources}
     */
    public static VoiceReactorResources.Builder builder() {
        return new VoiceReactorResources.Builder();
    }

    /**
     * Returns a new builder to create {@link VoiceReactorResources} from a pre-configured {@link ReactorResources},
     * copying its settings.
     *
     * @return a builder to create {@link VoiceReactorResources} with settings copied from parent resources
     */
    public static VoiceReactorResources.Builder builder(ReactorResources reactorResources) {
        return builder()
                .httpClient(reactorResources.getHttpClient())
                .timerTaskScheduler(reactorResources.getTimerTaskScheduler())
                .blockingTaskScheduler(reactorResources.getBlockingTaskScheduler());
    }

    /**
     * Returns a builder to create a new {@link VoiceReactorResources} with settings copied from the current
     * {@link VoiceReactorResources}.
     *
     * @return a builder based off this instance properties
     */
    public Builder mutate() {
        return new Builder()
                .httpClient(getHttpClient())
                .timerTaskScheduler(getTimerTaskScheduler())
                .blockingTaskScheduler(getBlockingTaskScheduler())
                .udpClient(getUdpClient())
                .sendTaskScheduler(getSendTaskScheduler())
                .receiveTaskScheduler(getReceiveTaskScheduler());
    }

    /**
     * Returns the UDP client used to create voice protocol connections.
     *
     * @return the UDP client
     */
    public UdpClient getUdpClient() {
        return udpClient;
    }

    /**
     * Returns the {@link Scheduler} used to run the voice send loop.
     *
     * @return the send scheduler
     */
    public Scheduler getSendTaskScheduler() {
        return sendTaskScheduler;
    }

    /**
     * Returns the {@link Scheduler} used to run the voice receive loop.
     *
     * @return the receive scheduler
     */
    public Scheduler getReceiveTaskScheduler() {
        return receiveTaskScheduler;
    }

    /**
     * Builder for {@link VoiceReactorResources}.
     */
    public static class Builder extends ReactorResources.Builder {

        private UdpClient udpClient;
        private Scheduler sendTaskScheduler;
        private Scheduler receiveTaskScheduler;

        protected Builder() {
        }

        /**
         * Set the UDP client used to create voice protocol connections.
         *
         * @param udpClient the UDP client
         * @return this builder
         */
        public Builder udpClient(UdpClient udpClient) {
            this.udpClient = udpClient;
            return this;
        }

        /**
         * Set the {@link Scheduler} used for voice send loop.
         *
         * @param sendTaskScheduler the voice send scheduler
         * @return this builder
         */
        public Builder sendTaskScheduler(Scheduler sendTaskScheduler) {
            this.sendTaskScheduler = sendTaskScheduler;
            return this;
        }

        /**
         * Set the {@link Scheduler} used for voice receive loop.
         *
         * @param receiveTaskScheduler the voice receive scheduler
         * @return this builder
         */
        public Builder receiveTaskScheduler(Scheduler receiveTaskScheduler) {
            this.receiveTaskScheduler = receiveTaskScheduler;
            return this;
        }

        @Override
        public Builder httpClient(HttpClient httpClient) {
            super.httpClient(httpClient);
            return this;
        }

        @Override
        public Builder timerTaskScheduler(Scheduler timerTaskScheduler) {
            super.timerTaskScheduler(timerTaskScheduler);
            return this;
        }

        @Override
        public Builder blockingTaskScheduler(Scheduler blockingTaskScheduler) {
            super.blockingTaskScheduler(blockingTaskScheduler);
            return this;
        }

        /**
         * Creates a new instance of {@link VoiceReactorResources}.
         *
         * @return a new instance of {@link VoiceReactorResources}
         */
        public VoiceReactorResources build() {
            return new VoiceReactorResources(this);
        }

    }
}
