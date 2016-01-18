package sx.blah.discord.handle;

import net.jodah.typetools.TypeResolver;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages event listeners and event logic.
 */
public class EventDispatcher {
	
	private ConcurrentHashMap<Class<?>, HashMap<Method, Object>> methodListeners = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Class<?>, List<IListener>> classListeners = new ConcurrentHashMap<>();
	private IDiscordClient client;
	
	public EventDispatcher(IDiscordClient client) {
		this.client = client;
	}
	
	/**
	 * Registers a listener using {@link EventSubscriber} method annotations.
	 *
	 * @param listener The listener.
	 */
	public void registerListener(Object listener) {
		for (Method method : listener.getClass().getDeclaredMethods()) {
			if (method.getParameterCount() == 1) {
				Class<?> eventClass = method.getParameterTypes()[0];
				if (Event.class.isAssignableFrom(eventClass)) {
					if (!methodListeners.containsKey(eventClass))
						methodListeners.put(eventClass, new HashMap<>());
					
					methodListeners.get(eventClass).put(method, listener);
				}
			}
		}
	}
	
	/**
	 * Registers a single event listener.
	 *
	 * @param listener The listener.
	 */
	public void registerListener(IListener listener) {
		Class<?> rawType = TypeResolver.resolveRawArgument(IListener.class, listener.getClass());
		if (Event.class.isAssignableFrom(rawType)) {
			if (!classListeners.containsKey(rawType))
				classListeners.put(rawType, new CopyOnWriteArrayList<>());
			
			classListeners.get(rawType).add(listener);
		}
	}
	
	/**
	 * Unregisters a listener using {@link EventSubscriber} method annotations.
	 *
	 * @param listener The listener.
	 */
	public void unregisterListener(Object listener) {
		for (Method method : listener.getClass().getDeclaredMethods()) {
			if (method.getParameterCount() == 1) {
				Class<?> eventClass = method.getParameterTypes()[0];
				if (Event.class.isAssignableFrom(eventClass)) {
					if (methodListeners.containsKey(eventClass))
						methodListeners.get(eventClass).remove(method);
				}
			}
		}
	}
	
	/**
	 * Unregisters a single event listener.
	 *
	 * @param listener The listener.
	 */
	public void unregisterListener(IListener listener) {
		Class<?> rawType = TypeResolver.resolveRawArgument(IListener.class, listener.getClass());
		if (Event.class.isAssignableFrom(rawType)) {
			if (classListeners.containsKey(rawType))
				classListeners.get(rawType).remove(listener);
		}
	}
	
	/**
	 * Dispatches an event.
	 *
	 * @param event The event.
	 */
	public void dispatch(Event event) {
		if (client.isReady()) {
			Discord4J.LOGGER.debug("Dispatching event of type {}", event.getClass().getSimpleName());
			event.client = client;
			if (methodListeners.containsKey(event.getClass())) {
				HashMap<Method, Object> methodListeners = this.methodListeners.get(event.getClass());
				for (Method method : methodListeners.keySet())
					try {
						method.invoke(methodListeners.get(method), event);
					} catch (IllegalAccessException | InvocationTargetException e) {
						Discord4J.LOGGER.error("Error dispatching event "+event.getClass().getSimpleName(), e);
					}
			}
			
			if (classListeners.containsKey(event.getClass())) {
				for (IListener listener : classListeners.get(event.getClass()))
					listener.handle(event);
			}
		}
	}
}
