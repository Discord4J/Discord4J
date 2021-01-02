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
        Mono<GuildUpdateData> updateDataMono =  restGuild.getData(true);
        GuildUpdateData updateData = updateDataMono.block();
    }
}
