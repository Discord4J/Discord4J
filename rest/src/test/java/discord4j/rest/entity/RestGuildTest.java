package discord4j.rest.entity;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.GuildUpdateData;
import discord4j.rest.RestClient;
import reactor.core.publisher.Mono;

public class RestGuildTest {
    public static void main(String[] args) {
        RestClient restClient = RestClient.create(System.getenv("token"));
        System.out.println(restClient.getRestResources().getJacksonResources().toString());
        RestGuild restGuild = restClient.getGuildById(Snowflake.of(System.getenv("guildId")));
        Mono<GuildUpdateData> updateDataMono =  restGuild.getData(true); // Get data with possibility of requesting with query parameter "with_count"
        GuildUpdateData updateData = updateDataMono.block();
        System.out.println(updateData.approximateMemberCount().get()); // Will just work if getData(withCounts == true)
        System.out.println(updateData.approximatePresenceCount().get()); // Will just work if getData(withCounts == true)
    }
}
