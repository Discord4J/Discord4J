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
package discord4j.common.json.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VoiceRegionResponse {

    private String id;
    private String name;
    @JsonProperty("sample_hostname")
    private String sampleHostname;
    @JsonProperty("sample_port")
    private int samplePort;
    private boolean vip;
    private boolean optimal;
    private boolean deprecated;
    private boolean custom;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSampleHostname() {
        return sampleHostname;
    }

    public int getSamplePort() {
        return samplePort;
    }

    public boolean isVip() {
        return vip;
    }

    public boolean isOptimal() {
        return optimal;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public boolean isCustom() {
        return custom;
    }

    @Override
    public String toString() {
        return "VoiceRegionResponse[" +
                "id=" + id +
                ", name=" + name +
                ", sampleHostname=" + sampleHostname +
                ", samplePort=" + samplePort +
                ", vip=" + vip +
                ", optimal=" + optimal +
                ", deprecated=" + deprecated +
                ", custom=" + custom +
                ']';
    }
}
