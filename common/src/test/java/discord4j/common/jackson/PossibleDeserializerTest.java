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
import discord4j.common.json.PossiblePojo;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

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
