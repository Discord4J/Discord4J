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
package discord4j.core.object.data.stored;

import discord4j.common.json.ReactionResponse;
import reactor.util.annotation.Nullable;

import java.io.Serializable;

public final class ReactionBean implements Serializable {

    private static final long serialVersionUID = 2852469870736582846L;

    private int count;
    private boolean me;
    @Nullable
    private Long emojiId;
    private String emojiName;
    private boolean emojiAnimated;

    public ReactionBean(final ReactionResponse response) {
        count = response.getCount();
        me = response.isMe();
        emojiId = response.getEmoji().getId();
        emojiName = response.getEmoji().getName();
        emojiAnimated = response.getEmoji().getAnimated() != null && response.getEmoji().getAnimated();
    }

    public ReactionBean(int count, boolean me, @Nullable Long emojiId, String emojiName, boolean emojiAnimated) {
        this.count = count;
        this.me = me;
        this.emojiId = emojiId;
        this.emojiName = emojiName;
        this.emojiAnimated = emojiAnimated;
    }

    public ReactionBean() {}

    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public boolean isMe() {
        return me;
    }

    public void setMe(final boolean me) {
        this.me = me;
    }

    @Nullable
    public Long getEmojiId() {
        return emojiId;
    }

    public void setEmojiId(@Nullable final Long emojiId) {
        this.emojiId = emojiId;
    }

    public String getEmojiName() {
        return emojiName;
    }

    public void setEmojiName(final String emojiName) {
        this.emojiName = emojiName;
    }

    public boolean isEmojiAnimated() {
        return emojiAnimated;
    }

    public void setEmojiAnimated(boolean emojiAnimated) {
        this.emojiAnimated = emojiAnimated;
    }

    @Override
    public String toString() {
        return "ReactionBean{" +
                "count=" + count +
                ", me=" + me +
                ", emojiId=" + emojiId +
                ", emojiName='" + emojiName + '\'' +
                ", emojiAnimated=" + emojiAnimated +
                '}';
    }
}
