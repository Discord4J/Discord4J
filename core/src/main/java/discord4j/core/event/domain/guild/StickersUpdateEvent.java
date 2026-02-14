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

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildSticker;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;
import java.util.Optional;
import java.util.Set;

/**
 * Dispatched when an sticker is added/deleted/or edited in a guild. The {@link #stickers} set includes ALL stickers of the
 * guild.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#guild-stickers-update">Guild Stickers Update</a>
 */
public class StickersUpdateEvent extends GuildEvent {

    private final long guildId;
    private final Set<GuildSticker> stickers;

    @Nullable
    private final Set<GuildSticker> old;

    public StickersUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long guildId, Set<GuildSticker> stickers, @Nullable Set<GuildSticker> old) {
        super(gateway, shardInfo);
        this.guildId = guildId;
        this.stickers = stickers;
        this.old = old;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} involved in the event.
     *
     * @return The ID of the {@link Guild}.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the {@link Guild} whose stickers have been updated.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets a list of ALL stickers of the {@link Guild}.
     *
     * @return A list of ALL stickers of the {@link Guild}.
     */
    public Set<GuildSticker> getStickers() {
        return stickers;
    }

    /**
     * Gets a list of ALL old stickers of the {@link Guild}, if present.
     * This may not be available if {@code GuildSticker} are not stored.
     *
     * @return A list of ALL old stickers of the {@link Guild}.
     */
    public Optional<Set<GuildSticker>> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public String toString() {
        return "StickersUpdateEvent{" +
                "guildId=" + guildId +
                ", stickers=" + stickers +
                ", old=" + old +
                '}';
    }
}
