package discord4j.http.util;

import discord4j.util.UrlBuilder;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class UrlBuilderTest {

    @Test
    public void testUriWithoutTemplateVariable() {
        String template = "/gateway";
        assertEquals(template, UrlBuilder.expand(template));
    }

    @Test
    public void testUriWithOneVariable() {
        String template = "/channel/{channel.id}";
        String expected = "/channel/123456789";
        assertEquals(expected, UrlBuilder.expand(template, 123456789));
    }

    @Test
    public void testUriWithTwoVariables() {
        String template = "/channels/{channel.id}/messages/{message.id}";
        String expected = "/channels/123456789/messages/987654321";
        assertEquals(expected, UrlBuilder.expand(template, 123456789, 987654321));
    }

    @Test
    public void testUriWithOneVariableAndOneQueryParameter() {
        String template = "/channels/{channel.id}/messages";
        String expected = "/channels/123456789/messages?after=101010";
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("channel.id", 123456789);
        map.put("after", 101010);
        assertEquals(expected, UrlBuilder.expand(template, map));

        Map<String, Object> map2 = new LinkedHashMap<>();
        map2.put("after", 101010);
        assertEquals(expected, UrlBuilder.expand(template, map2, 123456789));
    }

    @Test
    public void testUriWithOneVariableAndTwoQueryParameters() {
        String template = "/channels/{channel.id}/messages";
        String expected = "/channels/123456789/messages?after=101010&before=151515";
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("channel.id", 123456789);
        map.put("after", 101010);
        map.put("before", 151515);
        assertEquals(expected, UrlBuilder.expand(template, map));

        Map<String, Object> map2 = new LinkedHashMap<>();
        map2.put("after", 101010);
        map2.put("before", 151515);
        assertEquals(expected, UrlBuilder.expand(template, map2, 123456789));
    }
}
