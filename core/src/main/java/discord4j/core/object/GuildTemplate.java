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
import discord4j.core.spec.GuildCreateFromTemplateMono;
import discord4j.core.spec.GuildCreateFromTemplateSpec;
import discord4j.core.spec.GuildTemplateEditSpec;
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
     * Constructs a {@code GuildTemplate} with an associated {@link GatewayDiscordClient} and Discord data.
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
     * Gets the data of the template.
     *
     * @return The data of the template.
     */
    public TemplateData getData() {
        return data;
    }

    /**
     * Gets the template code (unique ID).
     *
     * @return The template code (unique ID).
     */
    public String getCode() {
        return data.code();
    }

    /**
     * Gets the ID of the guild this template is associated with.
     *
     * @return The source guild ID.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Gets the name of the template.
     *
     * @return The template name.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the description of the template, if present.
     *
     * @return The template description.
     */
    public Optional<String> getDescription() {
        return data.description();
    }

    /**
     * Gets the number of times the template has been used.
     *
     * @return The number of times the template has been used.
     */
    public int getUsageCount() {
        return data.usageCount();
    }

    /**
     * Gets the ID of the user who created the template.
     *
     * @return The ID of the user who created the template.
     */
    public Snowflake getCreatorId() {
        return Snowflake.of(data.creatorId());
    }

    /**
     * Gets the user who created the template.
     *
     * @return The user who created the template.
     */
    public User getCreator() {
        return new User(gateway, data.creator());
    }

    /**
     * Gets when the template was created.
     *
     * @return When the template was created.
     */
    public Instant getCreatedAt() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(data.createdAt(), Instant::from);
    }

    /**
     * Gets when the template was last updated.
     *
     * @return When the template was last updated.
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
     * Requests to create a new guild from this template. Properties specifying how to create a new guild from this
     * template can be set via the {@code withXxx} methods of the returned {@link GuildCreateFromTemplateMono}.
     *
     * @param name the name of the guild to create
     * @return A {@link GuildCreateFromTemplateMono} where, upon successful completion, emits the {@link Guild created
     * guild}. If an error is received, it is emitted through the {@code GuildCreateFromTemplateMono}.
     */
    public GuildCreateFromTemplateMono createGuild(String name) {
        return GuildCreateFromTemplateMono.of(name, this);
    }

    /**
     * Requests to create a new guild from this template.
     *
     * @param spec an immutable object that specifies how to create a new guild from this template
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild created guild}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> createGuild(GuildCreateFromTemplateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(() -> gateway.getRestClient().getTemplateService().createGuild(getCode(), spec.asRequest()))
                .map(data -> new Guild(gateway, data));
    }

    /**
     * Requests to sync this template with the guild's current state.
     *
     * @return a {@link Mono} that, upon subscription, syncs a template with its guild. If an error is received, it
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
