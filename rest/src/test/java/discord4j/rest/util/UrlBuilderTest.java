package discord4j.rest.util;

import org.junit.Test;

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
}
