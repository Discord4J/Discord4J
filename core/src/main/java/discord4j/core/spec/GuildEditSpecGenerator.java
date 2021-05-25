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
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import static discord4j.core.spec.InternalSpecUtils.*;

@SpecStyle
@Value.Immutable(singleton = true)
interface GuildEditSpecGenerator extends AuditSpec<GuildModifyRequest> {
    
    @Nullable
    String name();
    
    @Nullable
    default Possible<Region> region() {
        return Possible.absent();
    }
    
    @Nullable
    default Possible<Guild.VerificationLevel> verificationLevel() {
        return Possible.absent();
    }
    
    @Nullable
    default Possible<Guild.NotificationLevel> defaultMessageNotificationsLevel() {
        return Possible.absent();
    }

    @Nullable
    default Possible<Guild.ContentFilterLevel> explicitContentFilter() {
        return Possible.absent();
    }
    
    @Nullable
    default Possible<Snowflake> afkChannelId() {
        return Possible.absent();
    }
    
    @Nullable
    Integer afkTimeout();
    
    @Nullable
    default Possible<Image> icon() {
        return Possible.absent();
    }
    
    @Nullable
    Snowflake ownerId();
    
    @Nullable
    default Possible<Image> splash() {
        return Possible.absent();
    }
    
    @Nullable
    default Possible<Image> discoverySplash() {
        return Possible.absent();
    }
    
    @Nullable
    default Possible<Image> banner() {
        return Possible.absent();
    }
    
    @Nullable
    default Possible<Snowflake> systemChannelId() {
        return Possible.absent();
    }
    
    @Nullable
    Guild.SystemChannelFlag systemChannelFlags();
    
    @Nullable
    default Possible<Snowflake> rulesChannelId() {
        return Possible.absent();
    }
    
    @Nullable
    default Possible<Snowflake> publicUpdatesChannelId() {
        return Possible.absent();
    }
    
    @Nullable
    default Possible<Locale> preferredLocale() {
        return Possible.absent();
    }
    
    @Nullable
    Set<String> features();
    
    @Nullable
    default Possible<String> description() {
        return Possible.absent();
    }

    @Override
    default GuildModifyRequest asRequest() {
        return GuildModifyRequest.builder()
                .name(toPossible(name()))
                .region(toPossibleOptional(region(), Region::getId))
                .verificationLevel(toPossibleOptional(verificationLevel(), Guild.VerificationLevel::getValue))
                .defaultMessageNotifications(toPossibleOptional(defaultMessageNotificationsLevel(),
                        Guild.NotificationLevel::getValue))
                .explicitContentFilter(toPossibleOptional(explicitContentFilter(), Guild.ContentFilterLevel::getValue))
                .afkChannelId(toPossibleOptional(afkChannelId(), Snowflake::asString))
                .afkTimeout(toPossible(afkTimeout()))
                .icon(toPossibleOptional(icon(), Image::getDataUri))
                .ownerId(toPossible(mapNullable(ownerId(), Snowflake::asString)))
                .splash(toPossibleOptional(splash(), Image::getDataUri))
                .discoverySplash(toPossibleOptional(discoverySplash(), Image::getDataUri))
                .banner(toPossibleOptional(banner(), Image::getDataUri))
                .systemChannelId(toPossibleOptional(systemChannelId(), Snowflake::asString))
                .systemChannelFlags(toPossible(mapNullable(systemChannelFlags(), Guild.SystemChannelFlag::getValue)))
                .rulesChannelId(toPossibleOptional(rulesChannelId(), Snowflake::asString))
                .publicUpdatesChannelId(toPossibleOptional(publicUpdatesChannelId(), Snowflake::asString))
                .preferredLocale(toPossibleOptional(preferredLocale(), Locale::toLanguageTag))
                .features(toPossible(mapNullable(features(), ArrayList::new)))
                .description(toPossibleOptional(description()))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@SpecStyle
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