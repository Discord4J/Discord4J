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
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.stream.IntStream;

import static discord4j.core.spec.MessageCreateFields.File;

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

        // The ID of a thread.
        Snowflake threadId = Snowflake.of(System.getenv("thread_id"));

        // The path of a .txt file.
        String file1 = System.getenv("webhook_file1");

        // The path of a .png file
        String file2 = System.getenv("webhook_file2");

        GatewayDiscordClient gateway = DiscordClient.create(token)
                .gateway()
                .login()
                .block();

        assert gateway != null;

        gateway.getWebhookByIdWithToken(secretWebhookId, secretWebhookToken)
            .flatMap(webhook -> webhook.execute()
                .withThreadId(threadId)
                .withContent("A webhook can execute in channels with threads doesn't have access to."))
            .block();

        gateway.getWebhookById(webhookId)
                .flatMap(webhook -> {
                    try {
                        return webhook.execute()
                                .withContent("A webhook can upload multiple files.")
                                .withFiles(File.of("first file.txt", new FileInputStream(file1)),
                                        File.of("second file.png", new FileInputStream(file2)));
                    } catch (FileNotFoundException e) {
                        return Mono.error(e);
                    }
                })
                .block();

        gateway.getWebhookByIdWithToken(secretWebhookId, secretWebhookToken)
                .flatMap(webhook -> webhook.execute()
                        .withContent("A webhook can execute in channels the bot doesn't have access to."))
                .block();

        gateway.getWebhookById(webhookId)
                .flatMap(webhook -> webhook.execute()
                        .withContent("A webhook can create several embeds.")
                        .withEmbeds(IntStream.range(0, 10)
                                .mapToObj(i -> EmbedCreateSpec.create()
                                        .withDescription("I can create a lot of embeds at once too. #" + (i + 1)))
                                .toArray(EmbedCreateSpec[]::new)))
                .block();

        gateway.getChannelById(webhookChannel)
                .flatMap(channel -> ((TextChannel) channel).createWebhook("A webhook for testing")
                        .withReason("testing"))
                .flatMap(hook -> hook.execute()
                        .withWaitForMessage(true)
                        .withContent("you can execute webhooks after you create them.")
                        .thenReturn(hook))
                .flatMap(hook -> hook.delete("deleting test webhook"))
                .block();
    }
}
