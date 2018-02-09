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

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ReferenceTypeSerializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.util.NameTransformer;

import javax.annotation.Nullable;

public class PossibleLongSerializer extends ReferenceTypeSerializer<PossibleLong> {

	PossibleLongSerializer(ReferenceType fullType, boolean staticTyping, TypeSerializer vts, JsonSerializer<Object> ser) {
		super(fullType, staticTyping, vts, ser);
	}

	private PossibleLongSerializer(PossibleLongSerializer base, BeanProperty property, TypeSerializer vts,
	                           JsonSerializer<?> valueSer, NameTransformer unwrapper, Object suppressableValue,
	                           boolean suppressNulls) {
		super(base, property, vts, valueSer, unwrapper, suppressableValue, suppressNulls);

	}

	@Override
	protected ReferenceTypeSerializer<PossibleLong> withResolved(BeanProperty prop, TypeSerializer vts,
	                                                            JsonSerializer<?> value, NameTransformer unwrapper) {
		return new PossibleLongSerializer(this, prop, vts, value, unwrapper, _suppressableValue, _suppressNulls);
	}

	@Override
	public ReferenceTypeSerializer<PossibleLong> withContentInclusion(Object suppressableValue, boolean suppressNulls) {
		return new PossibleLongSerializer(this, _property, _valueTypeSerializer, _valueSerializer, _unwrapper,
				suppressableValue, suppressNulls);
	}

	@Override
	protected boolean _isValuePresent(PossibleLong value) {
		return !value.isAbsent();
	}

	@Override
	protected Object _getReferenced(PossibleLong value) {
		return value.get();
	}

	@Nullable
	@Override
	protected Object _getReferencedIfPresent(PossibleLong value) {
		return value.isAbsent() ? null : value.get();
	}
}
