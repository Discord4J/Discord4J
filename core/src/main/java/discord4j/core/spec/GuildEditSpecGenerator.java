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
import discord4j.core.object.Region;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.GuildModifyRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;
import static discord4j.core.spec.InternalSpecUtils.mapPossibleOptional;

@Value.Immutable(singleton = true)
interface GuildEditSpecGenerator extends AuditSpec<GuildModifyRequest> {

    Possible<String> name();
    
    Possible<Optional<Region>> region();
    
    Possible<Optional<Guild.VerificationLevel>> verificationLevel();
    
    Possible<Optional<Guild.NotificationLevel>> defaultMessageNotificationsLevel();

    Possible<Optional<Guild.ContentFilterLevel>> explicitContentFilter();
    
    Possible<Optional<Snowflake>> afkChannelId();

    Possible<Integer> afkTimeout();
    
    Possible<Optional<Image>> icon();

    Possible<Snowflake> ownerId();
    
    Possible<Optional<Image>> splash();
    
    Possible<Optional<Image>> discoverySplash();
    
    Possible<Optional<Image>> banner();
    
    Possible<Optional<Snowflake>> systemChannelId();
    
    Possible<Guild.SystemChannelFlag> systemChannelFlags();
    
    Possible<Optional<Snowflake>> rulesChannelId();
    
    Possible<Optional<Snowflake>> publicUpdatesChannelId();
    
    Possible<Optional<Locale>> preferredLocale();
    
    Possible</*~~>*/List<String>> features();
    
    Possible<Optional<String>> description();

    @Override
    default GuildModifyRequest asRequest() {
        return GuildModifyRequest.builder()
                .name(name())
                .region(mapPossibleOptional(region(), Region::getId))
                .verificationLevel(mapPossibleOptional(verificationLevel(), Guild.VerificationLevel::getValue))
                .defaultMessageNotifications(mapPossibleOptional(defaultMessageNotificationsLevel(),
                        Guild.NotificationLevel::getValue))
                .explicitContentFilter(mapPossibleOptional(explicitContentFilter(), Guild.ContentFilterLevel::getValue))
                .afkChannelId(mapPossibleOptional(afkChannelId(), Snowflake::asString))
                .afkTimeout(afkTimeout())
                .icon(mapPossibleOptional(icon(), Image::getDataUri))
                .ownerId(mapPossible(ownerId(), Snowflake::asString))
                .splash(mapPossibleOptional(splash(), Image::getDataUri))
                .discoverySplash(mapPossibleOptional(discoverySplash(), Image::getDataUri))
                .banner(mapPossibleOptional(banner(), Image::getDataUri))
                .systemChannelId(mapPossibleOptional(systemChannelId(), Snowflake::asString))
                .systemChannelFlags(mapPossible(systemChannelFlags(), Guild.SystemChannelFlag::getValue))
                .rulesChannelId(mapPossibleOptional(rulesChannelId(), Snowflake::asString))
                .publicUpdatesChannelId(mapPossibleOptional(publicUpdatesChannelId(), Snowflake::asString))
                .preferredLocale(mapPossibleOptional(preferredLocale(), Locale::toLanguageTag))
                .features(mapPossible(features(), ArrayList::new))
                .description(description())
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class GuildEditMonoGenerator extends Mono<Guild> implements GuildEditSpecGenerator {

    abstract Guild guild();

    @Override
    public void subscribe(CoreSubscriber<? super Guild> actual) {
        guild().edit(GuildEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
