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

public class Template implements DiscordObject {

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
    public Template(final GatewayDiscordClient gateway, final TemplateData data) {
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
    public final String getCode() {
        return data.code();
    }

    /**
     * Returns the source guild ID.
     *
     * @return the guild id
     */
    public final Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Returns the name of the template.
     *
     * @return The template name
     */
    public final String getName() {
        return data.name();
    }

    /**
     * Returns the description of the template.
     *
     * @return the template description
     */
    public final Optional<String> getDescription() {
        return data.description();
    }

    /**
     * Returns the amount of times the template has been used.
     *
     * @return the template usage count
     */
    public final int getUsageCount() {
        return data.usageCount();
    }

    /**
     * Returns the id of the creator of this template.
     *
     * @return the creator id
     */
    public final Snowflake getCreatorId() {
        return Snowflake.of(data.creatorId());
    }

    /**
     * Returns the creator of this template.
     *
     * @return the creator
     */
    public final User getCreator() {
        return new User(gateway, data.creator());
    }

    /**
     * Returns an {@link Instant} when this template was created.
     *
     * @return a timestamp when template was last updated
     */
    public final Instant getCreatedAt() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(data.createdAt(), Instant::from);
    }

    /**
     * Returns an {@link Instant} when this template was last updated.
     *
     * @return a timestamp when template was last updated
     */
    public final Instant getUpdatedAt() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(data.updatedAt(), Instant::from);
    }

    /**
     * Gets the guild snapshot of the template.
     *
     * @return The guild snapshot.
     */
    public final SerializedSourceGuildData getSourceGuild() {
        return data.serializedSourceGuild();
    }

    /**
     * Creates a new guild from a template. Fires Guild Create Gateway event.
     *
     * @return the guild object
     */
    public final Mono<Guild> createGuild(final Consumer<? super TemplateCreateGuildSpec> spec) {
        return Mono.defer(
                () -> {
                    TemplateCreateGuildSpec mutatedSpec = new TemplateCreateGuildSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getTemplateService().createGuild(getCode(),
                            mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> new Guild(gateway, data));
    }

    /**
     * Requests to sync this template with the guild's current state.
     *
     * @return a {@link Mono} that, upon subscription, syncs a guild with this template. If an error is received, it
     * will be emitted through the Mono.
     */
    public final Mono<Template> sync() {
        return sync(guildId);
    }

    /**
     * Requests to sync this template with the given guild.
     *
     * @param guildId the guild to fetch current state for the template
     * @return a {@link Mono} that, upon subscription, syncs a guild with this template. If an error is received, it
     * will be emitted through the Mono.
     */
    public final Mono<Template> sync(long guildId) {
        return gateway.getRestClient().getTemplateService()
                .syncTemplate(guildId, getCode())
                .map(data -> new Template(gateway, data));
    }

    /**
     * Requests to edit this guild template.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link GuildTemplateEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Template}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public final Mono<Template> edit(final Consumer<? super GuildTemplateEditSpec> spec) {
        return Mono.defer(
                () -> {
                    GuildTemplateEditSpec mutatedSpec = new GuildTemplateEditSpec();
                    spec.accept(mutatedSpec);
                    return gateway.getRestClient().getTemplateService().modifyTemplate(guildId, getCode(),
                            mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> new Template(gateway, data));
    }

    /**
     * Requests to delete this template while optionally specifying a reason.
     *
     * @return A {@link Mono} where, upon successful completion, emits the deleted template. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public final Mono<Template> delete(Snowflake guildId) {
        return gateway.getRestClient().getTemplateService()
                .deleteTemplate(guildId.asLong(), getCode())
                .map(data -> new Template(gateway, data));
    }
}
