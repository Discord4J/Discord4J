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

package discord4j.core.util;

import discord4j.discordjson.json.gateway.RequestGuildMembers;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.intent.IntentSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidationUtilTest {

    @Test
    public void shouldLetAQueryForAllMembersHappenIfIntentsAreAbsent() {
        ValidationUtil.validateRequestGuildMembers(
            RequestGuildMembers.builder().guildId("1234").query("").limit(0).build(),
            Possible.absent()
        );
    }

    @Test
    public void shouldMakeSureExactlyOneOfQueryOrUserIds() {
        ValidationUtil.validateRequestGuildMembers(
            RequestGuildMembers.builder().guildId("1234").query("prefix").limit(0).build(),
            Possible.absent()
        );

        ValidationUtil.validateRequestGuildMembers(
            RequestGuildMembers.builder().guildId("1234").addUserId("9876").limit(1).build(),
            Possible.absent()
        );

        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateRequestGuildMembers(
            RequestGuildMembers.builder().guildId("1234").query("prefix").addUserId("5678").limit(0).build(),
            Possible.absent()
        ));

        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateRequestGuildMembers(
            RequestGuildMembers.builder().guildId("1234").limit(0).build(),
            Possible.absent()
        ));
    }

    @Test
    public void shouldRequireGuildPresencesIntentsIfRequestingEntireMemberListAndUsingIntents() {
        ValidationUtil.validateRequestGuildMembers(
            RequestGuildMembers.builder().guildId("1234").query("").limit(0).build(),
            Possible.absent()
        );
        ValidationUtil.validateRequestGuildMembers(
            RequestGuildMembers.builder().guildId("1234").query("prefix").limit(0).build(),
            Possible.of(IntentSet.none())
        );
        ValidationUtil.validateRequestGuildMembers(
            RequestGuildMembers.builder().guildId("1234").query("").limit(100).build(),
            Possible.of(IntentSet.none())
        );

        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateRequestGuildMembers(
            RequestGuildMembers.builder().guildId("1234").query("").limit(0).build(),
            Possible.of(IntentSet.none())
        ));
    }
}
