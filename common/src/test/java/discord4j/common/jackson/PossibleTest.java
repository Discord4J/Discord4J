package discord4j.common.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import discord4j.common.pojo.Pojo;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class PossibleTest {
	private ObjectMapper getMapper() {
		return new ObjectMapper().registerModules(new Jdk8Module());
	}

	@org.junit.Test
	public void testAlwaysPresentAndNullableNull() throws JsonProcessingException {
		ObjectMapper mapper = getMapper();

		Pojo pojo = new Pojo();
		pojo.setAlways("Hello");
		pojo.setNullable(Optional.empty());
		pojo.setSometimes(Possible.absent());
		pojo.setSometimesAndNullable(PossibleOptional.absent());

		String serialized = mapper.writeValueAsString(pojo);
		assertEquals("{\"always\":\"Hello\",\"nullable\":null}", serialized);
	}

	@org.junit.Test
	public void testAlwaysPresentAndNullableNotNull() throws JsonProcessingException {
		ObjectMapper mapper = getMapper();

		Pojo pojo = new Pojo();
		pojo.setAlways("Hello");
		pojo.setNullable(Optional.of("World"));
		pojo.setSometimes(Possible.absent());
		pojo.setSometimesAndNullable(PossibleOptional.absent());

		String serialized = mapper.writeValueAsString(pojo);
		assertEquals("{\"always\":\"Hello\",\"nullable\":\"World\"}", serialized);
	}

	@org.junit.Test
	public void testAlwaysPresentAndNullableNotNullAndSometimesPresent() throws JsonProcessingException {
		ObjectMapper mapper = getMapper();

		Pojo pojo = new Pojo();
		pojo.setAlways("Hello");
		pojo.setNullable(Optional.of("World"));
		pojo.setSometimes(Possible.of("Foo"));
		pojo.setSometimesAndNullable(PossibleOptional.absent());

		String serialized = mapper.writeValueAsString(pojo);
		assertEquals("{\"always\":\"Hello\",\"nullable\":\"World\",\"sometimes\":\"Foo\"}", serialized);
	}

	@org.junit.Test
	public void testAllPresentAndNotNull() throws JsonProcessingException {
		ObjectMapper mapper = getMapper();

		Pojo pojo = new Pojo();
		pojo.setAlways("Hello");
		pojo.setNullable(Optional.of("World"));
		pojo.setSometimes(Possible.of("Foo"));
		pojo.setSometimesAndNullable(PossibleOptional.of("Bar"));

		String serialized = mapper.writeValueAsString(pojo);
		assertEquals("{\"always\":\"Hello\",\"nullable\":\"World\",\"sometimes\":\"Foo\",\"sometimesAndNullable\":\"Bar\"}", serialized);
	}
}
