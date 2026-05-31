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
package discord4j.core.object.component.attribute;

import discord4j.core.object.component.Component;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.component.attribute.ICanHaveRangedValueCount;

public interface ICanHaveRangedValueCountComponent<D extends ComponentData, T extends Component<D>> {

    ICanHaveRangedValueCount getData();

    /**
     * Gets the minimum number of options that must be chosen.
     *
     * @return The minimum number of options that must be chosen.
     */
    default int getMinValues() {
        return getData().minValues().toOptional().orElse(1);
    }

    /**
     * Gets the maximum number of options that must be chosen.
     *
     * @return The maximum number of options that must be chosen.
     */
    default int getMaxValues() {
        return getData().maxValues().toOptional().orElse(1);
    }

    T withMinValues(int minValues);

    T withMaxValues(int maxValues);

}
