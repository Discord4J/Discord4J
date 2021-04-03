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

package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.TextChannel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ExampleWebhook {

    public static void main(String[] args) {

        // The discord bot token.
        String token = System.getenv("token");

        // The ID of a webhook in a channel the bot has MANAGE_WEBHOOKS permission in.
        Snowflake webhookId = Snowflake.of(System.getenv("webhook_id"));

        // The ID and token of a webhook in a channel the bot does not have the MANAGE_WEBHOOKS permission.
        Snowflake secretWebhookId = Snowflake.of(System.getenv("secret_webhook_id"));
        String secretWebhookToken = System.getenv("secret_webhook_token");

        // The ID of a channel the bot has the MANAGE_WEBHOOKS permission in.
        Snowflake webhookChannel = Snowflake.of(System.getenv("webhook_channel"));

        // The path of a .txt file.
        String file1 = System.getenv("webhook_file1");

        // The path of a .png file
        String file2 = System.getenv("webhook_file2");

        GatewayDiscordClient gateway = DiscordClient.create(token)
                .gateway()
                .login()
                .block();

        assert gateway != null;

        gateway.getWebhookById(webhookId)
                .flatMap(webhook -> webhook.execute(spec -> {
                    spec.setContent("A webhook can upload multiple files.");
                    try {
                        spec.addFile("first file.txt", new FileInputStream(file1));
                        spec.addFile("second file.png", new FileInputStream(file2));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }))
                .block();

        gateway.getWebhookByIdWithToken(secretWebhookId, secretWebhookToken)
                .flatMap(webhook -> webhook.execute(spec -> {
                    spec.setContent("A webhook can execute in channels the bot doesn't have access to.");
                }))
                .block();

        gateway.getWebhookById(webhookId)
                .flatMap(webhook -> webhook.execute(spec -> {
                    spec.setContent("A webhook can create several embeds.");
                    for (int i = 0; i < 10; i++) {
                        final int finalI = i;
                        spec.addEmbed(embed ->
                                embed.setDescription("I can create a lot of embeds at once too. #" + (finalI + 1))
                        );
                    }
                }))
                .block();


        gateway.getChannelById(webhookChannel)
                .flatMap(channel -> ((TextChannel) channel).createWebhook(webhook ->
                        webhook.setReason("testing").setName("A webhook for testing")))
                .flatMap(hook -> hook.executeAndWait(spec ->
                        spec.setContent("you can execute webhooks after you create them.")
                ).thenReturn(hook))
                .flatMap(hook -> hook.delete("deleting test webhook"))
                .block();
    }
}
