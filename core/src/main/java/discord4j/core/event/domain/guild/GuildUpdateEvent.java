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
package discord4j.core.event.domain.guild;

import discord4j.core.Client;
import discord4j.core.event.Update;
import discord4j.core.object.Snowflake;
import discord4j.core.object.entity.Guild;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

public class GuildUpdateEvent extends GuildEvent {

    private final Update<Snowflake> afkChannel;
    private final Update<Integer> afkTimeout;
    private final Update<Guild.ContentFilterLevel> contentFilterLevel;
    private final Update<Set<String>> features;
    private final Update<String> iconHash;
    private final Update<Guild.MfaLevel> mfaLevel;
    private final Update<String> name;
    private final Update<Guild.NotificationLevel> notificationLevel;
    private final Update<Snowflake> owner;
    private final Update<String> region;
    private final Update<String> splash;
    private final Update<Snowflake> systemChannel;
    private final Update<Guild.VerificationLevel> verificationLevel;

    public GuildUpdateEvent(Client client, @Nullable Update<Snowflake> afkChannel, @Nullable Update<Integer> afkTimeout,
                            @Nullable Update<Guild.ContentFilterLevel> contentFilterLevel,
                            @Nullable Update<Set<String>> features, @Nullable Update<String> iconHash,
                            @Nullable Update<Guild.MfaLevel> mfaLevel, Update<String> name,
                            @Nullable Update<Guild.NotificationLevel> notificationLevel, Update<Snowflake> owner,
                            @Nullable Update<String> region, @Nullable Update<String> splash,
                            @Nullable Update<Snowflake> systemChannel,
                            @Nullable Update<Guild.VerificationLevel> verificationLevel) {
        super(client);
        this.afkChannel = afkChannel;
        this.afkTimeout = afkTimeout;
        this.contentFilterLevel = contentFilterLevel;
        this.features = features;
        this.iconHash = iconHash;
        this.mfaLevel = mfaLevel;
        this.name = name;
        this.notificationLevel = notificationLevel;
        this.owner = owner;
        this.region = region;
        this.splash = splash;
        this.systemChannel = systemChannel;
        this.verificationLevel = verificationLevel;
    }

    public Optional<Update<Snowflake>> getAfkChannel() {
        return Optional.ofNullable(afkChannel);
    }

    public Optional<Update<Integer>> getAfkTimeout() {
        return Optional.ofNullable(afkTimeout);
    }

    public Optional<Update<Guild.ContentFilterLevel>> getContentFilterLevel() {
        return Optional.ofNullable(contentFilterLevel);
    }

    public Optional<Update<Set<String>>> getFeatures() {
        return Optional.ofNullable(features);
    }

    public Optional<Update<String>> getIconHash() {
        return Optional.ofNullable(iconHash);
    }

    public Optional<Update<Guild.MfaLevel>> getMfaLevel() {
        return Optional.ofNullable(mfaLevel);
    }

    public Optional<Update<String>> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<Update<Guild.NotificationLevel>> getNotificationLevel() {
        return Optional.ofNullable(notificationLevel);
    }

    public Optional<Update<Snowflake>> getOwner() {
        return Optional.ofNullable(owner);
    }

    public Optional<Update<String>> getRegion() {
        return Optional.ofNullable(region);
    }

    public Optional<Update<String>> getSplash() {
        return Optional.ofNullable(splash);
    }

    public Optional<Update<Snowflake>> getSystemChannel() {
        return Optional.ofNullable(systemChannel);
    }

    public Optional<Update<Guild.VerificationLevel>> getVerificationLevel() {
        return Optional.ofNullable(verificationLevel);
    }
}
