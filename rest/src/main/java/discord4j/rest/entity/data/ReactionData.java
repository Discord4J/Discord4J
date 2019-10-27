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

import discord4j.common.json.ReactionResponse;
import reactor.util.annotation.Nullable;

public class ReactionData {

    private final int count;
    private final boolean me;
    @Nullable
    private final Long emojiId;
    private final String emojiName;
    private final boolean emojiAnimated;

    public ReactionData(ReactionResponse response) {
        count = response.getCount();
        me = response.isMe();
        emojiId = response.getEmoji().getId();
        emojiName = response.getEmoji().getName();
        emojiAnimated = response.getEmoji().getAnimated() != null && response.getEmoji().getAnimated();
    }

    public int getCount() {
        return count;
    }

    public boolean isMe() {
        return me;
    }

    @Nullable
    public Long getEmojiId() {
        return emojiId;
    }

    public String getEmojiName() {
        return emojiName;
    }

    public boolean isEmojiAnimated() {
        return emojiAnimated;
    }

    @Override
    public String toString() {
        return "ReactionData{" +
                "count=" + count +
                ", me=" + me +
                ", emojiId=" + emojiId +
                ", emojiName='" + emojiName + '\'' +
                ", emojiAnimated=" + emojiAnimated +
                '}';
    }
}
