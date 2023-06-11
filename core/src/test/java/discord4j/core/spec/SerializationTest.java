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

package discord4j.core.spec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.JacksonResources;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SerializationTest {

    private static final Logger log = Loggers.getLogger(SerializationTest.class);

    ObjectMapper mapper;

    @BeforeAll
    public void setup() {
        JacksonResources resources = JacksonResources.create();
        mapper = resources.getObjectMapper();
    }

    @Test
    public void canSerializeEmbedCreateSpec() throws JsonProcessingException {
        // TODO: add all fields to test
        EmbedCreateSpec spec = EmbedCreateSpec.builder()
                .title("test")
                .build();

        String value = mapper.writeValueAsString(spec);
        EmbedCreateSpec read = mapper.readValue(value, EmbedCreateSpec.class);

        assertEquals("test", read.title().get());
    }

    @Test
    public void canSerializeMessageCreateSpec() throws JsonProcessingException {
        // TODO: add all fields to test
        MessageCreateSpec spec = MessageCreateSpec.builder()
                .content("test")
                .addFile("test.txt", new ByteArrayInputStream(new byte[0]))
                .build();

        String value = mapper.writeValueAsString(spec);
        MessageCreateSpec read = mapper.readValue(value, MessageCreateSpec.class);

        assertEquals("test", read.content().get());
        assertEquals(0, read.files().size());
    }

}
