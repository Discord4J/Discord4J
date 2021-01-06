package discord4j.core.spec;

import discord4j.core.object.Template;
import discord4j.discordjson.json.TemplateCreateRequest;
import discord4j.discordjson.possible.Possible;
import reactor.util.annotation.Nullable;

import java.util.Optional;

public class GuildTemplateCreateSpec implements AuditSpec<TemplateCreateRequest> {

    private String name = null;
    private Possible<Optional<String>> description = Possible.absent();
    @Nullable
    private String reason;

    /**
     * Sets the name for the modified {@link Template}.
     *
     * @param name The name for the template.
     * @return This spec.
     */
    public GuildTemplateCreateSpec setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the description for the modified {@link Template}.
     *
     * @param description The description for the template.
     * @return This spec.
     */
    public GuildTemplateCreateSpec setDescription(String description) {
        this.description = Possible.of(Optional.of(description));
        return this;
    }

    @Override
    public GuildTemplateCreateSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public TemplateCreateRequest asRequest() {
        return TemplateCreateRequest.builder()
            .name(name)
            .description(description)
            .build();
    }
}
