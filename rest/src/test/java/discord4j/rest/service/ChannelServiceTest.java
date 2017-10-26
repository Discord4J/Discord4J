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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleModule;
import discord4j.common.json.request.*;
import discord4j.rest.http.EmptyReaderStrategy;
import discord4j.rest.http.EmptyWriterStrategy;
import discord4j.rest.http.JacksonReaderStrategy;
import discord4j.rest.http.JacksonWriterStrategy;
import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;

public class ChannelServiceTest {

	private static final long permanentChannel = Long.parseUnsignedLong(System.getenv("permanentChannel"));
	private static final long permanentMessage = Long.parseUnsignedLong(System.getenv("permanentMessage"));
	private static final long modifyChannel = Long.parseUnsignedLong(System.getenv("modifyChannel"));
	private static final long reactionMessage = Long.parseUnsignedLong(System.getenv("reactionMessage"));
	private static final long editMessage = Long.parseUnsignedLong(System.getenv("editMessage"));
	private static final long permanentOverwrite = Long.parseUnsignedLong(System.getenv("permanentOverwrite"));

	private ChannelService channelService = null;

	private ChannelService getChannelService() {

		if (channelService != null) return channelService;

		String token = System.getenv("token");
		ObjectMapper mapper = getMapper();

		SimpleHttpClient httpClient = SimpleHttpClient.builder()
				.baseUrl(Routes.BASE_URL)
				.defaultHeader("Authorization", "Bot " + token)
				.defaultHeader("Content-Type", "application/json")
				.readerStrategy(new JacksonReaderStrategy<>(mapper))
				.readerStrategy(new EmptyReaderStrategy())
				.writerStrategy(new JacksonWriterStrategy(mapper))
				.writerStrategy(new EmptyWriterStrategy())
				.build();

		Router router = new Router(httpClient);

		return channelService = new ChannelService(router);
	}

	private ObjectMapper getMapper() {
		return new ObjectMapper()
				.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
				.registerModule(new PossibleModule());
	}

	@Test
	public void testGetChannel() {
		getChannelService().getChannel(permanentChannel).block();
	}

	@Test
	public void testModifyChannel() {
		ChannelModifyRequest req = new ChannelModifyRequest(Possible.absent(), Possible.absent(),
				Possible.of("test modify"), Possible.absent(), Possible.absent(), Possible.absent(), Possible.absent(),
				Possible.absent());
		getChannelService().modifyChannel(modifyChannel, req).block();
	}

	@Test
	public void testDeleteChannel() {
		// TODO
	}

	@Test
	public void testGetMessages() {
		getChannelService().getMessages(permanentChannel, Collections.emptyMap()).block();
	}

	@Test
	public void testGetMessage() {
		getChannelService().getMessage(permanentChannel, permanentMessage).block();
	}

	@Test
	public void testCreateMessage() {
		MessageCreateRequest req = new MessageCreateRequest("Hello world", null, false, null);
		getChannelService().createMessage(permanentChannel, req).block();
	}

	@Test
	public void testCreateReaction() throws UnsupportedEncodingException {
		String reaction = URLEncoder.encode("❤", "UTF-8");
		getChannelService().createReaction(permanentChannel, reactionMessage, reaction).block();
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
		String reaction = URLEncoder.encode("❤", "UTF-8");
		getChannelService().getReactions(permanentChannel, permanentMessage, reaction, Collections.emptyMap()).block();
	}

	@Test
	public void testDeleteAllReactions() {
		// TODO
	}

	@Test
	public void testEditMessage() {
		MessageEditRequest req = new MessageEditRequest(Possible.of("This is a message I can edit."), Possible.absent());
		getChannelService().editMessage(permanentChannel, editMessage, req).block();
	}

	@Test
	public void testDeleteMessage() {
		// TODO
	}

	@Test
	public void testBulkDeleteMessages() {
		// TODO
	}

	@Test
	public void testEditChannelPermissions() {
		PermissionsEditRequest req = new PermissionsEditRequest(0, 0, "member");
		getChannelService().editChannelPermissions(modifyChannel, permanentOverwrite, req).block();
	}

	@Test
	public void testGetChannelInvites() {
		getChannelService().getChannelInvites(permanentChannel).block();
	}

	@Test
	public void testCreateChannelInvite() {
		InviteCreateRequest req = new InviteCreateRequest(1, 0, true, true);
		getChannelService().createChannelInvite(modifyChannel, req).block();
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
		getChannelService().getPinnedMessages(permanentChannel).block();
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
