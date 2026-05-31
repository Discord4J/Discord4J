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
package discord4j.core.object.component.impl.selectmenu;

import discord4j.core.object.component.attribute.ICanBeDisabledComponent;
import discord4j.core.object.component.attribute.ICanBeRequiredComponent;
import discord4j.core.object.component.attribute.ICanHavePlaceholderComponent;
import discord4j.core.object.component.attribute.ICanHaveRangedValueCountComponent;
import discord4j.core.object.component.attribute.IHaveValuesComponent;
import discord4j.core.object.component.usage.ICanBeUsedInActionRowComponent;
import discord4j.core.object.component.usage.ICanBeUsedInLabelComponent;
import discord4j.core.object.component.kind.ActionComponent;
import discord4j.discordjson.json.component.type.SelectComponentDataBase;

/**
 * The base class for all select menu components.
 *
 * @param <D> The type of the select menu data
 * @param <T> The type of the select menu component
 * @see discord4j.core.object.component.impl.selectmenu.StringSelectMenu
 * @see discord4j.core.object.component.impl.selectmenu.UserSelectMenu
 * @see discord4j.core.object.component.impl.selectmenu.RoleSelectMenu
 * @see discord4j.core.object.component.impl.selectmenu.ChannelSelectMenu
 * @see discord4j.core.object.component.impl.selectmenu.MentionableSelectMenu
 */
public abstract class BaseSelectMenu<D extends SelectComponentDataBase, T extends BaseSelectMenu<D, T>>
        extends ActionComponent<D>
        implements ICanBeUsedInLabelComponent, ICanBeUsedInActionRowComponent, ICanHavePlaceholderComponent<D, T>,
        ICanHaveRangedValueCountComponent<D, T>, ICanBeDisabledComponent<D, T>,
        ICanBeRequiredComponent<D, T>, IHaveValuesComponent {

    protected BaseSelectMenu(D data) {
        super(data);
    }

}
