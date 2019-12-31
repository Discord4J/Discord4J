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

package discord4j.rest.entity.data;

import discord4j.rest.json.response.VoiceRegionResponse;

public class RegionData {

    private final String id;
    private final String name;
    private final String sampleHostname;
    private final int samplePort;
    private final boolean vip;
    private final boolean optimal;
    private final boolean deprecated;
    private final boolean custom;

    public RegionData(final VoiceRegionResponse response) {
        id = response.getId();
        name = response.getName();
        sampleHostname = response.getSampleHostname();
        samplePort = response.getSamplePort();
        vip = response.isVip();
        optimal = response.isOptimal();
        deprecated = response.isDeprecated();
        custom = response.isCustom();
    }

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
        return "RegionData{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", sampleHostname='" + sampleHostname + '\'' +
                ", samplePort=" + samplePort +
                ", vip=" + vip +
                ", optimal=" + optimal +
                ", deprecated=" + deprecated +
                ", custom=" + custom +
                '}';
    }
}
