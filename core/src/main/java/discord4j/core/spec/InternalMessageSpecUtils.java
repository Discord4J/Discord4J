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
package discord4j.core.spec;

import discord4j.core.object.component.TopLevelMessageComponent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.possible.Possible;
import reactor.util.annotation.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class InternalMessageSpecUtils {

    private InternalMessageSpecUtils() {
        throw new AssertionError();
    }

    public static @Nullable Set<Message.Flag> decorateFlags(final @Nullable Collection<Message.Flag> oldFlags, final Possible<Boolean> ephemeral, final Possible<Boolean> suppressEmbeds, final Possible<List<TopLevelMessageComponent>> components) {
        final Set<Message.Flag> newFlags = new HashSet<>();
        if (ephemeral.toOptional().orElse(false)) {
            newFlags.add(Message.Flag.EPHEMERAL);
        }
        if (suppressEmbeds.toOptional().orElse(false)) {
            newFlags.add(Message.Flag.SUPPRESS_EMBEDS);
        }
        if (!components.isAbsent() && components.get().stream().anyMatch(component -> component.getType().isRequiredFlag())) {
            newFlags.add(Message.Flag.IS_COMPONENTS_V2);
        }
        if (!newFlags.isEmpty()) {
            if (oldFlags != null) {
                newFlags.addAll(oldFlags);
            }
            return newFlags;
        } else if (oldFlags != null) {
            return new HashSet<>(oldFlags);
        } else {
            return null;
        }
    }
}
