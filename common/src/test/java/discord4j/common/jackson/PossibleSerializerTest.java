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
import discord4j.common.json.PossiblePojoWithLong;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PossibleSerializerTest {

    @Test
    public void testPresent() throws Exception {
        ObjectMapper mapper = getMapper();
        String expected = "{\"string\":\"Hello world\",\"llong\":123}";

        PossiblePojoWithLong pojo = new PossiblePojoWithLong(Possible.of("Hello world"), PossibleLong.of(123L));
        String result = mapper.writeValueAsString(pojo);

        assertEquals(expected, result);
    }

    @Test
    public void testAbsent() throws Exception {
        ObjectMapper mapper = getMapper();
        String expected = "{}";

        PossiblePojoWithLong pojo = new PossiblePojoWithLong(Possible.absent(), PossibleLong.absent());
        String result = mapper.writeValueAsString(pojo);

        assertEquals(expected, result);
    }

    @Test
    public void testNull() throws Exception {
        ObjectMapper mapper = getMapper();
        String expected = "{\"string\":null,\"llong\":null}";

        PossiblePojoWithLong pojo = new PossiblePojoWithLong(null, null);
        String result = mapper.writeValueAsString(pojo);

        assertEquals(expected, result);
    }

    private ObjectMapper getMapper() {
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .registerModule(new PossibleModule());
    }
}
