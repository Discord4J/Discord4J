package discord4j.core.util;

import discord4j.discordjson.json.gateway.RequestGuildMembers;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.intent.IntentSet;
import org.junit.Assert;
import org.junit.Test;

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

        Assert.assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateRequestGuildMembers(
            RequestGuildMembers.builder().guildId("1234").query("prefix").addUserId("5678").limit(0).build(),
            Possible.absent()
        ));

        Assert.assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateRequestGuildMembers(
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

        Assert.assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateRequestGuildMembers(
            RequestGuildMembers.builder().guildId("1234").query("").limit(0).build(),
            Possible.of(IntentSet.none())
        ));
    }
}
