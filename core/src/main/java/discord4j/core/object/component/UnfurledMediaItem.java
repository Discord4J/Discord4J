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
package discord4j.core.object.component;

import discord4j.discordjson.json.UnfurledMediaItemData;
import discord4j.discordjson.possible.Possible;

/**
 * Represents a unfurled media item.
 */
public class UnfurledMediaItem {

    /**
     * Creates a {@code UnfurledMediaItem} with a given url.
     *
     * @param url The url to use
     * @return An {@code UnfurledMediaItem}
     */
    public static UnfurledMediaItem of(String url) {
        return new UnfurledMediaItem(UnfurledMediaItemData.builder().url(url).build());
    }

    private final UnfurledMediaItemData data;

    public UnfurledMediaItem(UnfurledMediaItemData data) {
        this.data = data;
    }

    public UnfurledMediaItemData getData() {
        return this.data;
    }

    /**
     * Gets the URL for this item.
     *
     * @return The url
     */
    public String getURL() {
        return this.getData().url();
    }

    public int getWidth() {
        return Possible.flatOpt(this.getData().width()).orElse(0);
    }

    public int getHeight() {
        return Possible.flatOpt(this.getData().height()).orElse(0);
    }

    public String getContentType() {
        return Possible.flatOpt(this.getData().contentType()).orElse("");
    }

}
