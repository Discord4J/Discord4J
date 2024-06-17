package discord4j.core.object;

import discord4j.discordjson.json.ApplicationIntegrationTypeConfigurationData;
import discord4j.discordjson.json.InstallParamsData;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents the configuration of an integration type
 *
 * @see <a href="https://discord.com/developers/docs/resources/application#application-object-application-integration-type-configuration-object">https://discord.com/developers/docs/resources/application#application-object-application-integration-type-configuration-object</a>
 */
public class ApplicationIntegrationTypeConfiguration {

    private final ApplicationIntegrationTypeConfigurationData data;

    public ApplicationIntegrationTypeConfiguration(ApplicationIntegrationTypeConfigurationData data) {
        this.data = data;
    }

    /**
     * Gets the data of this configuration
     *
     * @return The data of this configuration
     */
    public ApplicationIntegrationTypeConfigurationData getData() {
        return this.data;
    }

    /**
     * Gets the scopes of this configuration
     *
     * @return The scopes of this configuration
     */
    public List<String> scopes() {
        return this.data.oauth2InstallParams()
            .toOptional()
            .map(InstallParamsData::scopes)
            .orElse(Collections.emptyList());
    }

    /**
     * Gets the permissions of this configuration
     *
     * @return The permissions of this configuration
     */
    public Optional<String> permissions() {
        return this.data.oauth2InstallParams()
            .toOptional()
            .map(InstallParamsData::permissions);
    }

}
