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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.oauth2.spec;

import discord4j.core.spec.Spec;
import discord4j.oauth2.Scope;
import discord4j.oauth2.request.ClientCredentialsGrantRequest;

/**
 * Spec used to exchange Discord application credentials for the access token of the application owner.
 *
 * @see <a href="https://discord.com/developers/docs/topics/oauth2#client-credentials-grant">Client Credentials Grant</a>
 */
public class ClientCredentialsGrantSpec implements Spec<ClientCredentialsGrantRequest> {

    private long clientId;
    private String clientSecret;
    private final StringBuilder scope = new StringBuilder();

    /**
     * Sets the client ID of the Discord application.
     *
     * @param clientId The client ID.
     * @return This spec.
     */
    public ClientCredentialsGrantSpec setClientId(long clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Sets the client secret of the Discord application.
     *
     * @param clientSecret The client secret.
     * @return This spec.
     */
    public ClientCredentialsGrantSpec setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Adds the given scope to the list of scopes that the exchanged access token will grant.
     *
     * @param scope The scope to grant.
     * @return This spec.
     */
    public ClientCredentialsGrantSpec addScope(Scope scope) {
        this.scope.append(' ').append(scope.getValue());
        return this;
    }

    /**
     * Sets the list of scopes that the exchanged access token will grant.
     *
     * @param scope The space-delimited string of the scopes to grant.
     * @return This spec.
     * @see Scope
     */
    public ClientCredentialsGrantSpec setScope(String scope) {
        this.scope.setLength(0);
        this.scope.append(' ').append(scope);
        return this;
    }

    @Override
    public ClientCredentialsGrantRequest asRequest() {
        return ClientCredentialsGrantRequest.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope(scope.length() == 0 ? null : scope.substring(1))
                .build();
    }
}
