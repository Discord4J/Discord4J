package discord4j.core.object;

import discord4j.discordjson.json.InstallParamsData;

import java.util.List;

/**
 * Represents the configuration of an installation context
 *
 * @see <a href="https://discord.com/developers/docs/resources/application#install-params-object">https://discord.com/developers/docs/resources/application#install-params-object</a>
 */
public class ApplicationInstallParams {

    private final InstallParamsData data;

    public ApplicationInstallParams(InstallParamsData data) {
        this.data = data;
    }

    /**
     * Gets the data of this configuration
     *
     * @return The data of this configuration
     */
    public InstallParamsData getData() {
        return this.data;
    }

    /**
     * Gets the scopes of this configuration
     *
     * @return The scopes of this configuration
     */
    public List<String> getScopes() {
        return this.data.scopes();
    }

    /**
     * Gets the permissions of this configuration
     *
     * @return The permissions of this configuration
     */
    public String getPermissions() {
        return this.data.permissions();
    }

}
