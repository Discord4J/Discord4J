package discord4j.rest.entity;

import discord4j.discordjson.json.TemplateData;
import discord4j.rest.RestClient;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

/**
 * Represents a code that when used, creates a guild based on a snapshot of an existing one.
 */
public class RestTemplate {

    private final RestClient restClient;
    private final String code;

    private RestTemplate(RestClient restClient, String code) {
        this.restClient = restClient;
        this.code = code;
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

    public Mono<TemplateData> getData() {
        return restClient.getTemplateService().getTemplate(code);
    }

    public Mono<TemplateData> delete(@Nullable String reason) {
        return restClient.getTemplateService().deleteTemplate(code, reason);
    }

}
