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

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.ReferenceType;

public class PossibleDeserializers extends Deserializers.Base {

	@Override
	public JsonDeserializer<?> findReferenceDeserializer(ReferenceType type, DeserializationConfig config,
	                                                     BeanDescription beanDesc, TypeDeserializer typeDeserializer,
	                                                     JsonDeserializer<?> contentDesr) throws JsonMappingException {
		if (type.hasRawClass(Possible.class)) {
			return new PossibleDeserializer(type, null, typeDeserializer, contentDesr);
		}
		if (type.hasRawClass(PossibleOptional.class)) {
			return new PossibleOptionalDeserializer(type, null, typeDeserializer, contentDesr);
		}

		return null;
	}
}
