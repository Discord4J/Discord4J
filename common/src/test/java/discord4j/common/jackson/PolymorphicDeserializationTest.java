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

package discord4j.common.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.json.payload.*;
import discord4j.common.json.payload.dispatch.Dispatch;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.*;

public class PolymorphicDeserializationTest {

	private static final Logger log = LoggerFactory.getLogger(PolymorphicDeserializationTest.class);

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
	public void testDispatchReady() throws IOException {
		String input = "{\n" +
				"  \"t\": \"READY\",\n" +
				"  \"s\": 1,\n" +
				"  \"op\": 0,\n" +
				"  \"d\": {\n" +
				"    \"v\": 6,\n" +
				"    \"user_settings\": {},\n" +
				"    \"user\": {\n" +
				"      \"verified\": true,\n" +
				"      \"username\": \"Reacton\",\n" +
				"      \"mfa_enabled\": true,\n" +
				"      \"id\": \"344487830824943618\",\n" +
				"      \"email\": null,\n" +
				"      \"discriminator\": \"6221\",\n" +
				"      \"bot\": true,\n" +
				"      \"avatar\": \"bb1ac764222d6b3242d6a4f78214c9c9\"\n" +
				"    },\n" +
				"    \"session_id\": \"070e00aac4f437d4219501063559164c\",\n" +
				"    \"relationships\": [],\n" +
				"    \"private_channels\": [],\n" +
				"    \"presences\": [],\n" +
				"    \"guilds\": [\n" +
				"      {\n" +
				"        \"unavailable\": true,\n" +
				"        \"id\": \"135197118292819968\"\n" +
				"      },\n" +
				"      {\n" +
				"        \"unavailable\": true,\n" +
				"        \"id\": \"346719828784185375\"\n" +
				"      }\n" +
				"    ],\n" +
				"    \"_trace\": [\n" +
				"      \"gateway-prd-main-qgwq\",\n" +
				"      \"discord-sessions-prd-1-12\"\n" +
				"    ]\n" +
				"  }\n" +
				"}";
		GatewayPayload payload = mapper.readValue(input, GatewayPayload.class);

		assertEquals(0, payload.getOp());
		assertTrue(payload.getData() instanceof Dispatch);
		assertEquals(6, ((Dispatch) payload.getData()).get("v"));
	}

	@Test
	public void testHeartbeat() throws IOException {
		String input = "{\n" +
				"    \"op\": 1,\n" +
				"    \"d\": 251\n" +
				"}";
		GatewayPayload payload = mapper.readValue(input, GatewayPayload.class);

		assertEquals(1, payload.getOp());
		assertTrue(payload.getData() instanceof Heartbeat);
		assertEquals(251, ((Heartbeat) payload.getData()).getSeq());
	}

	@Test
	public void testReconnect() throws IOException {
		String input = "{\n" +
				"    \"op\": 7,\n" +
				"    \"d\": null\n" +
				"}";
		GatewayPayload payload = mapper.readValue(input, GatewayPayload.class);

		assertEquals(7, payload.getOp());
		assertNull(payload.getData());
	}

	@Test
	public void testInvalidSession() throws IOException {
		String input = "{\n" +
				"    \"op\": 9,\n" +
				"    \"d\": false\n" +
				"}";
		GatewayPayload payload = mapper.readValue(input, GatewayPayload.class);

		assertEquals(9, payload.getOp());
		assertTrue(payload.getData() instanceof InvalidSession);
		assertEquals(false, ((InvalidSession) payload.getData()).isResumable());
	}

	@Test
	public void testHello() throws IOException {
		String input = "{\n" +
				"    \"op\": 10,\n" +
				"    \"d\": {\n" +
				"        \"heartbeat_interval\": 45000,\n" +
				"        \"_trace\": [\"discord-gateway-prd-1-99\"]\n" +
				"    }\n" +
				"}";
		GatewayPayload payload = mapper.readValue(input, GatewayPayload.class);

		assertEquals(10, payload.getOp());
		assertTrue(payload.getData() instanceof Hello);
		assertEquals(45000, ((Hello) payload.getData()).getHeartbeatInterval());
	}

	@Test
	public void testHeartbeatAck() throws IOException {
		String input = "{\n" +
				"    \"op\": 11\n" +
				"}";
		GatewayPayload payload = mapper.readValue(input, GatewayPayload.class);

		assertEquals(11, payload.getOp());
		assertNull(payload.getData());
	}
}
