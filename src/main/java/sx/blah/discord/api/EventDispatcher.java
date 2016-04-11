package sx.blah.discord.api;

import net.jodah.typetools.TypeResolver;
import sx.blah.discord.Discord4J;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages event listeners and event logic.
 */
public class EventDispatcher {

	private ConcurrentHashMap<Class<?>, ConcurrentHashMap<Method, CopyOnWriteArrayList<Object>>> methodListeners = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<IListener>> classListeners = new ConcurrentHashMap<>();
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
		for (Method method : listener.getClass().getMethods()) {
			if (method.getParameterCount() == 1
					&& method.isAnnotationPresent(EventSubscriber.class)) {
				method.setAccessible(true);
				Class<?> eventClass = method.getParameterTypes()[0];
				if (Event.class.isAssignableFrom(eventClass)) {
					if (!methodListeners.containsKey(eventClass))
						methodListeners.put(eventClass, new ConcurrentHashMap<>());

					if (!methodListeners.get(eventClass).containsKey(method))
						methodListeners.get(eventClass).put(method, new CopyOnWriteArrayList<>());

					methodListeners.get(eventClass).get(method).add(listener);
					Discord4J.LOGGER.trace("Registered method listener {}", listener.getClass().getSimpleName(), method.toString());
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

			Discord4J.LOGGER.trace("Registered IListener {}", listener.getClass().getSimpleName());
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
						if (methodListeners.get(eventClass).containsKey(method)) {
							methodListeners.get(eventClass).get(method).remove(listener);
							Discord4J.LOGGER.trace("Unregistered method listener {}", listener.getClass().getSimpleName(), method.toString());
						}
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
			if (classListeners.containsKey(rawType)) {
				classListeners.get(rawType).remove(listener);
				Discord4J.LOGGER.trace("Unregistered IListener {}", listener.getClass().getSimpleName());
			}
		}
	}

	/**
	 * Dispatches an event.
	 *
	 * @param event The event.
	 */
	public void dispatch(Event event) {
		if (client.isReady()) {
			Discord4J.LOGGER.trace("Dispatching event of type {}", event.getClass().getSimpleName());
			event.client = client;

			methodListeners.entrySet().stream()
					.filter(e -> e.getKey().isAssignableFrom(event.getClass()))
					.map(e -> e.getValue())
					.forEach(m ->
							m.forEach((k, v) ->
									v.forEach(o -> {
										try {
											k.invoke(o, event);
										} catch (IllegalAccessException | InvocationTargetException e) {
											Discord4J.LOGGER.error("Error dispatching event "+event.getClass().getSimpleName(), e);
										} catch (Exception e) {
											Discord4J.LOGGER.error("Unhandled exception caught dispatching event "+event.getClass().getSimpleName(), e);
										}
									})));

			classListeners.entrySet().stream()
					.filter(e -> e.getKey().isAssignableFrom(event.getClass()))
					.map(e -> e.getValue())
					.forEach(s -> s.forEach(l -> {
						try {
							l.handle(event);
						} catch (Exception e) {
							Discord4J.LOGGER.error("Unhandled exception caught dispatching event "+event.getClass().getSimpleName(), e);
						}
					}));
		}
	}
}
