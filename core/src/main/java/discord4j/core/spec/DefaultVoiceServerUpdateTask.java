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

package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceServerUpdateEvent;
import discord4j.voice.VoiceServerOptions;
import discord4j.voice.VoiceServerUpdateTask;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DefaultVoiceServerUpdateTask implements VoiceServerUpdateTask {

    private final GatewayDiscordClient gateway;

    public DefaultVoiceServerUpdateTask(GatewayDiscordClient gateway) {
        this.gateway = gateway;
    }

    @Override
    public Flux<VoiceServerOptions> onVoiceServerUpdates(Snowflake guildId) {
        //noinspection DataFlowIssue
        return gateway.getEventDispatcher()
                .on(VoiceServerUpdateEvent.class)
                .filter(vsu -> vsu.getGuildId().equals(guildId) && vsu.getEndpoint() != null)
                .map(vsu -> new VoiceServerOptions(vsu.getToken(), vsu.getEndpoint()));
    }

    @Override
    public Mono<VoiceServerOptions> onVoiceServerUpdate(Snowflake guildId) {
        return onVoiceServerUpdates(guildId).next();
    }
}
