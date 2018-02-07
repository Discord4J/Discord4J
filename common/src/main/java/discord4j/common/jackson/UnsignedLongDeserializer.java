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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class UnsignedLongDeserializer extends StdDeserializer<Object> implements ContextualDeserializer { // <Long | long[]>

	public UnsignedLongDeserializer() {
		super(Object.class);
	}

	private UnsignedLongDeserializer(JavaType type) {
		super(type);
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
		return new UnsignedLongDeserializer(property.getType());
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		Class<?> type = handledType();
		if (type.equals(long.class) || type.equals(Long.class)) {
			return Long.parseUnsignedLong(p.getValueAsString());
		} else if (type.equals(long[].class)) {
			String[] ary = p.readValueAs(String[].class);
			long[] ret = new long[ary.length];
			for (int i = 0; i < ary.length; i++) {
				ret[i] = Long.parseUnsignedLong(ary[i]);
			}
			return ret;
		}

		throw new IllegalStateException("Attempt to deserialize field marked with @UnsignedJson which is not of type Long | long[]: " + type.getSimpleName());
	}
}
