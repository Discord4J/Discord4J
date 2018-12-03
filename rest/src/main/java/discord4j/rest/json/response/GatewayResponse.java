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
package discord4j.rest.json.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

public class GatewayResponse {

    private String url;
    @Nullable
    private Integer shards;
    @JsonProperty("session_start_limit")
    @Nullable
    private SessionStartLimit sessionStartLimit;

    public String getUrl() {
        return url;
    }

    @Nullable
    public Integer getShards() {
        return shards;
    }

    @Nullable
    public SessionStartLimit getSessionStartLimit() {
        return sessionStartLimit;
    }

    @Override
    public String toString() {
        return "GatewayResponse{" +
                "url='" + url + '\'' +
                ", shards=" + shards +
                ", sessionStartLimit=" + sessionStartLimit +
                '}';
    }

    public static class SessionStartLimit {

        private int total;
        private int remaining;
        @JsonProperty("reset_after")
        private long resetAfter;

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getRemaining() {
            return remaining;
        }

        public void setRemaining(int remaining) {
            this.remaining = remaining;
        }

        public long getResetAfter() {
            return resetAfter;
        }

        public void setResetAfter(long resetAfter) {
            this.resetAfter = resetAfter;
        }

        @Override
        public String toString() {
            return "SessionStartLimit{" +
                    "total=" + total +
                    ", remaining=" + remaining +
                    ", resetAfter=" + resetAfter +
                    '}';
        }
    }
}
