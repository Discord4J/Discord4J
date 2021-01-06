package discord4j.core.object;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.TemplateData;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class Template implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final TemplateData data;

    /** The ID of the guild this role is associated to. */
    private final long guildId;

    /**
     * Constructs a {@code Template} with an associated ServiceMediator and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Template(final GatewayDiscordClient gateway, final TemplateData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
        this.guildId = Long.parseLong(data.sourceGuildId());
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the template code (unique ID).
     *
     * @return The template code (unique ID).
     */
    public final String getCode() {
        return data.code();
    }

    /**
     * Gets the source guild ID.
     *
     * @return The guild id.
     */
    public final long getGuildId() {
        return guildId;
    }

    /**
     * Requests to delete this template while optionally specifying a reason.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the template has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Void> delete(Snowflake guildId) {
        return gateway.getRestClient().getTemplateService()
            .deleteTemplate(guildId.asLong(), getCode())
            .then();
    }

}
