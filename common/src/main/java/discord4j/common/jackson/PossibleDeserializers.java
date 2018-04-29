package discord4j.common.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.ReferenceType;

public class PossibleDeserializers extends Deserializers.Base {

    @Override
    public JsonDeserializer<?> findReferenceDeserializer(ReferenceType refType, DeserializationConfig config,
                                                         BeanDescription beanDesc,
                                                         TypeDeserializer contentTypeDeserializer,
                                                         JsonDeserializer<?> contentDeserializer) {
        if (refType.hasRawClass(Possible.class)) {
            return new PossibleDeserializer(refType, null, contentTypeDeserializer, contentDeserializer);
        }

        return null;
    }
}
