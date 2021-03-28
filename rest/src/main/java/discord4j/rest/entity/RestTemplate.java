package discord4j.rest.entity;

import discord4j.discordjson.json.*;
import discord4j.rest.RestClient;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

/**
 * Represents a guild template entity in Discord.
 */
public class RestTemplate {

    private final RestClient restClient;
    private final String code;

    private RestTemplate(RestClient restClient, String code) {
        this.restClient = restClient;
        this.code = code;
    }

    /**
     * Returns a {@link TemplateData} object for the given code.
     *
     * @return a template object
     */
    public Mono<TemplateData> getData() {
        return restClient.getTemplateService().getTemplate(code);
    }

    /**
     * Create a {@link RestTemplate} with the given parameters. This method does not perform any API request.
     *
     * @param restClient REST API resources
     * @param code the ID of this entity
     * @return a {@code RestTemplate} represented by the given parameters.
     */
    public static RestTemplate create(RestClient restClient, String code) {
        return new RestTemplate(restClient, code);
    }

    /**
     * Create a new guild based on a template. Returns a {@link GuildData} on success. Fires a Guild Create Gateway event.
     *
     * This endpoint can be used only by bots in less than 10 guilds.
     *
     * @return a guild object
     */
    public Mono<GuildData> createGuild(String templateCode, TemplateCreateGuildRequest request, @Nullable String reason) {
        return restClient.getTemplateService().createGuild(templateCode, request, reason);
    }

    /**
     * Return a {@link Flux} of guild templates.
     *
     * @return a sequence of this guild templates
     */
    public Flux<TemplateData> getTemplates(long guildId) {
        return restClient.getTemplateService().getTemplates(guildId);
    }

    /**
     * Creates a template for the guild. Requires the {@link Permission#MANAGE_GUILD} permission. Returns the created {@link TemplateData} object on success.
     *
     * @return a template object
     */
    public Mono<TemplateData> createTemplate(long guildId, TemplateCreateRequest request, @Nullable String reason) {
        return restClient.getTemplateService().createTemplate(guildId, request, reason);
    }

    /**
     * Syncs the template to the guild's current state. Requires the {@link Permission#MANAGE_GUILD} permission. Returns the {@link TemplateData} object on success.
     *
     * @return a template object
     */
    public Mono<TemplateData> syncTemplate(long guildId) {
        return restClient.getTemplateService().syncTemplate(guildId, code);
    }

    /**
     * Modifies the template's metadata. Requires the {@link Permission#MANAGE_GUILD} permission. Returns the {@link TemplateData} object on success.
     *
     * @return a template object
     */
    public Mono<TemplateData> modifyTemplate(long guildId, TemplateModifyRequest request, @Nullable String reason) {
        return restClient.getTemplateService().modifyTemplate(guildId, code, request, reason);
    }

    /**
     *  Deletes the template. Requires the {@link Permission#MANAGE_GUILD} permission. Returns the deleted {@link TemplateData} object on success.
     *
     * @return a template object
     */
    public Mono<TemplateData> deleteTemplate(long guildId) {
        return restClient.getTemplateService().deleteTemplate(guildId, code);
    }

}
