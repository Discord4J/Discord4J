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

import net.jodah.typetools.TypeResolver;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.util.LogMarkers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages event listeners and event logic.
 *
 * The EventDispatcher stores a registry of listeners which are used on every event being dispatched. Dispatching of these events happens asynchronously
 * either on user provided threadpools, or a default threadpool.
 * <p/>
 * When registering a listener, the client has the option of specifying the thread pool for that particular listener, this way, different listeners are effectively
 * isolated in terms of threads, avoiding possible thread starvation.
 * <p/>
 * <b>Note on the default thread pool:</b>
 * It is assumed that when the user doesn't specify a threadpool, it's D4J responsibility to ensure correct asynchronous behavior. Because we have no control
 * whether a user blocks the thread belonging to the default executor or not, defensive measures must be taken not to overflow resources such as ram or cpu, which would
 * ultimately lead to a dead JVM. In this regard, the default executor is instantiated to a sensible amount of threads depending on the available cores on the machine
 * and supports a small events queue so as to handle bursts of events, nevertheless, if this queue gets filled up, it will slow down accordingly the producer of events
 * by forcing them to execute the listeners themselves. This has the desired effect of causing the Gateway threads to not process any more events, until consumers are
 * available in the downstream listeners.
 * <p/>
 * You are encouraged to provide your own threadpool to your listeners to have proper control of resources, using a ThreadPoolExecutor.CallerRunsPolicy rejection policy
 * to allow proper backpressure in case your threads are overwhelmed.
 */
public class EventDispatcher {

	private final MethodHandles.Lookup lookup = MethodHandles.lookup();
	private final AtomicReference<HashSet<EventHandler>> listenersRegistry = new AtomicReference<>(new HashSet<>());
	private final ExecutorService defaultEventExecutor;
	/**
	 * Special executor used for waitFor.
	 *
	 * Essentially forces the thread dispatching the event to execute the handler, because all wait for does is exchange with the blocking thread, this serves us well.
	 */
	private final Executor callingThreadExecutor = Runnable::run;
	private final IDiscordClient client;

	public EventDispatcher(IDiscordClient client, RejectedExecutionHandler backpressureHandler, int minimumPoolSize,
						   int maximumPoolSize, int overflowCapacity, long eventThreadTimeout, TimeUnit eventThreadTimeoutUnit) {
		this.client = client;
		this.defaultEventExecutor = new ThreadPoolExecutor(minimumPoolSize, maximumPoolSize, eventThreadTimeout,
				eventThreadTimeoutUnit, new ArrayBlockingQueue<>(overflowCapacity),
				DiscordUtils.createDaemonThreadFactory("Event Dispatcher Handler"), backpressureHandler);
	}

	/**
	 * Registers a listener using {@link EventSubscriber} method annotations.
	 *
	 * All events sent to this listener will be done asynchronously using a default thread pool configured by {@link java.util.concurrent.Executors}.newCachedThreadPool .
	 * <p/>
	 * Constraints:
	 * <li>Only public methods are considered.</li>
	 * <li>Methods annotated with {@link EventSubscriber} must accept exactly one argument.</li>
	 * <li>The argument to the method must be ${@link Event} or a subclass of it.</li>
	 * Note: this method will only register the annotated instance methods of the passed listener.
	 *
	 * @param listener The listener.
	 */
	public void registerListener(Object listener) {
		if (listener instanceof IListener)
			registerListener((IListener) listener);
		else
			registerListener(listener.getClass(), listener, false, defaultEventExecutor);
	}

	/**
	 * Registers a listener using {@link EventSubscriber} method annotations.
	 *
	 * All events sent to this listener will be handled asynchronously over the passed executor.
	 * <p/>
	 * Constraints:
	 * <li>Only public methods are considered.</li>
	 * <li>Methods annotated with {@link EventSubscriber} must accept exactly one argument.</li>
	 * <li>The argument to the method must be ${@link Event} or a subclass of it.</li>
	 * Note: this method will only register the annotated instance methods of the passed listener.
	 *
	 * @param executor Executor that will used to handle the events.
	 * @param listener The listener.
	 */
	public void registerListener(Executor executor, Object listener) {
		if (listener instanceof IListener)
			registerListener(executor, (IListener) listener);
		else
			registerListener(listener.getClass(), listener, false, executor);
	}

	/**
	 * Registers a listener using {@link EventSubscriber} method annotations.
	 *
	 * All events sent to this listener will be done asynchronously using a default thread pool configured by {@link java.util.concurrent.Executors}.newCachedThreadPool .
	 * <p/>
	 * Constraints:
	 * <li>Only public methods are considered.</li>
	 * <li>Methods annotated with {@link EventSubscriber} must accept exactly one argument.</li>
	 * <li>The argument to the method must be ${@link Event} or a subclass of it.</li>
	 * Note: this method will only register the annotated static methods of the passed listener.
	 *
	 * @param listener The listener.
	 */
	public void registerListener(Class<?> listener) {
		registerListener(listener, null, false, defaultEventExecutor);
	}

	/**
	 * Registers a listener using {@link EventSubscriber} method annotations.
	 *
	 * All events sent to this listener will be handled asynchronously over the passed executor.
	 * <p/>
	 * Constraints:
	 * <li>Only public methods are considered.</li>
	 * <li>Methods annotated with {@link EventSubscriber} must accept exactly one argument.</li>
	 * <li>The argument to the method must be ${@link Event} or a subclass of it.</li>
	 * Note: this method will only register the annotated static methods of the passed listener.
	 *
	 * @param executor Executor that will used to handle the events.
	 * @param listener The listener.
	 */
	public void registerListener(Executor executor, Class<?> listener) {
		registerListener(listener, null, false, executor);
	}

	/**
	 * Registers a single event listener.
	 *
	 * All events sent to this listener will be done asynchronously using a default thread pool configured by {@link java.util.concurrent.Executors}.newCachedThreadPool .
	 *
	 * @param listener The listener.
	 */
	public void registerListener(IListener listener) {
		registerListener(listener, false, defaultEventExecutor);
	}

	/**
	 * Registers a single event listener.
	 *
	 * All events sent to this listener will be handled asynchronously over the passed executor.
	 *
	 * @param executor Executor that will used to handle the events.
	 * @param listener The listener.
	 */
	public void registerListener(Executor executor, IListener listener) {
		registerListener(listener, false, executor);
	}

	/**
	 * Registers a set of listeners using {@link EventSubscriber} method annotations.
	 *
	 * All events sent to this listener will be done asynchronously using a default thread pool configured by {@link java.util.concurrent.Executors}.newCachedThreadPool .
	 * <p/>
	 * Constraints:
	 * <li>Only public methods are considered.</li>
	 * <li>Methods annotated with {@link EventSubscriber} must accept exactly one argument.</li>
	 * <li>The argument to the method must be ${@link Event} or a subclass of it.</li>
	 * Note: this method will only register the instance methods of the passed listener.
	 *
	 * @param listeners The listeners.
	 */
	public void registerListeners(Object... listeners) {
		Arrays.stream(listeners).forEach(this::registerListener);
	}

	/**
	 * Registers a set of listeners using {@link EventSubscriber} method annotations.
	 *
	 * All events sent to these listeners will be handled asynchronously over the passed executor.
	 * <p/>
	 * Constraints:
	 * <li>Only public methods are considered.</li>
	 * <li>Methods annotated with {@link EventSubscriber} must accept exactly one argument.</li>
	 * <li>The argument to the method must be ${@link Event} or a subclass of it.</li>
	 * Note: this method will only register the instance methods of the passed listener.
	 *
	 * @param executor Executor that will used to handle the events.
	 * @param listeners The listeners.
	 */
	public void registerListeners(Executor executor, Object... listeners) {
		Arrays.stream(listeners).forEach(l -> registerListener(executor, l));
	}

	/**
	 * Registers a set of listeners using {@link EventSubscriber} method annotations.
	 *
	 * All events sent to this listener will be done asynchronously using a default thread pool configured by {@link java.util.concurrent.Executors}.newCachedThreadPool .
	 * <p/>
	 * Constraints:
	 * <li>Only public methods are considered.</li>
	 * <li>Methods annotated with {@link EventSubscriber} must accept exactly one argument.</li>
	 * <li>The argument to the method must be ${@link Event} or a subclass of it.</li>
	 * Note: this method will only register the static methods of the passed listener.
	 *
	 * @param listeners The listeners.
	 */
	public void registerListeners(Class<?>... listeners) {
		Arrays.stream(listeners).forEach(this::registerListener);
	}

	/**
	 * Registers a set of listeners using {@link EventSubscriber} method annotations.
	 *
	 * All events sent to these listeners will be handled asynchronously over the passed executor.
	 * <p/>
	 * Constraints:
	 * <li>Only public methods are considered.</li>
	 * <li>Methods annotated with {@link EventSubscriber} must accept exactly one argument.</li>
	 * <li>The argument to the method must be ${@link Event} or a subclass of it.</li>
	 * Note: this method will only register the static methods of the passed listener.
	 *
	 * @param executor Executor that will used to handle the events.
	 * @param listeners The listeners.
	 */
	public void registerListeners(Executor executor, Class<?>... listeners) {
		Arrays.stream(listeners).forEach(l -> registerListener(executor, l));
	}

	/**
	 * Registers a set of single event listeners.
	 *
	 * All events sent to this listener will be done asynchronously using a default thread pool configured by {@link java.util.concurrent.Executors}.newCachedThreadPool .
	 *
	 * @param listeners The listeners.
	 */
	public void registerListeners(IListener... listeners) {
		Arrays.stream(listeners).forEach(this::registerListener);
	}

	/**
	 * Registers a set of single event listeners.
	 *
	 * All events sent to these listeners will be handled asynchronously over the passed executor.
	 *
	 * @param executor Executor that will used to handle the events.
	 * @param listeners The listeners.
	 */
	public void registerListeners(Executor executor, IListener... listeners) {
		Arrays.stream(listeners).forEach(l -> registerListener(executor, l));
	}

	private void registerListener(Class<?> listenerClass, Object listener, boolean isTemporary, Executor executor) {
		if (IListener.class.isAssignableFrom(listenerClass)) {
			Discord4J.LOGGER.warn(LogMarkers.EVENTS, "IListener was attempted to be registered as an annotation listener. The listener in question will now be registered as an IListener.");
			registerListener((IListener) listener, isTemporary, executor);
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
				MethodHandle methodHandle = lookup.unreflect(method);
				if (listener != null) methodHandle = methodHandle.bindTo(listener);
				return new MethodEventHandler(eventClass, methodHandle, method, listener, isTemporary, executor);
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException("Method " + method + " is not accessible", ex);
			}

		}).collect(Collectors.toList());

		listenersRegistry.updateAndGet(set -> {
			HashSet<EventHandler> updatedSet = (HashSet<EventHandler>) set.clone();
			for (EventHandler handler : handlers) {
				updatedSet.add(handler);
				Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Registered {}", handler);
			}
			updatedSet.addAll(handlers);
			return updatedSet;
		});
	}

	private <T extends Event> void registerListener(IListener<T> listener, boolean isTemporary, Executor executor) {
		Class<?> rawType = TypeResolver.resolveRawArgument(IListener.class, listener.getClass());
		if (!Event.class.isAssignableFrom(rawType)) throw new IllegalArgumentException("Type " + rawType + " is not a subclass of Event.");

		ListenerEventHandler eventHandler = new ListenerEventHandler(isTemporary, rawType, listener, executor);
		listenersRegistry.updateAndGet(set -> {
			HashSet<EventHandler> updatedSet = (HashSet<EventHandler>) set.clone();
			updatedSet.add(eventHandler);
			Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Registered IListener {}", eventHandler);
			return updatedSet;
		});
	}

	/**
	 * This registers a temporary event listener using {@link EventSubscriber} method annotations.
	 *
	 * Meaning that when it listens to an event, it immediately unregisters itself.
	 *
	 * @see registerListener(Object) registerListener for the constraints
	 *
	 * @param listener The listener.
	 */
	public void registerTemporaryListener(Object listener) {
		if (listener instanceof IListener)
			registerTemporaryListener((IListener<? extends Event>) listener);
		else
			registerListener(listener.getClass(), listener, true, defaultEventExecutor);
	}

	/**
	 * This registers a temporary event listener using {@link EventSubscriber} method annotations and passed Executor.
	 *
	 * Meaning that when it listens to an event, it immediately unregisters itself.
	 *
	 * @see registerListener(Executor, Object) registerListener for the constraints
	 *
	 * @param executor The executor where events will be handled.
	 * @param listener The listener.
	 */
	public void registerTemporaryListener(Executor executor, Object listener) {
		if (listener instanceof IListener)
			registerTemporaryListener(executor, (IListener<? extends Event>) listener);
		else
			registerListener(listener.getClass(), listener, true, executor);
	}

	/**
	 * This registers a temporary event listener using {@link EventSubscriber} method annotations.
	 *
	 * Meaning that when it listens to an event, it immediately unregisters itself.
	 *
	 * @see registerListener(Object) registerListener for the constraints
	 *
	 * @param listener The listener.
	 */
	public void registerTemporaryListener(Class<?> listener) {
		registerListener(listener, null, true, defaultEventExecutor);
	}

	/**
	 * This registers a temporary event listener using {@link EventSubscriber} method annotations.
	 *
	 * Meaning that when it listens to an event, it immediately unregisters itself.
	 *
	 * @see registerListener(Executor, Object) registerListener for the constraints
	 *
	 * @param executor The executor where events will be handled.
	 * @param listener The listener.
	 */
	public void registerTemporaryListener(Executor executor, Class<?> listener) {
		registerListener(listener, null, true, executor);
	}

	/**
	 * This registers a temporary single event listener.
	 *
	 * Meaning that when it listens to an event, it immediately unregisters itself.
	 *
	 * @see registerListener(Object) registerListener for the constraints
	 *
	 * @param listener The listener.
	 */
	public <T extends Event> void registerTemporaryListener(IListener<T> listener) {
		registerListener(listener, true, defaultEventExecutor);
	}

	/**
	 * This registers a temporary single event listener.
	 *
	 * Meaning that when it listens to an event, it immediately unregisters itself.
	 *
	 * @see registerListener(Object) registerListener for the constraints
	 *
	 * @param executor The executor where events will be handled.
	 * @param listener The listener.
	 */
	public <T extends Event> void registerTemporaryListener(Executor executor, IListener<T> listener) {
		registerListener(listener, true, executor);
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched.
	 *
	 * @param eventClass The class of the event to wait for.
	 * @param <T> The event type to wait for.
	 * @return The event found.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> T waitFor(Class<T> eventClass) throws InterruptedException {
		return (T) waitFor((Event event) -> eventClass.isAssignableFrom(event.getClass()));
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched.
	 *
	 * @param eventClass The class of the event to wait for.
	 * @param time The timeout, in milliseconds. After this amount of time is reached, the thread is notified regardless of whether the event fired.
	 * @param <T> The event type to wait for.
	 * @return The event found.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> T waitFor(Class<T> eventClass, long time) throws InterruptedException {
		return (T) waitFor((Event event) -> eventClass.isAssignableFrom(event.getClass()), time);
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched.
	 *
	 * @param eventClass The class of the event to wait for.
	 * @param time The timeout. After this amount of time is reached, the thread is notified regardless of whether the event fired.
	 * @param unit The unit for the time parameter.
	 * @param <T> The event type to wait for.
	 * @return The event found.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> T waitFor(Class<T> eventClass, long time, TimeUnit unit) throws InterruptedException {
		return (T) waitFor((Event event) -> eventClass.isAssignableFrom(event.getClass()), time, unit);
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched and the provided {@link Predicate} returns true.
	 *
	 * @param filter This is called to determine whether the thread should be resumed as a result of this event.
	 * @param <T> The event type to wait for.
	 * @return The event found.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> T waitFor(Predicate<T> filter) throws InterruptedException {
		return waitFor(filter, Long.MAX_VALUE);
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched and the provided {@link Predicate} returns true.
	 *
	 * @param filter This is called to determine whether the thread should be resumed as a result of this event.
	 * @param time The timeout, in milliseconds. After this amount of time is reached, the thread is notified regardless of whether the event fired.
	 * @param <T> The event type to wait for.
	 * @return The event found.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> T waitFor(Predicate<T> filter, long time) throws InterruptedException {
		return waitFor(filter, time, TimeUnit.MILLISECONDS);
	}

	/**
	 * This causes the currently executing thread to wait until the specified event is dispatched and the provided {@link Predicate} returns true.
	 *
	 * @param filter This is called to determine whether the thread should be resumed as a result of this event.
	 * @param time The timeout, in milliseconds. After this amount of time is reached, the thread is notified regardless of whether the event fired.
	 * @param unit The unit for the time parameter.
	 * @param <T> The event type to wait for.
	 * @return The event found.
	 *
	 * @throws InterruptedException
	 */
	public <T extends Event> T waitFor(Predicate<T> filter, long time, TimeUnit unit) throws InterruptedException {
		SynchronousQueue<T> result = new SynchronousQueue<>();
		// we need to account for the fact that the predicate will have an implicit cast introduced by the compiler
		// meanwhile new IListener<T> will erase to Object and there will be no compiler check, hence we manually introduce filterRawType.isInstance
		Class<?> filterRawType = TypeResolver.resolveRawArgument(Predicate.class, filter.getClass());
		registerListener(callingThreadExecutor, new IListener<T>() {
			@Override
			public void handle(T event) {
				if (filterRawType.isInstance(event) && filter.test(event)) {
					unregisterListener(this);
					result.offer(event);
				}
			}
		});
		return result.poll(time, unit);
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
			HashSet<EventHandler> updatedSet = (HashSet<EventHandler>) set.clone();
			for (Iterator<EventHandler> it = updatedSet.iterator(); it.hasNext();) {
				EventHandler eventHandler = it.next();
				if (eventHandler instanceof MethodEventHandler) {
					MethodEventHandler handler = (MethodEventHandler) eventHandler;
					if (methods.contains(handler.method) && instance == handler.instance) {
						it.remove();
						Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Unregistered class method listener {}", clazz.getSimpleName(), handler.method.toString());
					}
				}
			}
			return updatedSet;
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
				HashSet<EventHandler> updatedSet = (HashSet<EventHandler>) set.clone();
				for (Iterator<EventHandler> iterator = updatedSet.iterator(); iterator.hasNext();) {
					EventHandler eventHandler = iterator.next();
					if (eventHandler instanceof ListenerEventHandler) {
						ListenerEventHandler<?> handler = (ListenerEventHandler) eventHandler;
						if (handler.listener == listener) {//Yes, the == is intentional. We want the exact same instance.
							iterator.remove();
							Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Unregistered IListener {}", listener);
						}
					}
				}
				return updatedSet;
			});
		}
	}

	private void unregisterHandler(EventHandler eventHandler) {
		listenersRegistry.updateAndGet(set -> {
			HashSet<EventHandler> updatedSet = (HashSet<EventHandler>) set.clone();
			updatedSet.remove(eventHandler);
			Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Unregistered event handler {}", eventHandler);
			return updatedSet;
		});
	}

	/**
	 * Dispatches an event.
	 *
	 * @param event The event.
	 */
	public void dispatch(Event event) {
		Discord4J.LOGGER.trace(LogMarkers.EVENTS, "Dispatching event of type {}", event.getClass().getSimpleName());
		event.client = client;

		listenersRegistry.get().stream().filter(e -> e.accepts(event)).forEach(handler -> {
			handler.getExecutor().execute(() -> {
				try {
					if (handler.isTemporary()) unregisterHandler(handler);
					handler.handle(event);
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

	/**
	 * General behavior of an event handler.
	 */
	private static interface EventHandler {

		/**
		 * Should the handler be removed after handling an event.
		 *
		 * @return
		 */
		boolean isTemporary();

		/**
		 * Checks whether the handler should process the given event.
		 *
		 * @param e
		 * @return
		 */
		boolean accepts(Event e);

		Executor getExecutor();

		void handle(Event e) throws Throwable;
	}

	/**
	 * Specialized version of EventHandler that invokes the given MethodHandle for each event.
	 */
	private static class MethodEventHandler implements EventHandler {

		private final Class<?> eventClass;
		private final MethodHandle methodHandle;
		private final Method method;
		private final Object instance;
		private final boolean temporary;
		private final Executor executor;

		public MethodEventHandler(Class<?> eventClass, MethodHandle methodHandle, Method method, Object instance, boolean temporary, Executor executor) {
			this.eventClass = eventClass;
			this.methodHandle = methodHandle;
			this.method = method;
			this.instance = instance;
			this.temporary = temporary;
			this.executor = executor;
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
		public Executor getExecutor() {
			return executor;
		}

		@Override
		public String toString() {
			return method.toString();
		}

	}

	/**
	 * EventHandler implementation that delegates to an IListener.
	 *
	 * @param <T>
	 */
	private static class ListenerEventHandler<T extends Event> implements EventHandler {

		private final boolean isTemporary;
		private final Class<?> rawType;
		private final IListener<T> listener;
		private final Executor executor;

		public ListenerEventHandler(boolean isTemporary, Class<?> rawType, IListener<T> listener, Executor executor) {
			this.isTemporary = isTemporary;
			this.rawType = rawType;
			this.listener = listener;
			this.executor = executor;
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
		public Executor getExecutor() {
			return executor;
		}

		@Override
		public String toString() {
			return listener.getClass().getSimpleName();
		}
	}

	public static class CallerRunsPolicy implements RejectedExecutionHandler {

		long lastNotification = 0;

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			long now = System.currentTimeMillis();
			synchronized(this)  {
				if (now - lastNotification >= 5000) {
					Discord4J.LOGGER.warn(LogMarkers.EVENTS, "Event buffer limit exceeded, refer to the class-level javadocs for sx.blah.discord.api.events.EventDispatcher for more information.");
					lastNotification = now;
				}
			}
			if (!executor.isShutdown()) r.run();
		}

	}
}
