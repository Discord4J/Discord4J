package discord4j.core.spec;

import discord4j.core.object.Template;
import discord4j.discordjson.json.TemplateModifyRequest;
import discord4j.discordjson.possible.Possible;
import reactor.util.annotation.Nullable;

import java.util.Optional;

public class GuildTemplateEditSpec implements AuditSpec<TemplateModifyRequest> {

    private Possible<String> name = Possible.absent();
    private Possible<Optional<String>> description = Possible.absent();
    @Nullable
    private String reason;

    /**
     * Sets the name for the modified {@link Template}.
     *
     * @param name The name for the template.
     * @return This spec.
     */
    public GuildTemplateEditSpec setName(String name) {
        this.name = Possible.of(name);
        return this;
    }

    /**
     * Sets the description for the modified {@link Template}.
     *
     * @param description The description for the template.
     * @return This spec.
     */
    public GuildTemplateEditSpec setDescription(String description) {
        this.description = Possible.of(Optional.of(description));
        return this;
    }

    @Override
    public GuildTemplateEditSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public TemplateModifyRequest asRequest() {
        return TemplateModifyRequest.builder()
            .name(name)
            .description(description)
            .build();
    }
}
