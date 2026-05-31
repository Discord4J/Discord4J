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
package discord4j.core.object.component.kind;

import discord4j.core.object.component.Component;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.component.attribute.ICanHaveCustomId;
import discord4j.discordjson.json.component.attribute.IHasCustomId;

import java.util.Optional;

/**
 * A message component who can trigger an action.
 */
public abstract class ActionComponent<D extends ComponentData> extends Component<D> {

    protected ActionComponent(D data) {
        super(data);
    }

    /**
     * Get this action component's custom id if present
     *
     * @return An {@link Optional} containing the custom id if present
     */
    public Optional<String> getCustomId() {
        D data = getData();

        if (data instanceof ICanHaveCustomId) {
            return ((ICanHaveCustomId) data).customId().toOptional();
        } else if (data instanceof IHasCustomId) {
            return Optional.ofNullable(((IHasCustomId) data).customId());
        } else {
            return Optional.empty();
        }
    }

}
