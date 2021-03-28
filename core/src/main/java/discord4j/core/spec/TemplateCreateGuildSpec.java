package discord4j.core.spec;

import discord4j.core.object.Template;
import discord4j.discordjson.json.TemplateCreateGuildRequest;
import discord4j.discordjson.possible.Possible;
import reactor.util.annotation.Nullable;

public class TemplateCreateGuildSpec implements AuditSpec<TemplateCreateGuildRequest> {

    private String name = null;
    private Possible<String> icon = Possible.absent();
    @Nullable
    private String reason;

    /**
     * Sets the name for the modified {@link Template}.
     *
     * @param name The name for the template.
     * @return This spec.
     */
    public TemplateCreateGuildSpec setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the icon for the modified {@link Template}.
     *
     * @param icon The icon for the template.
     * @return This spec.
     */
    public TemplateCreateGuildSpec setIcon(String icon) {
        this.icon = Possible.of(icon);
        return this;
    }

    @Override
    public TemplateCreateGuildSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public TemplateCreateGuildRequest asRequest() {
        return TemplateCreateGuildRequest.builder()
            .name(name)
            .icon(icon)
            .build();
    }
}
