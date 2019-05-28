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
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleLong;
import discord4j.common.jackson.UnsignedJson;
import reactor.util.annotation.Nullable;

public class ActivityResponse {

    private String name;
    private int type;
    @Nullable
    private Possible<String> url;
    private Possible<Timestamps> timestamps;
    @JsonProperty("application_id")
    @UnsignedJson
    private PossibleLong applicationId;
    @Nullable
    private Possible<String> details;
    @Nullable
    private Possible<String> state;
    private Possible<Party> party;
    private Possible<Assets> assets;
    private Possible<Secrets> secrets;
    private Possible<Boolean> instance;
    private Possible<Integer> flags;

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    @Nullable
    public Possible<String> getUrl() {
        return url;
    }

    public Possible<Timestamps> getTimestamps() {
        return timestamps;
    }

    public PossibleLong getApplicationId() {
        return applicationId;
    }

    @Nullable
    public Possible<String> getDetails() {
        return details;
    }

    @Nullable
    public Possible<String> getState() {
        return state;
    }

    public Possible<Party> getParty() {
        return party;
    }

    public Possible<Assets> getAssets() {
        return assets;
    }

    public Possible<Secrets> getSecrets() {
        return secrets;
    }

    public Possible<Boolean> getInstance() {
        return instance;
    }

    public Possible<Integer> getFlags() {
        return flags;
    }

    @Override
    public String toString() {
        return "ActivityResponse{" +
            "name='" + name + '\'' +
            ", type=" + type +
            ", url=" + url +
            ", timestamps=" + timestamps +
            ", applicationId=" + applicationId +
            ", details=" + details +
            ", state=" + state +
            ", party=" + party +
            ", assets=" + assets +
            ", secrets=" + secrets +
            ", instance=" + instance +
            ", flags=" + flags +
            '}';
    }

    public static class Timestamps {
        private Possible<Integer> start;
        private Possible<Integer> end;

        public Possible<Integer> getStart() {
            return start;
        }

        public Possible<Integer> getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return "Timestamps{" +
                "start=" + start +
                ", end=" + end +
                '}';
        }
    }

    public static class Party {
        private Possible<String> id;
        private Possible<int[]> size;

        public Possible<String> getId() {
            return id;
        }

        public Possible<int[]> getSize() {
            return size;
        }

        @Override
        public String toString() {
            return "Party{" +
                "id=" + id +
                ", size=" + size +
                '}';
        }
    }

    public static class Assets {
        @JsonProperty("large_image")
        private Possible<String> largeImage;
        @JsonProperty("large_text")
        private Possible<String> largeText;
        @JsonProperty("small_image")
        private Possible<String> smallImage;
        @JsonProperty("small_text")
        private Possible<String> smallText;

        public Possible<String> getLargeImage() {
            return largeImage;
        }

        public Possible<String> getLargeText() {
            return largeText;
        }

        public Possible<String> getSmallImage() {
            return smallImage;
        }

        public Possible<String> getSmallText() {
            return smallText;
        }

        @Override
        public String toString() {
            return "Assets{" +
                "largeImage=" + largeImage +
                ", largeText=" + largeText +
                ", smallImage=" + smallImage +
                ", smallText=" + smallText +
                '}';
        }
    }

    public static class Secrets {
        private Possible<String> join;
        private Possible<String> spectate;
        private Possible<String> match;

        public Possible<String> getJoin() {
            return join;
        }

        public Possible<String> getSpectate() {
            return spectate;
        }

        public Possible<String> getMatch() {
            return match;
        }

        @Override
        public String toString() {
            return "Secrets{" +
                "join=" + join +
                ", spectate=" + spectate +
                ", match=" + match +
                '}';
        }
    }
}
