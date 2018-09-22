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
package discord4j.core.object.data;

import discord4j.rest.json.response.VoiceRegionResponse;

import java.io.Serializable;

public final class RegionBean implements Serializable {

    private static final long serialVersionUID = 6190213184200305407L;

    private String id;
    private String name;
    private String sampleHostname;
    private int samplePort;
    private boolean vip;
    private boolean optimal;
    private boolean deprecated;
    private boolean custom;

    public RegionBean(final VoiceRegionResponse response) {
        id = response.getId();
        name = response.getName();
        sampleHostname = response.getSampleHostname();
        samplePort = response.getSamplePort();
        vip = response.isVip();
        optimal = response.isOptimal();
        deprecated = response.isDeprecated();
        custom = response.isCustom();
    }

    public RegionBean() {}

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSampleHostname() {
        return sampleHostname;
    }

    public void setSampleHostname(final String sampleHostname) {
        this.sampleHostname = sampleHostname;
    }

    public int getSamplePort() {
        return samplePort;
    }

    public void setSamplePort(final int samplePort) {
        this.samplePort = samplePort;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(final boolean vip) {
        this.vip = vip;
    }

    public boolean isOptimal() {
        return optimal;
    }

    public void setOptimal(final boolean optimal) {
        this.optimal = optimal;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(final boolean deprecated) {
        this.deprecated = deprecated;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(final boolean custom) {
        this.custom = custom;
    }

    @Override
    public String toString() {
        return "RegionBean{" +
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
