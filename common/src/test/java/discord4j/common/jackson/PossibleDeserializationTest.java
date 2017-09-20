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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import discord4j.common.entity.Pojo;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class PossibleDeserializationTest {

	private ObjectMapper getMapper() {
		return new ObjectMapper().registerModules(new Jdk8Module(), new PossibleModule());
	}

	@Test
	public void testAlwaysPresentAndNullableNull() throws IOException {
		ObjectMapper mapper = getMapper();

		String serialized = "{\"always\":\"Hello\",\"nullable\":null}";

		Pojo pojo = new Pojo();
		pojo.setAlways("Hello");
		pojo.setNullable(Optional.empty());
		pojo.setSometimes(Possible.absent());
		pojo.setSometimesAndNullable(PossibleOptional.absent());

		Pojo deserialized = mapper.readValue(serialized, Pojo.class);
		assertEquals(pojo, deserialized);
	}

	@Test
	public void testAlwaysPresentAndNullableNotNull() throws IOException {
		ObjectMapper mapper = getMapper();

		String serialized = "{\"always\":\"Hello\",\"nullable\":\"World\"}";

		Pojo pojo = new Pojo();
		pojo.setAlways("Hello");
		pojo.setNullable(Optional.of("World"));
		pojo.setSometimes(Possible.absent());
		pojo.setSometimesAndNullable(PossibleOptional.absent());

		Pojo deserialized = mapper.readValue(serialized, Pojo.class);
		assertEquals(pojo, deserialized);
	}

	@Test
	public void testAlwaysPresentAndNullableNotNullAndSometimesPresent() throws IOException {
		ObjectMapper mapper = getMapper();

		String serialized = "{\"always\":\"Hello\",\"nullable\":\"World\",\"sometimes\":\"Foo\"}";

		Pojo pojo = new Pojo();
		pojo.setAlways("Hello");
		pojo.setNullable(Optional.of("World"));
		pojo.setSometimes(Possible.of("Foo"));
		pojo.setSometimesAndNullable(PossibleOptional.absent());

		Pojo deserialized = mapper.readValue(serialized, Pojo.class);
		assertEquals(pojo, deserialized);
	}

	@Test
	public void testAllPresentAndNotNull() throws IOException {
		ObjectMapper mapper = getMapper();

		String serialized = "{\"always\":\"Hello\",\"nullable\":\"World\",\"sometimes\":\"Foo\",\"sometimesAndNullable\":\"Bar\"}";

		Pojo pojo = new Pojo();
		pojo.setAlways("Hello");
		pojo.setNullable(Optional.of("World"));
		pojo.setSometimes(Possible.of("Foo"));
		pojo.setSometimesAndNullable(PossibleOptional.of("Bar"));

		Pojo deserialized = mapper.readValue(serialized, Pojo.class);
		assertEquals(pojo, deserialized);
	}

	@Test
	public void testPossibleOptionalNull() throws IOException {
		ObjectMapper mapper = getMapper();

		String serialized = "{\"always\":\"Hello\",\"nullable\":\"World\",\"sometimes\":\"Foo\",\"sometimesAndNullable\":null}";

		Pojo pojo = new Pojo();
		pojo.setAlways("Hello");
		pojo.setNullable(Optional.of("World"));
		pojo.setSometimes(Possible.of("Foo"));
		pojo.setSometimesAndNullable(PossibleOptional.empty());

		Pojo deserialized = mapper.readValue(serialized, Pojo.class);
		assertEquals(pojo, deserialized);
	}

}
