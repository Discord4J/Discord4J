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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.Region;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.GuildCreateRequest;
import discord4j.discordjson.json.PartialChannelCreateRequest;
import discord4j.discordjson.json.RoleCreateRequest;
import discord4j.rest.util.Image;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapNullable;

@Value.Immutable
interface GuildCreateSpecGenerator extends Spec<GuildCreateRequest> {

    String name();

    Region region();

    @Nullable
    Image icon();

    @Value.Default
    default Guild.VerificationLevel verificationLevel() {
        return Guild.VerificationLevel.NONE;
    }

    @Value.Default
    default Guild.NotificationLevel defaultMessageNotificationLevel() {
        return Guild.NotificationLevel.ALL_MESSAGES;
    }

    @Value.Default
    default Guild.ContentFilterLevel explicitContentFilter() {
        return Guild.ContentFilterLevel.DISABLED;
    }

    @Value.Default
    default /*~~>*/List<RoleCreateSpec> roles() {
        return Collections.emptyList();
    }

    @Nullable
    RoleCreateSpec everyoneRole();

    @Value.Default
    default /*~~>*/List<GuildCreateFields.PartialChannel> channels() {
        return Collections.emptyList();
    }

    @Override
    default GuildCreateRequest asRequest() {
        List<RoleCreateRequest> roles = roles().stream()
                .map(RoleCreateSpec::asRequest)
                .collect(Collectors.toCollection(ArrayList::new));
        RoleCreateSpec everyoneRole = everyoneRole();
        if (everyoneRole != null) {
            roles.add(0, everyoneRole.asRequest());
        }
        List<PartialChannelCreateRequest> channels = channels().stream()
                .map(GuildCreateFields.PartialChannel::asRequest)
                .collect(Collectors.toList());
        return GuildCreateRequest.builder()
                .name(name())
                .region(region().getId())
                .icon(mapNullable(icon(), Image::getDataUri))
                .verificationLevel(verificationLevel().getValue())
                .defaultMessageNotifications(defaultMessageNotificationLevel().getValue())
                .explicitContentFilter(explicitContentFilter().getValue())
                .roles(roles)
                .channels(channels)
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class GuildCreateMonoGenerator extends Mono<Guild> implements GuildCreateSpecGenerator {

    abstract GatewayDiscordClient gateway();

    @Override
    public void subscribe(CoreSubscriber<? super Guild> actual) {
        gateway().createGuild(GuildCreateSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
