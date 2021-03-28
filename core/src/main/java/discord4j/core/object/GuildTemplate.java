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
package discord4j.core.object;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.spec.GuildTemplateEditSpec;
import discord4j.core.spec.TemplateCreateGuildSpec;
import discord4j.discordjson.json.SerializedSourceGuildData;
import discord4j.discordjson.json.TemplateData;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A Discord Guild Template.
 *
 * @see <a href="https://discord.com/developers/docs/resources/template">Template Resource</a>
 */
public final class GuildTemplate implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final TemplateData data;

    /** The ID of the guild this template is associated to. */
    private final long guildId;

    /**
     * Constructs a {@code Template} with an associated Discord client and data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public GuildTemplate(final GatewayDiscordClient gateway, final TemplateData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
        this.guildId = Snowflake.asLong(data.sourceGuildId());
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    /**
     * Returns this object underlying raw data structure.
     *
     * @return an immutable data representation of this object
     */
    public TemplateData getData() {
        return data;
    }

    /**
     * Returns the template code (unique ID).
     *
     * @return the template code (unique ID)
     */
    public String getCode() {
        return data.code();
    }

    /**
     * Returns the source guild ID.
     *
     * @return the guild id
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Returns the name of the template.
     *
     * @return The template name
     */
    public String getName() {
        return data.name();
    }

    /**
     * Returns the description of the template.
     *
     * @return the template description
     */
    public Optional<String> getDescription() {
        return data.description();
    }

    /**
     * Returns the amount of times the template has been used.
     *
     * @return the template usage count
     */
    public int getUsageCount() {
        return data.usageCount();
    }

    /**
     * Returns the id of the creator of this template.
     *
     * @return the creator id
     */
    public Snowflake getCreatorId() {
        return Snowflake.of(data.creatorId());
    }

    /**
     * Returns the creator of this template.
     *
     * @return the creator
     */
    public User getCreator() {
        return new User(gateway, data.creator());
    }

    /**
     * Returns an {@link Instant} when this template was created.
     *
     * @return a timestamp when template was last updated
     */
    public Instant getCreatedAt() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(data.createdAt(), Instant::from);
    }

    /**
     * Returns an {@link Instant} when this template was last updated.
     *
     * @return a timestamp when template was last updated
     */
    public Instant getUpdatedAt() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(data.updatedAt(), Instant::from);
    }

    /**
     * Gets the guild snapshot of the template.
     *
     * @return The guild snapshot.
     */
    public SerializedSourceGuildData getSourceGuild() {
        return data.serializedSourceGuild();
    }

    /**
     * Creates a new guild from a template. Fires Guild Create Gateway event.
     *
     * @return the guild object
     */
    public Mono<Guild> createGuild(final Consumer<? super TemplateCreateGuildSpec> spec) {
        return Mono.defer(
                () -> {
                    TemplateCreateGuildSpec mutatedSpec = new TemplateCreateGuildSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getTemplateService()
                            .createGuild(getCode(), mutatedSpec.asRequest());
                })
                .map(data -> new Guild(gateway, data));
    }

    /**
     * Requests to sync this template with the guild's current state.
     *
     * @return a {@link Mono} that, upon subscription, syncs a guild with this template. If an error is received, it
     * will be emitted through the Mono.
     */
    public Mono<GuildTemplate> sync() {
        return gateway.getRestClient().getTemplateService()
                .syncTemplate(guildId, getCode())
                .map(data -> new GuildTemplate(gateway, data));
    }

    /**
     * Requests to edit this guild template.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link GuildTemplateEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link GuildTemplate}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildTemplate> edit(final Consumer<? super GuildTemplateEditSpec> spec) {
        return Mono.defer(
                () -> {
                    GuildTemplateEditSpec mutatedSpec = new GuildTemplateEditSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getTemplateService()
                            .modifyTemplate(guildId, getCode(), mutatedSpec.asRequest());
                })
                .map(data -> new GuildTemplate(gateway, data));
    }

    /**
     * Requests to delete this template.
     *
     * @return A {@link Mono} where, upon successful completion, emits the deleted template. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<GuildTemplate> delete() {
        return gateway.getRestClient().getTemplateService()
                .deleteTemplate(guildId, getCode())
                .map(data -> new GuildTemplate(gateway, data));
    }
}
