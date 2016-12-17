package sx.blah.discord.api.events;

import net.jodah.typetools.TypeResolver;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.handle.impl.events.DisconnectedEvent;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.Procedure;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * Manages event listeners and event logic.
 */
public class EventDispatcher {

	private final ConcurrentHashMap<Class<?>, ConcurrentHashMap<Method, CopyOnWriteArrayList<ListenerPair<Object>>>> methodListeners = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<ListenerPair<IListener>>> classListeners = new ConcurrentHashMap<>();
	private final ExecutorService eventExecutor = Executors.newCachedThreadPool(runnable -> { //Ensures all threads are daemons
		Thread thread = Executors.defaultThreadFactory().newThread(runnable);
		thread.setName("Event Dispatch Thread");
		thread.setDaemon(true);
		return thread;
	});
	private final IDiscordClient client;

	public EventDispatcher(IDiscordClient client) {
		this.client = client;
	}

	/**
	 * Registers a listener using {@link EventSubscriber} method annotations.
	 *
	 * @param listener The listener.
	 */
	public void registerListener(Object listener) {
		registerListener(listener.getClass(), listener, false);
	}

	/**
	 * Registers a listener using {@link EventSubscriber} method annotations.
	 *
	 * @param listener The listener.
	 */
	public void registerListener(Class<?> listener) {
		registerListener(listener, null, false);
	}

	/**
	 * Registers a single event listener.
	 *
	 * @param listener The listener.
	 */
	public void registerListener(IListener listener) {
		registerListener(listener, false);
	}

	private void registerListener(Class<?> listenerClass, Object listener, boolean isTemporary) {
		if (IListener.class.isAssignableFrom(listenerClass)) {
			Discord4J.LOGGER.warn(LogMarkers.EVENTS, "IListener was attempted to be registered as an annotation listener. The listener in question will now be registered as an IListener.");
			registerListener((IListener) listener, isTemporary);
			return;
		}

		for (Method method : listenerClass.getMethods()) {
			if (method.getParameterCount() == 1
					&& method.isAnnotationPresent(EventSubscriber.class)) {
				if ((Modifier.isStatic(method.getModifiers()) && listener == null) || listener != null) {
					method.setAccessible(true);
					Class<?> eventClass = method.getParameterTypes()[0];
					if (Event.class.isAssignableFrom(eventClass)) {
						if (!methodListeners.containsKey(eventClass))
							methodListeners.put(eventClass, new ConcurrentHashMap<>());

						if (!methodListeners.get(eventClass).containsKey(method))
							methodListeners.get(eventClass).put(method, new CopyOnWriteArrayList<>());

						methodListeners.get(eventClass).get(method).add(new ListenerPair<>(isTemporary, listener));
						Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Registered method listener {}#{}", listenerClass.getSimpleName(), method.getName());
					}
				}
			}
		}
	}

	private <T extends Event> void registerListener(IListener<T> listener, boolean isTemporary) {
		Class<?> rawType = TypeResolver.resolveRawArgument(IListener.class, listener.getClass());
		if (Event.class.isAssignableFrom(rawType)) {
			if (!classListeners.containsKey(rawType))
				classListeners.put(rawType, new CopyOnWriteArrayList<>());

			Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Registered IListener {}", listener.getClass().getSimpleName());
			classListeners.get(rawType).add(new ListenerPair<>(isTemporary, listener));
		}
	}

	/**
	 * This registers a temporary event listener using {@link EventSubscriber} method annotations.
	 * Meaning that when it listens to an event, it immediately unregisters itself.
	 *
	 * @param listener The listener.
	 */
	public void registerTemporaryListener(Object listener) {
		registerListener(listener.getClass(), listener, true);
	}

	/**
	 * This registers a temporary event listener using {@link EventSubscriber} method annotations.
	 * Meaning that when it listens to an event, it immediately unregisters itself.
	 *
	 * @param listener The listener.
	 */
	public void registerTemporaryListener(Class<?> listener) {
		registerListener(listener, null, true);
	}

	/**
	 * This registers a temporary single event listener.
	 * Meaning that when it listens to an event, it immediately unregisters itself.
	 *
	 * @param listener The listener.
	 */
	public <T extends Event> void registerTemporaryListener(IListener<T> listener) {
		registerListener(listener, true);
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched.
	 *
	 * @param eventClass The class of the event to wait for.
	 * @param <T> The event type to wait for.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> void waitFor(Class<T> eventClass) throws InterruptedException {
		waitFor((T event) -> true);
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched.
	 *
	 * @param eventClass The class of the event to wait for.
	 * @param time The timeout, in milliseconds. After this amount of time is reached, the thread is notified regardless
	 * of whether the event fired.
	 * @param <T> The event type to wait for.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> void waitFor(Class<T> eventClass, long time) throws InterruptedException {
		waitFor((T event) -> true, time);
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched.
	 *
	 * @param eventClass The class of the event to wait for.
	 * @param time The timeout. After this amount of time is reached, the thread is notified regardless of whether the
	 * event fired.
	 * @param unit The unit for the time parameter.
	 * @param <T> The event type to wait for.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> void waitFor(Class<T> eventClass, long time, TimeUnit unit) throws InterruptedException {
		waitFor((T event) -> true, time, unit, () -> {});
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched.
	 *
	 * @param eventClass The class of the event to wait for.
	 * @param time The timeout. After this amount of time is reached, the thread is notified regardless of whether the
	 * event fired.
	 * @param unit The unit for the time parameter.
	 * @param onTimeout The procedure to execute when the timeout is reached.
	 * @param <T> The event type to wait for.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> void waitFor(Class<T> eventClass, long time, TimeUnit unit, Procedure onTimeout) throws InterruptedException {
		waitFor((T event) -> true, time, unit, onTimeout);
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched and the provided
	 * {@link Predicate} returns true.
	 *
	 * @param filter This is called to determine whether the thread should be resumed as a result of this event.
	 * @param <T> The event type to wait for.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> void waitFor(Predicate<T> filter) throws InterruptedException {
		waitFor(filter, 0);
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched and the provided
	 * {@link Predicate} returns true.
	 *
	 * @param filter This is called to determine whether the thread should be resumed as a result of this event.
	 * @param time The timeout, in milliseconds. After this amount of time is reached, the thread is notified regardless
	 * of whether the event fired.
	 * @param <T> The event type to wait for.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> void waitFor(Predicate<T> filter, long time) throws InterruptedException {
		waitFor(filter, time, TimeUnit.MILLISECONDS, () -> {});
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched and the provided
	 * {@link Predicate} returns true.
	 *
	 * @param filter This is called to determine whether the thread should be resumed as a result of this event.
	 * @param time The timeout, in milliseconds. After this amount of time is reached, the thread is notified regardless
	 * of whether the event fired.
	 * @param unit The unit for the time parameter.
	 * @param <T> The event type to wait for.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> void waitFor(Predicate<T> filter, long time, TimeUnit unit) throws InterruptedException {
		waitFor(filter, time, unit, () -> {});
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched and the provided
	 * {@link Predicate} returns true.
	 *
	 * @param filter This is called to determine whether the thread should be resumed as a result of this event.
	 * @param time The timeout. After this amount of time is reached, the thread is notified regardless of whether the
	 * event fired.
	 * @param unit The unit for the time parameter.
	 * @param onTimeout The procedure to execute when the timeout is reached.
	 * @param <T> The event type to wait for.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> void waitFor(Predicate<T> filter, long time, TimeUnit unit, Procedure onTimeout) throws InterruptedException {
		final Thread currentThread = Thread.currentThread();
		final AtomicBoolean timedOut = new AtomicBoolean(true);

		synchronized (currentThread) {
			registerListener(new IListener<T>() {
				@Override
				public void handle(T event) {
					if (filter.test(event)) {
						client.getDispatcher().unregisterListener(this);
						synchronized (currentThread) {
							timedOut.set(false);
							currentThread.notify();
						}
					}
				}
			});
			currentThread.wait(unit.toMillis(time));
			if (timedOut.get()) onTimeout.invoke();
		}
	}

	/**
	 * Unregisters a listener using {@link EventSubscriber} method annotations.
	 *
	 * @param listener The listener.
	 */
	public void unregisterListener(Object listener) {
		for (Method method : listener.getClass().getMethods()) {
			if (method.getParameterCount() == 1) {
				Class<?> eventClass = method.getParameterTypes()[0];
				if (Event.class.isAssignableFrom(eventClass)) {
					if (methodListeners.containsKey(eventClass))
						if (methodListeners.get(eventClass).containsKey(method)) {
							methodListeners.get(eventClass).get(method).removeIf((ListenerPair pair) -> pair.listener == listener); //Yes, the == is intentional. We want the exact same instance.
							Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Unregistered method listener {}", listener.getClass().getSimpleName(), method.toString());
						}
				}
			}
		}
	}

	/**
	 * Unregisters a listener using {@link EventSubscriber} method annotations.
	 *
	 * @param clazz The listener class with static methods.
	 */
	public void unregisterListener(Class<?> clazz) {
		for (Method method : clazz.getMethods()) {
			if (method.getParameterCount() == 1) {
				Class<?> eventClass = method.getParameterTypes()[0];
				if (Event.class.isAssignableFrom(eventClass)) {
					if (methodListeners.containsKey(eventClass))
						if (methodListeners.get(eventClass).containsKey(method)) {
							methodListeners.get(eventClass).get(method).removeIf((ListenerPair pair) -> pair.listener == null); // null for static listener
							Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Unregistered class method listener {}", clazz.getSimpleName(), method.toString());
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
				classListeners.get(rawType).removeIf((ListenerPair pair) -> pair.listener == listener); //Yes, the == is intentional. We want the exact same instance.
				Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Unregistered IListener {}", listener.getClass().getSimpleName());
			}
		}
	}

	/**
	 * Dispatches an event.
	 *
	 * @param event The event.
	 */
	public synchronized void dispatch(Event event) {
		if (client.getShards().stream().anyMatch(IShard::isLoggedIn) || event instanceof DisconnectedEvent) {
			eventExecutor.submit(() -> {
				Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Dispatching event of type {}", event.getClass().getSimpleName());
				event.client = client;

				methodListeners.entrySet().stream()
						.filter(e -> e.getKey().isAssignableFrom(event.getClass()))
						.map(Map.Entry::getValue)
						.forEach(m ->
								m.forEach((k, v) ->
										v.forEach(o -> {
											try {
												k.invoke(o.listener, event);
												if (o.isTemporary)
													unregisterListener(o.listener);
											} catch (IllegalAccessException e) {
												Discord4J.LOGGER.error(LogMarkers.EVENTS, "Error dispatching event " + event.getClass().getSimpleName(), e);
											} catch(InvocationTargetException e) {
												Discord4J.LOGGER.error(LogMarkers.EVENTS, "Unhandled exception caught dispatching event "+event.getClass().getSimpleName(), e.getCause());
											} catch (Exception e) {
												Discord4J.LOGGER.error(LogMarkers.EVENTS, "Unhandled exception caught dispatching event "+event.getClass().getSimpleName(), e);
											}
										})));

				classListeners.entrySet().stream()
						.filter(e -> e.getKey().isAssignableFrom(event.getClass()))
						.map(Map.Entry::getValue)
						.forEach(s -> s.forEach(l -> {
							try {
								l.listener.handle(event);

								if (l.isTemporary)
									unregisterListener(l.listener);
							} catch (ClassCastException e) {
								//FIXME: This occurs when a lambda expression is used to create an IListener leading it to be registered under the type 'Event'. This is due to a bug in TypeTools: https://github.com/jhalterman/typetools/issues/14
						 	} catch (Exception e) {
								Discord4J.LOGGER.error(LogMarkers.EVENTS, "Unhandled exception caught dispatching event "+event.getClass().getSimpleName(), e);
							}
						}));
			});
		}
	}

	/**
	 * This is used to differentiate temporary event listeners from permanent ones.
	 *
	 * @param <V> The type of listener, either {@link Object} or {@link IListener}
	 */
	private static class ListenerPair<V> {

		/**
		 * Whether the listener is temporary.
		 * True if a temporary listener, false if otherwise.
		 */
		final boolean isTemporary;
		/**
		 * The actual listener object instance.
		 */
		final V listener;

		private ListenerPair(boolean isTemporary, V listener) {
			this.isTemporary = isTemporary;
			this.listener = listener;
		}
	}
}
