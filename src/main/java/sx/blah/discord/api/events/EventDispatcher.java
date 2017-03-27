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
package sx.blah.discord.api.events;

import java.lang.invoke.MethodHandle;
import net.jodah.typetools.TypeResolver;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.Procedure;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages event listeners and event logic.
 */
public class EventDispatcher {

	private final MethodHandles.Lookup lookup = MethodHandles.lookup();
	private final AtomicReference<HashSet<EventHandler>> listenersRegistry = new AtomicReference<>(new HashSet<>());
	private final ExecutorService eventExecutor = Executors.newCachedThreadPool(DiscordUtils.createDaemonThreadFactory("Event Dispatcher Handler"));
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
		if (listener instanceof IListener)
			registerListener((IListener) listener);
		else
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

	/**
	 * Registers a set of listeners using {@link EventSubscriber} method annotations.
	 *
	 * @param listeners The listeners.
	 */
	public void registerListeners(Object... listeners) {
		Arrays.stream(listeners).forEach(this::registerListener);
	}

	/**
	 * Registers a set of listeners using {@link EventSubscriber} method annotations.
	 *
	 * @param listeners The listeners.
	 */
	public void registerListeners(Class<?>... listeners) {
		Arrays.stream(listeners).forEach(this::registerListener);
	}

	/**
	 * Registers a set of single event listeners.
	 *
	 * @param listeners The listeners.
	 */
	public void registerListeners(IListener... listeners) {
		Arrays.stream(listeners).forEach(this::registerListener);
	}

	private void registerListener(Class<?> listenerClass, Object listener, boolean isTemporary) {
		if (IListener.class.isAssignableFrom(listenerClass)) {
			Discord4J.LOGGER.warn(LogMarkers.EVENTS, "IListener was attempted to be registered as an annotation listener. The listener in question will now be registered as an IListener.");
			registerListener((IListener) listener, isTemporary);
			return;
		}

		Stream<Method> eventSubscriberMethods = Arrays.asList(listenerClass.getMethods()).stream().filter(m -> m.isAnnotationPresent(EventSubscriber.class));
		if (listener == null)
			eventSubscriberMethods = eventSubscriberMethods.filter(m -> Modifier.isStatic(m.getModifiers()));
		else
			eventSubscriberMethods = eventSubscriberMethods.filter(m -> !Modifier.isStatic(m.getModifiers()));

		//calculate handlers before attempting to add them to the registered listeners, so all invalid settings can be reported.
		List<EventHandler> handlers = eventSubscriberMethods.map(method -> {
			if (method.getParameterCount() != 1)
				throw new IllegalArgumentException("EventSubscriber methods must accept only one argument. Invalid method " + method);

			Class<?> eventClass = method.getParameterTypes()[0];
			if (!Event.class.isAssignableFrom(eventClass))
				throw new IllegalArgumentException("Argument type is not Event nor a subclass of it. Invalid method " + method);

			method.setAccessible(true);
			try {
				MethodHandle mh = lookup.unreflect(method);
				if (listener != null) mh = mh.bindTo(listener);
				final MethodHandle methodHandle = mh;
				return new MethodEventHandler(eventClass, methodHandle, method, listener, isTemporary);
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException("Method " + method + " is not accessible", ex);
			}

		}).collect(Collectors.toList());

		listenersRegistry.updateAndGet(set -> {
			HashSet<EventHandler> n = (HashSet<EventHandler>) set.clone();
			for (EventHandler handler : handlers) {
				n.add(handler);
				Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Registered {}", handler);
			}
			n.addAll(handlers);
			return n;
		});
	}

	private <T extends Event> void registerListener(IListener<T> listener, boolean isTemporary) {
		Class<?> rawType = TypeResolver.resolveRawArgument(IListener.class, listener.getClass());
		if (!Event.class.isAssignableFrom(rawType)) throw new IllegalArgumentException("Type " + rawType + " is not a subclass of Event.");

		ListenerEventHandler eventHandler = new ListenerEventHandler(isTemporary, rawType, listener);
		listenersRegistry.updateAndGet(set -> {
			HashSet<EventHandler> n = (HashSet<EventHandler>) set.clone();
			n.add(eventHandler);
			Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Registered IListener {}", eventHandler);
			return n;
		});
	}

	/**
	 * This registers a temporary event listener using {@link EventSubscriber} method annotations. Meaning that when it listens to an event, it immediately unregisters itself.
	 *
	 * @param listener The listener.
	 */
	public void registerTemporaryListener(Object listener) {
		if (listener instanceof IListener)
			registerTemporaryListener((IListener<? extends Event>) listener);
		else
			registerListener(listener.getClass(), listener, true);
	}

	/**
	 * This registers a temporary event listener using {@link EventSubscriber} method annotations. Meaning that when it listens to an event, it immediately unregisters itself.
	 *
	 * @param listener The listener.
	 */
	public void registerTemporaryListener(Class<?> listener) {
		registerListener(listener, null, true);
	}

	/**
	 * This registers a temporary single event listener. Meaning that when it listens to an event, it immediately unregisters itself.
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
		waitFor((Event event) -> eventClass.isAssignableFrom(event.getClass()));
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched.
	 *
	 * @param eventClass The class of the event to wait for.
	 * @param time The timeout, in milliseconds. After this amount of time is reached, the thread is notified regardless of whether the event fired.
	 * @param <T> The event type to wait for.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> void waitFor(Class<T> eventClass, long time) throws InterruptedException {
		waitFor((Event event) -> eventClass.isAssignableFrom(event.getClass()), time);
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched.
	 *
	 * @param eventClass The class of the event to wait for.
	 * @param time The timeout. After this amount of time is reached, the thread is notified regardless of whether the event fired.
	 * @param unit The unit for the time parameter.
	 * @param <T> The event type to wait for.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> void waitFor(Class<T> eventClass, long time, TimeUnit unit) throws InterruptedException {
		waitFor((Event event) -> eventClass.isAssignableFrom(event.getClass()), time, unit, () -> {
		});
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched.
	 *
	 * @param eventClass The class of the event to wait for.
	 * @param time The timeout. After this amount of time is reached, the thread is notified regardless of whether the event fired.
	 * @param unit The unit for the time parameter.
	 * @param onTimeout The procedure to execute when the timeout is reached.
	 * @param <T> The event type to wait for.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> void waitFor(Class<T> eventClass, long time, TimeUnit unit, Procedure onTimeout) throws InterruptedException {
		waitFor((Event event) -> eventClass.isAssignableFrom(event.getClass()), time, unit, onTimeout);
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched and the provided {@link Predicate} returns true.
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
	 * This causes the currently executing thread to wait until the specified event is dispatched and the provided {@link Predicate} returns true.
	 *
	 * @param filter This is called to determine whether the thread should be resumed as a result of this event.
	 * @param time The timeout, in milliseconds. After this amount of time is reached, the thread is notified regardless of whether the event fired.
	 * @param <T> The event type to wait for.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> void waitFor(Predicate<T> filter, long time) throws InterruptedException {
		waitFor(filter, time, TimeUnit.MILLISECONDS, () -> {
		});
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched and the provided {@link Predicate} returns true.
	 *
	 * @param filter This is called to determine whether the thread should be resumed as a result of this event.
	 * @param time The timeout, in milliseconds. After this amount of time is reached, the thread is notified regardless of whether the event fired.
	 * @param unit The unit for the time parameter.
	 * @param <T> The event type to wait for.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> void waitFor(Predicate<T> filter, long time, TimeUnit unit) throws InterruptedException {
		waitFor(filter, time, unit, () -> {
		});
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched and the provided {@link Predicate} returns true.
	 *
	 * @param filter This is called to determine whether the thread should be resumed as a result of this event.
	 * @param time The timeout. After this amount of time is reached, the thread is notified regardless of whether the event fired.
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
						unregisterListener(this);
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
		if (listener instanceof IListener) {
			unregisterListener((IListener) listener);
		} else {
			unregisterListener(listener.getClass(), listener);
		}
	}

	/**
	 * Unregisters a listener using {@link EventSubscriber} method annotations.
	 *
	 * @param clazz The listener class with static methods.
	 */
	public void unregisterListener(Class<?> clazz) {
		unregisterListener(clazz, null);
	}

	/**
	 * Iterates the methods of {@code clazz} finding their MethodEventHandler and removes them. Notice that removal depends on if we are looking for instance methods or static
	 * methods.
	 *
	 * @param clazz
	 * @param instance
	 */
	private void unregisterListener(Class<?> clazz, Object instance) {
		List<Method> methods = Arrays.asList(clazz.getMethods()).stream().
				filter(m -> m.getParameterCount() == 1 && Event.class.isAssignableFrom(m.getParameterTypes()[0])).collect(Collectors.toList());

		listenersRegistry.updateAndGet(set -> {
			HashSet<EventHandler> n = (HashSet<EventHandler>) set.clone();
			for (Iterator<EventHandler> it = n.iterator(); it.hasNext();) {
				EventHandler eventHandler = it.next();
				if (eventHandler instanceof MethodEventHandler) {
					MethodEventHandler handler = (MethodEventHandler) eventHandler;
					if (methods.contains(handler.method) && instance == handler.instance) {
						it.remove();
						Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Unregistered class method listener {}", clazz.getSimpleName(), handler.method.toString());
					}
				}
			}
			return n;
		});
	}

	/**
	 * Unregisters a single event listener.
	 *
	 * @param listener The listener.
	 */
	public void unregisterListener(IListener listener) {
		Class<?> rawType = TypeResolver.resolveRawArgument(IListener.class, listener.getClass());
		if (Event.class.isAssignableFrom(rawType)) {
			listenersRegistry.updateAndGet(set -> {
				HashSet<EventHandler> n = (HashSet<EventHandler>) set.clone();
				for (Iterator<EventHandler> iterator = n.iterator(); iterator.hasNext();) {
					EventHandler eventHandler = iterator.next();
					if (eventHandler instanceof ListenerEventHandler) {
						ListenerEventHandler<?> handler = (ListenerEventHandler) eventHandler;
						if (handler.listener == listener) {//Yes, the == is intentional. We want the exact same instance.
							iterator.remove();
							Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Unregistered IListener {}", listener);
						}
					}
				}
				return n;
			});
		}
	}

	private void unregisterHandler(EventHandler eventHandler) {
		listenersRegistry.updateAndGet(set -> {
			HashSet<EventHandler> n = (HashSet<EventHandler>) set.clone();
			n.remove(eventHandler);
			Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Unregistered event handler {}", eventHandler);
			return n;
		});
	}

	/**
	 * Dispatches an event.
	 *
	 * @param event The event.
	 */
	public synchronized void dispatch(Event event) {
		eventExecutor.submit(() -> {
			Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Dispatching event of type {}", event.getClass().getSimpleName());
			event.client = client;

			listenersRegistry.get().stream().filter(e -> e.accepts(event)).forEach(handler -> {
				try {
					handler.handle(event);
					if (handler.isTemporary()) unregisterHandler(handler);
				} catch (IllegalAccessException e) {
					Discord4J.LOGGER.error(LogMarkers.EVENTS, "Error dispatching event " + event.getClass().getSimpleName(), e);
				} catch (InvocationTargetException e) {
					Discord4J.LOGGER.error(LogMarkers.EVENTS, "Unhandled exception caught dispatching event " + event.getClass().getSimpleName(), e.getCause());
				} catch (Throwable e) {
					Discord4J.LOGGER.error(LogMarkers.EVENTS, "Unhandled exception caught dispatching event " + event.getClass().getSimpleName(), e);
				}
			});
		});
	}

	private static interface EventHandler {

		boolean isTemporary();

		boolean accepts(Event e);

		void handle(Event e) throws Throwable;
	}

	private static class MethodEventHandler implements EventHandler {

		private final Class<?> eventClass;
		private final MethodHandle methodHandle;
		private final Method method;
		private final Object instance;
		private final boolean temporary;

		public MethodEventHandler(Class<?> eventClass, MethodHandle methodHandle, Method method, Object instance, boolean temporary) {
			this.eventClass = eventClass;
			this.methodHandle = methodHandle;
			this.method = method;
			this.instance = instance;
			this.temporary = temporary;
		}

		@Override
		public boolean isTemporary() {
			return temporary;
		}

		@Override
		public boolean accepts(Event e) {
			return eventClass.isInstance(e);
		}

		@Override
		public void handle(Event e) throws Throwable {
			methodHandle.invoke(e);
		}

		@Override
		public String toString() {
			return method.toString();
		}

	}

	private static class ListenerEventHandler<T extends Event> implements EventHandler {

		private final boolean isTemporary;
		private final Class<?> rawType;
		private final IListener<T> listener;

		public ListenerEventHandler(boolean isTemporary, Class<?> rawType, IListener<T> listener) {
			this.isTemporary = isTemporary;
			this.rawType = rawType;
			this.listener = listener;
		}

		@Override
		public boolean isTemporary() {
			return isTemporary;
		}

		@Override
		public boolean accepts(Event e) {
			return rawType.isInstance(e);
		}

		@Override
		public void handle(Event e) throws Throwable {
			listener.handle((T) e);
		}

		@Override
		public String toString() {
			return listener.getClass().getSimpleName();
		}
	}
}
