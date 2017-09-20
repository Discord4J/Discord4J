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

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.ReferenceTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

public class PossibleOptionalDeserializer extends ReferenceTypeDeserializer<PossibleOptional<?>> {

	public PossibleOptionalDeserializer(JavaType fullType, ValueInstantiator inst, TypeDeserializer typeDeser,
	                                    JsonDeserializer<?> deser) {
		super(fullType, inst, typeDeser, deser);
	}

	@Override
	protected ReferenceTypeDeserializer<PossibleOptional<?>> withResolved(TypeDeserializer typeDeser,
	                                                                      JsonDeserializer<?> valueDeser) {
		return new PossibleOptionalDeserializer(_fullType, _valueInstantiator, typeDeser, valueDeser);
	}

	@Override
	public PossibleOptional<?> getNullValue(DeserializationContext ctxt) {
		return PossibleOptional.empty();
	}

	@Override
	public PossibleOptional<?> referenceValue(Object contents) {
		return PossibleOptional.of(contents);
	}

	@Override
	public PossibleOptional<?> updateReference(PossibleOptional<?> reference, Object contents) {
		return PossibleOptional.of(contents);
	}

	@Override
	public Object getReferenced(PossibleOptional<?> reference) {
		return reference.isPresent() ? reference.get() : null;
	}
}
