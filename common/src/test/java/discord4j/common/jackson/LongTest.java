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
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import discord4j.common.json.LongPojo;
import discord4j.common.json.OptionalLongPojo;
import discord4j.common.json.UnsignedLongPojo;
import discord4j.common.json.UnsignedOptionalLongPojo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LongTest {

    private ObjectMapper getMapper() {
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .registerModules(new PossibleModule(), new Jdk8Module());
    }

    @Test
    public void testSerializeUnsignedLongToString() throws Exception {
        class Pojo {

            @UnsignedJson
            long unsignedLong = Long.parseUnsignedLong("9223372036854775808"); // 2^63
        }

        String expected = "{\"unsignedLong\":\"9223372036854775808\"}";
        String result = getMapper().writeValueAsString(new Pojo());

        assertEquals(expected, result);
    }

    @Test
    public void testSerializeSignedLongToNumber() throws Exception {
        class Pojo {

            long signedLong = 1234L;
        }

        String expected = "{\"signedLong\":1234}";
        String result = getMapper().writeValueAsString(new Pojo());

        assertEquals(expected, result);
    }

    @Test
    public void testSerializeUnsignedPossibleLongToString() throws Exception {
        @PossibleJson
        class Pojo {

            @UnsignedJson
            PossibleLong unsignedLong = PossibleLong.of(Long.parseUnsignedLong("9223372036854775808")); // 2^63
        }

        String expected = "{\"unsignedLong\":\"9223372036854775808\"}";
        String result = getMapper().writeValueAsString(new Pojo());

        assertEquals(expected, result);
    }

    @Test
    public void testSerializeSignedPossibleLongToNumber() throws Exception {
        @PossibleJson
        class Pojo {

            PossibleLong signedLong = PossibleLong.of(123L);
        }

        String expected = "{\"signedLong\":123}";
        String result = getMapper().writeValueAsString(new Pojo());

        assertEquals(expected, result);
    }

    @Test
    public void testSerializeUnsignedAbsentPossibleLong() throws Exception {
        @PossibleJson
        class Pojo {

            @UnsignedJson
            PossibleLong unsignedLong = PossibleLong.absent();
        }

        String expected = "{}";
        String result = getMapper().writeValueAsString(new Pojo());

        assertEquals(expected, result);
    }

    @Test
    public void testSerializeUnsignedNullPossibleLong() throws Exception {
        @PossibleJson
        class Pojo {

            @UnsignedJson
            PossibleLong unsignedLong = null;
        }

        String expected = "{\"unsignedLong\":null}";
        String result = getMapper().writeValueAsString(new Pojo());

        assertEquals(expected, result);
    }

    @Test
    public void testSerializeArrayOfUnsignedLongsToArrayOfStrings() throws Exception {
        class Pojo {

            @UnsignedJson
            long[] ary = {
                    Long.parseUnsignedLong("9223372036854775808"),
                    Long.parseUnsignedLong("9223372036854775809"),
                    Long.parseUnsignedLong("9223372036854775810")
            };
        }

        String expected = "{\"ary\":[\"9223372036854775808\",\"9223372036854775809\",\"9223372036854775810\"]}";
        String result = getMapper().writeValueAsString(new Pojo());

        assertEquals(expected, result);
    }

    @Test
    public void testDeserializeNumberToSignedLong() throws Exception {
        long expected = 123L;
        long result = getMapper().readValue("{\"someLong\":123}", LongPojo.class).someLong;

        assertEquals(expected, result);
    }

    @Test
    public void testDeserializeStringToUnsignedLong() throws Exception {
        long expected = Long.parseUnsignedLong("9223372036854775808"); // 2^63
        long result = getMapper().readValue("{\"unsignedLong\":\"9223372036854775808\"}",
                UnsignedLongPojo.class).unsignedLong;

        assertEquals(expected, result);
    }

    @Test
    public void testDeserializeStringToSignedOptionalLong() throws Exception {
        long expected = 123L;
        long result = getMapper().readValue("{\"someLong\":\"123\"}", OptionalLongPojo.class).someLong.getAsLong();

        assertEquals(expected, result);
    }

    @Test
    public void testDeserializeStringToUnsignedOptionalLong() throws Exception {
        long expected = Long.parseUnsignedLong("9223372036854775808"); // 2^63
        long result = getMapper().readValue("{\"unsignedLong\":\"9223372036854775808\"}",
                UnsignedOptionalLongPojo.class).unsignedLong;

        assertEquals(expected, result);
    }


}
