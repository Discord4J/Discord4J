package discord4j.common.jackson;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.ReferenceTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

import javax.annotation.Nullable;

public class PossibleDeserializer extends ReferenceTypeDeserializer<Possible<?>> {

    PossibleDeserializer(JavaType fullType, ValueInstantiator vi, TypeDeserializer typeDeser,
                                JsonDeserializer<?> deser) {
        super(fullType, vi, typeDeser, deser);
    }

    @Override
    protected ReferenceTypeDeserializer<Possible<?>> withResolved(TypeDeserializer typeDeser,
                                                                  JsonDeserializer<?> valueDeser) {
        return new PossibleDeserializer(_fullType, _valueInstantiator, typeDeser, valueDeser);
    }

    @Nullable
    @Override
    public Possible<?> getNullValue(DeserializationContext ctxt) {
        return null;
    }

    @Override
    public Possible<?> referenceValue(Object contents) {
        return Possible.of(contents);
    }

    @Override
    public Possible<?> updateReference(Possible<?> reference, Object contents) {
        return Possible.of(contents);
    }

    @Nullable
    @Override
    public Object getReferenced(Possible<?> reference) {
        return reference.isAbsent() ? null : reference.get();
    }
}
