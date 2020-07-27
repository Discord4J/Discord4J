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
        GatewayDiscordClient gateway = DiscordClient.create(System.getenv("token"))
                .gateway()
                .login()
                .block();

        assert gateway != null;
        gateway.getWebhookById(Snowflake.of(System.getenv("webhook_id")))
                .flatMap(webhook -> webhook.execute(spec -> {
                    spec.setContent("hello from a sneaky webhook.");
                    try {
                        spec.addFile("first file.txt", new FileInputStream(System.getenv("webhook_file1")));
                        spec.addFile("second file.png", new FileInputStream(System.getenv("webhook_file2")));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                })).block();

        gateway.getWebhookByIdWithToken(
                Snowflake.of(System.getenv("webhook_id")),
                System.getenv("webhook_token")
        ).flatMap(webhook -> webhook.execute(spec -> {
            spec.setContent("hello from a sneaky webhook.");
            for (int i = 0; i < 10; i++) {
                int j = i;
                spec.addEmbed(embed ->
                        embed.setDescription("I can create a lot of embeds at once too. #" + (j + 1))
                );
            }
        })).block();


        gateway.getChannelById(Snowflake.of(System.getenv("webhook_channel")))
                .flatMap(channel -> ((TextChannel) channel).createWebhook(webhook ->
                        webhook.setReason("testing").setName("A testy boi")))
                .flatMap(hook -> hook.executeAndWait(spec ->
                        spec.setContent("you can execute webhooks from webhook objects as well.")
                ).thenReturn(hook))
                .flatMap(hook -> hook.delete("good bye"))
                .block();
    }
}
