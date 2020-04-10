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

import discord4j.discordjson.json.*;
import discord4j.discordjson.possible.Possible;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.rest.RestTests;
import discord4j.rest.request.Router;
import discord4j.rest.util.MultipartRequest;
import org.junit.Test;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ChannelServiceTest {

    private static final long permanentChannel = Long.parseUnsignedLong(System.getenv("permanentChannel"));
    private static final long permanentMessage = Long.parseUnsignedLong(System.getenv("permanentMessage"));
    private static final long modifyChannel = Long.parseUnsignedLong(System.getenv("modifyChannel"));
    private static final long reactionMessage = Long.parseUnsignedLong(System.getenv("reactionMessage"));
    private static final long editMessage = Long.parseUnsignedLong(System.getenv("editMessage"));
    private static final long permanentOverwrite = Long.parseUnsignedLong(System.getenv("permanentOverwrite"));

    private ChannelService channelService = null;

    private ChannelService getChannelService() {

        if (channelService != null) {
            return channelService;
        }

        String token = System.getenv("token");
        boolean ignoreUnknown = !Boolean.parseBoolean(System.getenv("failUnknown"));
        ObjectMapper mapper = RestTests.getMapper(ignoreUnknown);
        Router router = RestTests.getRouter(token, mapper);

        return channelService = new ChannelService(router);
    }

    @Test
    public void testGetChannel() {
        getChannelService().getChannel(permanentChannel).block();
    }

    @Test
    public void testModifyChannel() {
        ChannelModifyRequest req = ImmutableChannelModifyRequest.builder()
            .topic(Possible.of("test modify"))
            .build();
        getChannelService().modifyChannel(modifyChannel, req, null).block();
    }

    @Test
    public void testDeleteChannel() {
        // TODO
    }

    @Test
    public void testGetMessages() {
        getChannelService().getMessages(permanentChannel, Collections.emptyMap()).then().block();
    }

    @Test
    public void testGetMessage() {
        getChannelService().getMessage(permanentChannel, permanentMessage).block();
    }

    @Test
    public void testCreateMessage() {
        MessageCreateRequest req = ImmutableMessageCreateRequest.builder()
            .content(Possible.of("Hello world"))
            .build();
        getChannelService().createMessage(permanentChannel, new MultipartRequest(req)).block();
    }

    @Test
    public void testCreateMessageWithFile() throws IOException {
        MessageCreateRequest req = ImmutableMessageCreateRequest.builder()
            .content(Possible.of("Hello world with file!"))
            .build();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("fileTest.txt")) {
            if (inputStream == null) {
                throw new NullPointerException();
            }
            byte[] bytes = readAllBytes(inputStream);
            MultipartRequest request = new MultipartRequest(req, "fileTest.txt", new ByteArrayInputStream(bytes));
            getChannelService().createMessage(permanentChannel, request).block();
        }
    }

    @Test
    public void testCreateMessagesWithMultipleFiles() throws IOException {
        MessageCreateRequest req = ImmutableMessageCreateRequest.builder()
            .content(Possible.of("Hello world with *multiple* files!"))
            .build();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("fileTest.txt")) {
            if (inputStream == null) {
                throw new NullPointerException();
            }
            byte[] bytes = readAllBytes(inputStream);

            ByteArrayInputStream in0 = new ByteArrayInputStream(bytes);
            ByteArrayInputStream in1 = new ByteArrayInputStream(bytes);
            List<Tuple2<String, InputStream>> files = Arrays.asList(Tuples.of("file0.txt", in0),
                    Tuples.of("file1.txt", in1));

            MultipartRequest request = new MultipartRequest(req, files);
            getChannelService().createMessage(permanentChannel, request).block();
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
        getChannelService().createReaction(permanentChannel, reactionMessage, "❤").block();
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
        getChannelService().getReactions(permanentChannel, permanentMessage, "❤", Collections.emptyMap()).then()
                .block();
    }

    @Test
    public void testDeleteAllReactions() {
        // TODO
    }

    @Test
    public void testEditMessage() {
        MessageEditRequest req = ImmutableMessageEditRequest.builder()
            .content(Possible.of(Optional.of("This is a message I can edit.")))
            .build();
        getChannelService().editMessage(permanentChannel, editMessage, req).block();
    }

    @Test
    public void testDeleteMessage() {
        MessageCreateRequest req = ImmutableMessageCreateRequest.builder()
            .content(Possible.of("Going to delete this!"))
            .build();
        MessageData response = getChannelService().createMessage(permanentChannel, new MultipartRequest(req)).block();
        getChannelService().deleteMessage(permanentChannel, Long.parseUnsignedLong(response.id()), "This is just a test!").block();
    }

    @Test
    public void testBulkDeleteMessages() {
        // TODO
    }

    @Test
    public void testEditChannelPermissions() {
        PermissionsEditRequest req = ImmutablePermissionsEditRequest
                .builder()
                .allow(0)
                .deny(0)
                .type("member")
                .build();
        getChannelService().editChannelPermissions(modifyChannel, permanentOverwrite, req, null).block();
    }

    @Test
    public void testGetChannelInvites() {
        getChannelService().getChannelInvites(permanentChannel).then().block();
    }

    @Test
    public void testCreateChannelInvite() {
        InviteCreateRequest req = ImmutableInviteCreateRequest
                .builder()
                .maxAge(1)
                .maxUses(0)
                .temporary(true)
                .unique(true)
                .build();
        getChannelService().createChannelInvite(modifyChannel, req, null).block();
    }

    @Test
    public void testDeleteChannelPermission() {
        // TODO
    }

    @Test
    public void testTriggerTypingIndicator() {
        getChannelService().triggerTypingIndicator(permanentChannel).block();
    }

    @Test
    public void testGetPinnedMessages() {
        getChannelService().getPinnedMessages(permanentChannel).then().block();
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
