package discord4j.common.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.json.PossiblePojo;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class PossibleDeserializerTest {

    @Test
    public void testPresent() throws IOException {
        ObjectMapper mapper = getMapper();
        PossiblePojo expected = new PossiblePojo(Possible.of("hello world"));

        String json = "{\"string\": \"hello world\"}";
        PossiblePojo result = mapper.readValue(json, PossiblePojo.class);

        assertEquals(expected, result);
    }

    @Test
    public void testAbsent() throws IOException {
        ObjectMapper mapper = getMapper();
        PossiblePojo expected = new PossiblePojo(Possible.absent());

        String json = "{}";
        PossiblePojo result = mapper.readValue(json, PossiblePojo.class);

        assertEquals(expected, result);
    }

    @Test
    public void testNull() throws IOException {
        ObjectMapper mapper = getMapper();
        PossiblePojo expected = new PossiblePojo(null);

        String json = "{\"string\": null}";
        PossiblePojo result = mapper.readValue(json, PossiblePojo.class);

        assertEquals(expected, result);
    }

    private ObjectMapper getMapper() {
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .registerModule(new PossibleModule());
    }
}
