/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.util.components;

import java.lang.annotation.*;

/**
 * This annotation is used to inject components directly into an {@link sx.blah.discord.modules.IModule} object. This
 * can either be put on a field or a method which accepts only Component objects or a method parameters which
 * accept component objects (all other parameters must also have this annotation in this case as well).
 *
 * Passing a fully-qualified classname will override component type inference. This can be used to prevent hardcoding
 * imports of completely optional integrations.
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentInjection {
	
	/**
	 * The component to inject if it exists.
	 *
	 * @return The fully-qualified component class name (ex. "com.test.MyComponent").
	 */
	String value() default "";
}
