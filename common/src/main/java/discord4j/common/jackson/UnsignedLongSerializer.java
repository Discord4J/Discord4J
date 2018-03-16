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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class UnsignedLongSerializer extends StdSerializer<Object> { // <Long | PossibleLong | long[] | Possible<long[]>>

    public UnsignedLongSerializer() {
        super(Object.class);
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value instanceof Long) {
            gen.writeString(Long.toUnsignedString((Long) value));
        } else if (value instanceof PossibleLong) {
            PossibleLong pl = (PossibleLong) value;
            if (pl.isAbsent()) {
                provider.defaultSerializeNull(gen);
            } else {
                gen.writeString(Long.toUnsignedString(pl.get()));
            }
        } else if (value instanceof long[]) {
            writeArray((long[]) value, gen);
        } else if (value instanceof Possible) {
            Possible<?> p = (Possible<?>) value;
            if (p.isAbsent()) {
                provider.defaultSerializeNull(gen);
            } else {
                Object o = p.get();
                if (o instanceof long[]) {
                    writeArray((long[]) o, gen);
                }
            }
        } else {
            throw new IllegalStateException("Attempt to serialize field marked with @UnsignedJson which is not of " +
                    "type Long | PossibleLong | long[] | Possible<long[]>");
        }
    }

    private static void writeArray(long[] ary, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        for (long l : ary) {
            gen.writeString(Long.toUnsignedString(l));
        }
        gen.writeEndArray();
    }
}
