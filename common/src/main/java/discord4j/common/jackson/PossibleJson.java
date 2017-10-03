package discord4j.common.jackson;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@JacksonAnnotationsInside
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = PossibleFilter.class)
public @interface PossibleJson {
}
