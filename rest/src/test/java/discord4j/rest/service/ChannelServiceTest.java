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
import discord4j.common.json.request.ChannelModifyRequest;
import discord4j.common.json.request.MessageCreateRequest;
import discord4j.common.json.response.ChannelResponse;
import discord4j.common.json.response.MessageResponse;
import discord4j.rest.http.EmptyReaderStrategy;
import discord4j.rest.http.EmptyWriterStrategy;
import discord4j.rest.http.JacksonReaderStrategy;
import discord4j.rest.http.JacksonWriterStrategy;
import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;

public class ChannelServiceTest {

	@Test
	public void testGetChannel() {
		long channelId = Long.parseUnsignedLong(System.getenv("permanentChannel"));

		ChannelResponse channel = getChannelService().getChannel(channelId).block();
		assertNotNull(channel);
	}

	private ChannelService getChannelService() {
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

		return new ChannelService(router);
	}

	private ObjectMapper getMapper() {
		return new ObjectMapper()
				.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
				.registerModule(new PossibleModule());
	}

	@Test
	public void testModifyChannel() {
		long channelId = Long.parseUnsignedLong(System.getenv("modifyChannel"));

		ChannelModifyRequest req =
				new ChannelModifyRequest(Possible.absent(), Possible.absent(), Possible.of("test modify"), Possible
						.absent(), Possible.absent(), Possible.absent(), Possible.absent(), Possible.absent());
		ChannelResponse channel = getChannelService().modifyChannel(channelId, req).block();

		assertNotNull(channel);
	}

	@Test
	public void testDeleteChannel() {
		long channelId = Long.parseUnsignedLong(System.getenv("deleteChannel")); // TODO: kinda sucks

		ChannelResponse channel = getChannelService().deleteChannel(channelId).block();

		assertNotNull(channel);
	}

	@Test
	public void testGetMessages() {
		long channelId = Long.parseUnsignedLong(System.getenv("permanentChannel"));

		MessageResponse[] messages = getChannelService().getMessages(channelId, Collections.emptyMap()).block();

		assertNotNull(messages);
	}

	@Test
	public void testGetMessage() {
		long channelId = Long.parseUnsignedLong(System.getenv("permanentChannel"));
		long messageId = Long.parseUnsignedLong(System.getenv("permanentMessage"));

		MessageResponse message = getChannelService().getMessage(channelId, messageId).block();

		assertNotNull(message);
	}

	@Test
	public void testCreateMessage() {
		long channelId = Long.parseUnsignedLong(System.getenv("modifyChannel"));

		MessageCreateRequest req = new MessageCreateRequest("Hello world", null, false, null);
		MessageResponse message = getChannelService().createMessage(channelId, req).block();

		assertNotNull(message);
	}
}
