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

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.InheritanceUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.tuple.Pair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.util.LogMarkers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This is a central registry for components. Use this if you need components in a dynamic setting (i.e.
 * the {@link ComponentInjection} annotation cannot be used for a particular use case or you are not using the module
 * system in the first place).
 */
public class ComponentRegistry {
	
	private final Map<Class<? extends IComponent>, IComponentProvider<? extends IComponent>> providerMap = new ConcurrentHashMap<>();
	
	private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
	
	public ComponentRegistry() {}
	
	/**
	 * This registers an {@link IComponentProvider} with the component registry.
	 *
	 * @param provider The provider to register.
	 */
	public void registerComponentProvider(IComponentProvider<?> provider) {
		if (providerMap.containsKey(provider.getComponentClass())) {
			throw new IllegalStateException("Cannot register provider for class " + provider.getComponentClass() + ", the provider for it already exists!");
		}
		
		providerMap.put(provider.getComponentClass(), provider);
	}
	
	/**
	 * Gets all the registered component providers.
	 *
	 * @return The component providers.
	 */
	public Collection<IComponentProvider<? extends IComponent>> getComponentProviders() {
		return providerMap.values();
	}
	
	/**
	 * Gets all the classes representing possible components which may be injected.
	 *
	 * @return The possible component classes.
	 */
	public Collection<Class<? extends IComponent>> getComponentClasses() {
		return providerMap.keySet();
	}
	
	/**
	 * This performs automatic dependency injection into an object. This means that the object must contain
	 * {@link ComponentInjection} annotations in order for this to have any effect.
	 *
	 * @param obj The object to perform component injection into.
	 */
	public void injectInto(Object obj) {
		new Injector(obj).inject();
	}
	
	/**
	 * This creates an {@link Injector} instance for the provided object.
	 *
	 * @param obj The object to perform injection on.
	 * @return The injector.
	 */
	public Injector injectorFor(Object obj) {
		return new Injector(obj);
	}
	
	/**
	 * This class is used to perform component injection in an object.
	 */
	public class Injector {
		
		private final Object parent;
		
		private Injector(Object parent) {
			this.parent = parent;
		}
		
		/**
		 * Performs automated component injection into the object.
		 *
		 * @see #injectInto(Object)
		 */
		public void inject() {
			//Inject into fields
			FieldUtils.getFieldsListWithAnnotation(parent.getClass(), ComponentInjection.class)
					.stream()
					.peek(f -> f.setAccessible(true))
					.peek(FieldUtils::removeFinalModifier)
					.forEach(field -> {
						try {
							field.set(parent, createComponent(getComponentForAnnotation(field.getAnnotation(ComponentInjection.class), field)));
						} catch (IllegalAccessException e) {
							Discord4J.LOGGER.error(LogMarkers.UTIL, "Could not inject into field!", e);
						}
					});
			
			//Inject into method annotations
			MethodUtils.getMethodsListWithAnnotation(parent.getClass(), ComponentInjection.class)
					.stream()
					.peek(MethodUtils::getAccessibleMethod)
					.map(m -> {
						try {
							return Pair.of(m, lookup.unreflect(m));
						} catch (IllegalAccessException e) {
							Discord4J.LOGGER.error(LogMarkers.UTIL, "Could not inject into method!", e);
							return null;
						}
					})
					.filter(Objects::nonNull)
					.map(p -> Pair.of(p.getLeft(), p.getRight().bindTo(parent)))
					.forEach(p -> {
						Method method = p.getLeft();
						MethodHandle handle = p.getRight();
						MethodType methodType = handle.type();
						if (method.getParameterCount() == 1) {
							List<IComponent> params = getParamsOrEmpty(method, methodType);
							if (!params.isEmpty()) {
								try {
									handle.invokeWithArguments(params);
								} catch (Throwable e) {
									Discord4J.LOGGER.error(LogMarkers.UTIL, "Could not inject into method!", e);
								}
							}
						} else {
							Discord4J.LOGGER.error(LogMarkers.UTIL, "Method with signature {} must have exactly one acceptable parameter!", methodType.toString());
						}
					});
			
			//Inject into method parameter annotations
			Arrays.stream(parent.getClass().getMethods())
					.filter(m -> !m.isAnnotationPresent(ComponentInjection.class))
					.peek(MethodUtils::getAccessibleMethod)
					.map(m -> {
						try {
							return Pair.of(m, lookup.unreflect(m));
						} catch (IllegalAccessException e) {
							Discord4J.LOGGER.error(LogMarkers.UTIL, "Could not inject into method!", e);
							return null;
						}
					})
					.filter(Objects::nonNull)
					.map(p -> Pair.of(p.getLeft(), p.getRight().bindTo(parent)))
					.forEach(p -> {
						Method method = p.getLeft();
						MethodHandle handle = p.getRight();
						MethodType methodType = handle.type();
						List<IComponent> params = getParamsOrEmpty(method, methodType);
						if (!params.isEmpty()) {
							try {
								handle.invokeWithArguments(params);
							} catch (Throwable e) {
								Discord4J.LOGGER.error(LogMarkers.UTIL, "Could not inject into method!", e);
							}
						}
					});
		}
		
		private Class<?> getComponentForAnnotation(ComponentInjection annotation) throws ClassNotFoundException {
			return annotation.value().isEmpty() ? null : Class.forName(annotation.value());
		}
		
		private Class<? extends IComponent> getComponentForAnnotation(ComponentInjection annotation, Method method) {
			return getComponentForAnnotation(annotation, method.getParameters()[0]);
		}
		
		private Class<? extends IComponent> getComponentForAnnotation(ComponentInjection annotation, Field field) {
			try {
				Class<?> componentClass = getComponentForAnnotation(annotation);
				if (componentClass != null)
					return (Class<? extends IComponent>) componentClass;
				
				Class<?> fieldType = field.getType();
				if (IComponent.class.isAssignableFrom(fieldType)) {
					return (Class<? extends IComponent>) fieldType;
				} else {
					return null;
				}
			} catch (Exception e) {
				return null;
			}
		}
		
		private Class<? extends IComponent> getComponentForAnnotation(ComponentInjection annotation, Parameter param) {
			try {
				Class<?> componentClass = getComponentForAnnotation(annotation);
				if (componentClass != null)
					return (Class<? extends IComponent>) componentClass;
				
				Class<?> paramType = param.getType();
				if (IComponent.class.isAssignableFrom(paramType)) {
					return (Class<? extends IComponent>) paramType;
				} else {
					return null;
				}
			} catch (Exception e) {
				return null;
			}
		}
		
		private List<IComponent> getParamsOrEmpty(Method method, MethodType type) {
			if (type.parameterCount() == 0)
				return Collections.emptyList();
			
			try {
				if (type.parameterCount() == 1 && method.isAnnotationPresent(ComponentInjection.class)) {
					return Collections.singletonList(createComponent(getComponentForAnnotation(method.getAnnotation(ComponentInjection.class), method)));
				} else {
					List<IComponent> params = new LinkedList<>();
					for (Parameter p : method.getParameters()) {
						if (!p.isAnnotationPresent(ComponentInjection.class))
							return Collections.emptyList();
						
						params.add(createComponent(getComponentForAnnotation(p.getAnnotation(ComponentInjection.class), p)));
					}
					
					return params;
				}
			} catch (Throwable t) {
				return Collections.emptyList();
			}
		}
		
		/**
		 * Creates a component for the object wrapped object.
		 *
		 * @param componentType The component to generate.
		 * @return The component, or null if none could be provided.
		 */
		public <T extends IComponent> T createComponent(Class<T> componentType) {
			IComponentProvider provider = null;
			
			if (componentType != null) {
				Set<Class<? extends IComponent>> types = providerMap.keySet()
						.stream()
						.filter(componentType::isAssignableFrom)
						.collect(Collectors.toSet());
				
				if (types.size() == 1) {
					provider = providerMap.get(types.stream().findFirst().get());
				} else if (types.size() > 1) {
					for (Class<? extends IComponent> type : types) { //Prioritize the component type which exactly matches the request if there is any
						if (type.equals(componentType)) {
							provider = providerMap.get(componentType);
							break;
						}
					}
					
					if (provider == null) {
						Class<? extends IComponent> type = types.stream()
								.sorted(Comparator.comparingInt(o -> InheritanceUtils.distance(o, componentType)))
								.findFirst()
								.get();
						provider = providerMap.get(type);
					}
				}
			}
			
			if (provider == null)
				return null;
			
			return (T) provider.provideFor(parent);
		}
	}
}
