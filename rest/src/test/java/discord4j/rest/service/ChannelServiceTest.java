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

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.*;
import discord4j.rest.RestTests;
import discord4j.rest.util.MultipartRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "D4J_TEST_DISCORD", matches = "true")
public class ChannelServiceTest {

    private static final long permanentChannel = Snowflake.asLong(System.getenv("permanentChannel"));
    private static final long permanentMessage = Snowflake.asLong(System.getenv("permanentMessage"));
    private static final long modifyChannel = Snowflake.asLong(System.getenv("modifyChannel"));
    private static final long reactionMessage = Snowflake.asLong(System.getenv("reactionMessage"));
    private static final long editMessage = Snowflake.asLong(System.getenv("editMessage"));
    private static final long permanentOverwrite = Snowflake.asLong(System.getenv("permanentOverwrite"));

    private ChannelService channelService;

    @BeforeAll
    public void setup() {
        channelService = new ChannelService(RestTests.defaultRouter());
    }

    @Test
    public void testGetChannel() {
        channelService.getChannel(permanentChannel).block();
    }

    @Test
    public void testModifyChannel() {
        ChannelModifyRequest req = ChannelModifyRequest.builder()
                .topic("test modify")
                .build();
        channelService.modifyChannel(modifyChannel, req, null).block();
    }

    @Test
    public void testDeleteChannel() {
        // TODO
    }

    @Test
    public void testGetMessages() {
        channelService.getMessages(permanentChannel, Collections.emptyMap()).then().block();
    }

    @Test
    public void testGetMessage() {
        channelService.getMessage(permanentChannel, permanentMessage).block();
    }

    @Test
    public void testCreateMessage() {
        MessageCreateRequest req = MessageCreateRequest.builder()
                .content("Hello world")
                .build();
        channelService.createMessage(permanentChannel, MultipartRequest.ofRequest(req)).block();
    }

    @Test
    public void testCreateMessageWithFile() throws IOException {
        MessageCreateRequest req = MessageCreateRequest.builder()
                .content("Hello world with file!")
                .build();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("fileTest.txt")) {
            if (inputStream == null) {
                throw new NullPointerException();
            }
            byte[] bytes = readAllBytes(inputStream);
            MultipartRequest<MessageCreateRequest> request = MultipartRequest.ofRequest(req)
                    .addFile("fileTest.txt", new ByteArrayInputStream(bytes));
            channelService.createMessage(permanentChannel, request).block();
        }
    }

    @Test
    public void testCreateMessagesWithMultipleFiles() throws IOException {
        MessageCreateRequest req = MessageCreateRequest.builder()
                .content("Hello world with *multiple* files!")
                .build();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("fileTest.txt")) {
            if (inputStream == null) {
                throw new NullPointerException();
            }
            byte[] bytes = readAllBytes(inputStream);

            ByteArrayInputStream in0 = new ByteArrayInputStream(bytes);
            ByteArrayInputStream in1 = new ByteArrayInputStream(bytes);
            /*~~>*/List<Tuple2<String, InputStream>> files = Arrays.asList(Tuples.of("file0.txt", in0),
                    Tuples.of("file1.txt", in1));

            MultipartRequest<MessageCreateRequest> request = MultipartRequest.ofRequestAndFiles(req, files);
            channelService.createMessage(permanentChannel, request).block();
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        int size = 8192;
        int max = Integer.MAX_VALUE - 8;
        byte[] buf = new byte[size];
        int capacity = buf.length;
        int nread = 0;
        int n;
        for (; ; ) {
            while ((n = inputStream.read(buf, nread, capacity - nread)) > 0) {
                nread += n;
            }
            if (n < 0) {
                break;
            }
            if (capacity <= max - capacity) {
                capacity = capacity << 1;
            } else {
                if (capacity == max) {
                    throw new OutOfMemoryError("Required array size too large");
                }
                capacity = max;
            }
            buf = Arrays.copyOf(buf, capacity);
        }
        return (capacity == nread) ? buf : Arrays.copyOf(buf, nread);
    }

    @Test
    public void testCreateReaction() {
        channelService.createReaction(permanentChannel, reactionMessage, "❤").block();
    }

    @Test
    public void testDeleteOwnReaction() {
        // TODO
    }

    @Test
    public void testDeleteReaction() {
        // TODO
    }

    @Test
    public void testGetReactions() throws UnsupportedEncodingException {
        channelService.getReactions(permanentChannel, permanentMessage, "❤", Collections.emptyMap()).then()
                .block();
    }

    @Test
    public void testDeleteAllReactions() {
        // TODO
    }

    @Test
    public void testEditMessage() {
        MessageEditRequest req = MessageEditRequest.builder()
                .contentOrNull("This is a message I can edit.")
                .build();
        channelService.editMessage(permanentChannel, editMessage, MultipartRequest.ofRequest(req)).block();
    }

    @Test
    public void testDeleteMessage() {
        MessageCreateRequest req = MessageCreateRequest.builder()
                .content("Going to delete this!")
                .build();
        MessageData response = channelService.createMessage(permanentChannel, MultipartRequest.ofRequest(req)).block();
        channelService.deleteMessage(permanentChannel, Snowflake.asLong(response.id()), "This is just a " +
                "test!").block();
    }

    @Test
    public void testBulkDeleteMessages() {
        // TODO
    }

    @Test
    public void testEditChannelPermissions() {
        PermissionsEditRequest req = PermissionsEditRequest.builder()
                .allow(0)
                .deny(0)
                .type(1)
                .build();
        channelService.editChannelPermissions(modifyChannel, permanentOverwrite, req, null).block();
    }

    @Test
    public void testGetChannelInvites() {
        channelService.getChannelInvites(permanentChannel).then().block();
    }

    @Test
    public void testCreateChannelInvite() {
        InviteCreateRequest req = InviteCreateRequest.builder()
                .maxAge(1)
                .maxUses(0)
                .temporary(true)
                .unique(true)
                .build();
        channelService.createChannelInvite(modifyChannel, req, null).block();
    }

    @Test
    public void testDeleteChannelPermission() {
        // TODO
    }

    @Test
    public void testTriggerTypingIndicator() {
        channelService.triggerTypingIndicator(permanentChannel).block();
    }

    @Test
    public void testGetPinnedMessages() {
        channelService.getPinnedMessages(permanentChannel).then().block();
    }

    @Test
    public void testAddPinnedMessage() {
        // TODO
    }

    @Test
    public void testDeletePinnedMessage() {
        // TODO
    }

    @Test
    public void testAddGroupDMRecipient() {
        // TODO
    }

    @Test
    public void testDeleteGroupDMRecipient() {
        // TODO
    }
}
