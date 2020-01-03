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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.rest.json.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleJson;

@PossibleJson
public class IntegrationModifyRequest {

    @JsonProperty("expire_behavior")
    private final Possible<Integer> expireBehavior;
    @JsonProperty("expire_grace_period")
    private final Possible<Integer> expireGracePeriod;
    @JsonProperty("enable_emoticons")
    private final Possible<Boolean> enableEmoticons;

    public IntegrationModifyRequest(Possible<Integer> expireBehavior,
                                    Possible<Integer> expireGracePeriod,
                                    Possible<Boolean> enableEmoticons) {
        this.expireBehavior = expireBehavior;
        this.expireGracePeriod = expireGracePeriod;
        this.enableEmoticons = enableEmoticons;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Possible<Integer> expireBehavior = Possible.absent();
        private Possible<Integer> expireGracePeriod = Possible.absent();
        private Possible<Boolean> enableEmoticons = Possible.absent();

        public Builder expireBehavior(int expireBehavior) {
            this.expireBehavior = Possible.of(expireBehavior);
            return this;
        }

        public Builder expireGracePeriod(int expireGracePeriod) {
            this.expireGracePeriod = Possible.of(expireGracePeriod);
            return this;
        }

        public Builder enableEmoticons(boolean enableEmoticons) {
            this.enableEmoticons = Possible.of(enableEmoticons);
            return this;
        }

        public IntegrationModifyRequest build() {
            return new IntegrationModifyRequest(expireBehavior, expireGracePeriod, enableEmoticons);
        }
    }

    @Override
    public String toString() {
        return "IntegrationModifyRequest{" +
                "expireBehavior=" + expireBehavior +
                ", expireGracePeriod=" + expireGracePeriod +
                ", enableEmoticons=" + enableEmoticons +
                '}';
    }
}
