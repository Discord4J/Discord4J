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
package discord4j.rest.util;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RouteUtilsTest {

    @Test
    public void testUriWithoutTemplateVariable() {
        String template = "/gateway";
        assertEquals(template, RouteUtils.expand(template));
    }

    @Test
    public void testUriWithOneVariable() {
        String template = "/channel/{channel.id}";
        String expected = "/channel/123456789";
        assertEquals(expected, RouteUtils.expand(template, 123456789));
    }

    @Test
    public void testUriWithTwoVariables() {
        String template = "/channels/{channel.id}/messages/{message.id}";
        String expected = "/channels/123456789/messages/987654321";
        assertEquals(expected, RouteUtils.expand(template, 123456789, 987654321));
    }

	@Test
	public void testUriWithOneVariableAndOneQueryParameter() {
		String template = "/channels/{channel.id}/messages";
		String expected = "/channels/123456789/messages?after=101010";
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("after", 101010);
		assertEquals(expected, RouteUtils.expandQuery(RouteUtils.expand(template, 123456789), map));

		Map<String, Object> map2 = new LinkedHashMap<>();
		map2.put("after", 101010);
		assertEquals(expected, RouteUtils.expandQuery(RouteUtils.expand(template, 123456789), map2));
	}

	@Test
	public void testUriWithOneVariableAndTwoQueryParameters() {
		String template = "/channels/{channel.id}/messages";
		String expected = "/channels/123456789/messages?after=101010&before=151515";
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("after", 101010);
		map.put("before", 151515);
		assertEquals(expected, RouteUtils.expandQuery(RouteUtils.expand(template, 123456789), map));

		Map<String, Object> map2 = new LinkedHashMap<>();
		map2.put("after", 101010);
		map2.put("before", 151515);
		assertEquals(expected, RouteUtils.expandQuery(RouteUtils.expand(template, 123456789), map2));
	}
}
