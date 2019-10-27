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

import discord4j.common.json.EmbedFieldEntity;

public class EmbedFieldData {

    private final String name;
    private final String value;
    private final boolean inline;

    public EmbedFieldData(EmbedFieldEntity response) {
        name = response.getName();
        value = response.getValue();
        inline = response.isInline();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isInline() {
        return inline;
    }

    @Override
    public String toString() {
        return "EmbedFieldData{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", inline=" + inline +
                '}';
    }
}
