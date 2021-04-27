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
import discord4j.oauth2.request.AuthorizationCodeGrantRequest;

/**
 * Spec used to exchange an authorization code for an access token.
 *
 * @see <a href="https://discord.com/developers/docs/topics/oauth2#authorization-code-grant">Authorization Code Grant</a>
 */
public class AuthorizationCodeGrantSpec implements Spec<AuthorizationCodeGrantRequest> {

    private long clientId;
    private String clientSecret;
    private String code;
    private String redirectUri;

    /**
     * Sets the client ID of the Discord application that the authorization code was retrieved from.
     *
     * @param clientId The client ID.
     * @return This spec.
     */
    public AuthorizationCodeGrantSpec setClientId(long clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Sets the client secret of the Discord application that the authorization code was retrieved from.
     *
     * @param clientSecret The client secret.
     * @return This spec.
     */
    public AuthorizationCodeGrantSpec setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Sets the authorization code that will be exchanged for an access token.
     *
     * @param code The code to exchange.
     * @return This spec.
     */
    public AuthorizationCodeGrantSpec setCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * Sets the redirect URI that the authorization code was retrieved from.
     *
     * @param redirectUri The redirect URI.
     * @return This spec.
     */
    public AuthorizationCodeGrantSpec setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    @Override
    public AuthorizationCodeGrantRequest asRequest() {
        return AuthorizationCodeGrantRequest.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .code(code)
                .redirectUri(redirectUri)
                .build();
    }
}
