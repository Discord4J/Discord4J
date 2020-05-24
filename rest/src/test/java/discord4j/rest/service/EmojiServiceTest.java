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

import discord4j.rest.DiscordTest;
import discord4j.common.util.Snowflake;
import discord4j.rest.RestTests;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmojiServiceTest {

    private static final long guild = Snowflake.asLong(System.getenv("guild"));
    private static final long permanentEmoji = Snowflake.asLong(System.getenv("permanentEmoji"));

    private EmojiService emojiService;

    @BeforeAll
    public void setup() {
        emojiService = new EmojiService(RestTests.defaultRouter());
    }

    @DiscordTest
    public void testGetGuildEmojis() {
        emojiService.getGuildEmojis(guild).then().block();
    }

    @DiscordTest
    public void testGetGuildEmoji() {
        emojiService.getGuildEmoji(guild, permanentEmoji).block();
    }

    @DiscordTest
    public void testCreateGuildEmoji() {
        // TODO
    }

    @DiscordTest
    public void testModifyGuildEmoji() {
        // TODO
    }

    @DiscordTest
    public void testDeleteGuildEmoji() {
        // TODO
    }
}
