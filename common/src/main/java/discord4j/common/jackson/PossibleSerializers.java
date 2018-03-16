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
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.ReferenceType;

import javax.annotation.Nullable;

public class PossibleSerializers extends Serializers.Base {

    @Nullable
    @Override
    public JsonSerializer<?> findReferenceSerializer(SerializationConfig config, ReferenceType type,
                                                     BeanDescription beanDesc, TypeSerializer contentTypeSerializer,
                                                     JsonSerializer<Object> contentValueSerializer) {
        Class<?> raw = type.getRawClass();
        boolean staticTyping = config.isEnabled(MapperFeature.USE_STATIC_TYPING);

        if (Possible.class.isAssignableFrom(raw)) {
            return new PossibleSerializer(type, staticTyping, contentTypeSerializer, contentValueSerializer);
        } else if (PossibleLong.class.isAssignableFrom(raw)) {
            return new PossibleLongSerializer(type, staticTyping, contentTypeSerializer, contentValueSerializer);
        }

        return null;
    }
}
