package discord4j.common.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.json.Pojo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PossibleTest {

	@Test
	public void testPresent() throws Exception {
		ObjectMapper mapper = getMapper();
		String expected = "{\"string\":\"Hello world\"}";

		Pojo pojo = new Pojo(Possible.of("Hello world"));
		String result = mapper.writeValueAsString(pojo);

		assertEquals(expected, result);
	}

	@Test
	public void testAbsent() throws Exception {
		ObjectMapper mapper = getMapper();
		String expected = "{}";

		Pojo pojo = new Pojo(Possible.absent());
		String result = mapper.writeValueAsString(pojo);

		assertEquals(expected, result);
	}

	@Test
	public void testNull() throws Exception {
		ObjectMapper mapper = getMapper();
		String expected = "{\"string\":null}";

		Pojo pojo = new Pojo(null);
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
