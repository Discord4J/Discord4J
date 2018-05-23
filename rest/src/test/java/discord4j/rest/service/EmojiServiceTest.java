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
package discord4j.rest.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.jackson.PossibleModule;
import discord4j.rest.RestTests;
import discord4j.rest.request.Router;
import org.junit.Test;
import reactor.core.scheduler.Schedulers;

public class EmojiServiceTest {

    private static final long guild = Long.parseUnsignedLong(System.getenv("guild"));
    private static final long permanentEmoji = Long.parseUnsignedLong(System.getenv("permanentEmoji"));

    private EmojiService emojiService = null;

    private EmojiService getEmojiService() {

        if (emojiService != null) {
            return emojiService;
        }

        String token = System.getenv("token");
        ObjectMapper mapper = getMapper();
        Router router = RestTests.getRouter(token, mapper);

        return emojiService = new EmojiService(router);
    }

    private ObjectMapper getMapper() {
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .registerModule(new PossibleModule());
    }

    @Test
    public void testGetGuildEmojis() {
        getEmojiService().getGuildEmojis(guild).then().block();
    }

    @Test
    public void testGetGuildEmoji() {
        getEmojiService().getGuildEmoji(guild, permanentEmoji).block();
    }

    @Test
    public void testCreateGuildEmoji() {
        // TODO
    }

    @Test
    public void testModifyGuildEmoji() {
        // TODO
    }

    @Test
    public void testDeleteGuildEmoji() {
        // TODO
    }
}
