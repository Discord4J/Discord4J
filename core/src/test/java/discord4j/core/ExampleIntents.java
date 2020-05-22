package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;

import java.util.List;

public class ExampleIntents {
    public static void main(String[] args) {
        List<Member> members = DiscordClient.create(System.getenv("token"))
            .gateway()
            .setEnabledIntents(IntentSet.of(Intent.GUILD_MEMBERS))
            .login()
            .flatMapMany(gateway ->
                gateway.requestMembers(Snowflake.of(System.getenv("guild")))
            )
            .collectList()
            .block();
    }
}
