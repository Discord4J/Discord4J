package sx.blah.discord.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a module as having a dependency on another module. This will prevent it from being
 * loaded if its dependency isn't loaded.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Requires {

	/**
	 * This is the fully qualified class name of the required module. Ex. "com.foo.Bar"
	 */
	String value();
}
