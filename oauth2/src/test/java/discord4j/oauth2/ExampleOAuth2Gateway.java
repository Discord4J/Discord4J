package discord4j.oauth2;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.GuildMemberAddRequest;
import discord4j.oauth2.object.AccessToken;

import java.util.Collections;

public class ExampleOAuth2Gateway {

    public static void main(String[] args) {
        GatewayDiscordClient gateway = DiscordClient.create(System.getenv("token"))
                .login()
                .block();

        Guild guild = gateway.createGuild(spec -> spec.setName("Discord4J OAuth2 Gateway Test")
            .setRegion(gateway.getRegions().blockFirst())
            .setVerificationLevel(Guild.VerificationLevel.NONE)
            .setDefaultMessageNotificationLevel(Guild.NotificationLevel.ONLY_MENTIONS)).block();

        OAuth2Client client = OAuth2Client.createFromCredentials(spec -> spec.addScope(Scope.GUILDS_JOIN))
                .setClientId(Long.parseLong(System.getenv("id")))
                .setClientSecret(System.getenv("secret"))
                .build();

        GuildMemberAddRequest req = GuildMemberAddRequest.builder()
                .accessToken(client.getApplicationOwnerToken(spec -> spec.addScope(Scope.GUILDS_JOIN)).map(AccessToken::asString).block())
                .nick("test")
                .deaf(false)
                .mute(false)
                .roles(Collections.emptyList())
                .build();
        long user = gateway.getApplicationInfo().map(ApplicationInfo::getOwnerId).block().asLong();
        gateway.getRestClient().getGuildService().addGuildMember(guild.getId().asLong(), user, req).block();
        guild.delete().block();

        gateway.logout().block();
    }
}
