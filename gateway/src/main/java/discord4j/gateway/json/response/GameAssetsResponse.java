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
package discord4j.gateway.json.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.util.annotation.Nullable;

public class GameAssetsResponse {

    @JsonProperty("large_image")
    @Nullable
    private String largeImage;
    @JsonProperty("large_text")
    @Nullable
    private String largeText;
    @JsonProperty("small_image")
    @Nullable
    private String smallImage;
    @JsonProperty("small_text")
    @Nullable
    private String smallText;

    @Nullable
    public String getLargeImage() {
        return largeImage;
    }

    @Nullable
    public String getLargeText() {
        return largeText;
    }

    @Nullable
    public String getSmallImage() {
        return smallImage;
    }

    @Nullable
    public String getSmallText() {
        return smallText;
    }

    @Override
    public String toString() {
        return "GameAssetsResponse{" +
                "largeImage='" + largeImage + '\'' +
                ", largeText='" + largeText + '\'' +
                ", smallImage='" + smallImage + '\'' +
                ", smallText='" + smallText + '\'' +
                '}';
    }
}
