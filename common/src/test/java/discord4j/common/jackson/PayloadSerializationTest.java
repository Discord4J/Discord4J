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
package discord4j.common.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.json.payload.*;
import discord4j.common.json.request.GameRequest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.*;

public class PayloadSerializationTest {

	private static final Logger log = LoggerFactory.getLogger(PayloadSerializationTest.class);

	private ObjectMapper mapper;

	@Before
	public void init() {
		mapper = new ObjectMapper()
				.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
				.configure(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false) // required
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
				.registerModule(new PossibleModule());
	}

	@Test
	public void testHeartbeat() throws IOException {
		String expected = "{\"op\":1,\"d\":251,\"s\":null,\"t\":null}";

		GatewayPayload payload = new GatewayPayload(1, new Heartbeat(251), null, null);
		String result = mapper.writeValueAsString(payload);

		assertEquals(mapper.readTree(expected), mapper.readTree(result));
	}

	@Test
	public void testIdentify() throws IOException {
		String expected = ("{\n" +
				"    \"op\":2,\n" +
				"    \"d\":{\n" +
				"        \"token\": \"my_token\",\n" +
				"        \"properties\": {\n" +
				"            \"$os\": \"linux\",\n" +
				"            \"$browser\": \"disco\",\n" +
				"            \"$device\": \"disco\"\n" +
				"        },\n" +
				"        \"compress\": true,\n" +
				"        \"large_threshold\": 250\n" +
				"    },\n" +
				"    \"s\":null,\n" +
				"    \"t\":null\n" +
				"}").replaceAll("\\s+", "");

		Identify identify = new Identify("my_token", new IdentifyProperties("linux", "disco", "disco"), true, 250, Possible.absent(), Possible.absent());
		GatewayPayload payload = new GatewayPayload(2, identify, null, null);
		String result = mapper.writeValueAsString(payload);

		assertEquals(mapper.readTree(expected), mapper.readTree(result));
	}

	@Test
	public void testStatusUpdate() throws IOException {
		String expected = ("{\n" +
				"    \"op\": 3,\n" +
				"    \"d\":{\n" +
				"        \"since\": 91879201,\n" +
				"        \"game\": {\n" +
				"            \"name\": \"some_game\",\n" +
				"            \"type\": 0\n" +
				"        },\n" +
				"        \"status\": \"online\",\n" +
				"        \"afk\": false\n" +
				"    },\n" +
				"    \"s\":null,\n" +
				"    \"t\":null\n" +
				"}").replaceAll("\\s+", "");

		StatusUpdate statusUpdate = new StatusUpdate(91879201L, new GameRequest("some_game", 0, Possible.absent()), "online", false);
		GatewayPayload payload = new GatewayPayload(3, statusUpdate, null, null);
		String result = mapper.writeValueAsString(payload);

		assertEquals(expected, result);
	}

	@Test
	public void testVoiceStateUpdate() throws IOException {
		String expected = ("{\n" +
				"    \"op\":4,\n" +
				"    \"d\":{\n" +
				"        \"guild_id\": \"41771983423143937\",\n" +
				"        \"channel_id\": \"127121515262115840\",\n" +
				"        \"self_mute\": false,\n" +
				"        \"self_deaf\": false\n" +
				"    },\n" +
				"    \"s\":null,\n" +
				"    \"t\":null\n" +
				"}").replaceAll("\\s+", "");

		VoiceStateUpdate voiceStateUpdate = new VoiceStateUpdate(41771983423143937L, 127121515262115840L, false, false);
		GatewayPayload payload = new GatewayPayload(4, voiceStateUpdate, null, null);
		String result = mapper.writeValueAsString(payload);

		assertEquals(expected, result);
	}

	@Test
	public void testVoiceServerPing() throws IOException {
		// TODO
	}

	@Test
	public void testResume() throws IOException {
		String expected = ("{\n" +
				"    \"op\":6,\n" +
				"    \"d\":{\n" +
				"        \"token\": \"randomstring\",\n" +
				"        \"session_id\": \"evenmorerandomstring\",\n" +
				"        \"seq\": 1337\n" +
				"    },\n" +
				"    \"s\":null,\n" +
				"    \"t\":null\n" +
				"}").replaceAll("\\s+", "");

		Resume resume = new Resume("randomstring", "evenmorerandomstring", 1337);
		GatewayPayload payload = new GatewayPayload(6, resume, null, null);
		String result = mapper.writeValueAsString(payload);

		assertEquals(mapper.readTree(expected), mapper.readTree(result));
	}

	@Test
	public void testRequestGuildMembers() throws IOException {
		String expected = ("{\n" +
				"    \"op\":8,\n" +
				"    \"d\":{\n" +
				"        \"guild_id\": \"41771983444115456\",\n" +
				"        \"query\": \"\",\n" +
				"        \"limit\": 0\n" +
				"    },\n" +
				"    \"s\":null,\n" +
				"    \"t\":null\n" +
				"}").replaceAll("\\s+", "");

		RequestGuildMembers requestGuildMembers = new RequestGuildMembers(41771983444115456L, "", 0);
		GatewayPayload payload = new GatewayPayload(8, requestGuildMembers, null, null);
		String result = mapper.writeValueAsString(payload);

		assertEquals(mapper.readTree(expected), mapper.readTree(result));
	}
}
