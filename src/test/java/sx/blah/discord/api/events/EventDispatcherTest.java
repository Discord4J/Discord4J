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

import org.junit.BeforeClass;
import org.junit.Test;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class EventDispatcherTest {

	@BeforeClass
	public static void setupLogger() {
		Discord4J.Discord4JLogger l = (Discord4J.Discord4JLogger) Discord4J.LOGGER;
		l.setLevel(Discord4J.Discord4JLogger.Level.TRACE);
	}

	@Test
	public void testRegisterIListener() throws Exception {
		EventDispatcher eventDispatcher = new EventDispatcher(null, new EventDispatcher.CallerRunsPolicy(),
				1, Runtime.getRuntime().availableProcessors() * 4, 128,
				60L, TimeUnit.SECONDS);
		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(eventDispatcher);
		int originalSize = internalRegistry.get().size();
		eventDispatcher.registerListener((IListener<Event>) (Event event) -> {
		});
		assertEquals(originalSize + 1, internalRegistry.get().size());
	}

	@Test
	public void testRegisterInstanceEventSubscribers() throws Exception {
		EventDispatcher eventDispatcher = new EventDispatcher(null, new EventDispatcher.CallerRunsPolicy(),
				1, Runtime.getRuntime().availableProcessors() * 4, 128,
				60L, TimeUnit.SECONDS);		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(eventDispatcher);
		int originalSize = internalRegistry.get().size();

		StaticAndInstanceEventHandler eventHandler = new StaticAndInstanceEventHandler();

		eventDispatcher.registerListener(eventHandler);
		assertEquals(originalSize + 2, internalRegistry.get().size());
	}

	@Test
	public void testRegisterStaticEventSubscribers() throws Exception {
		EventDispatcher eventDispatcher = new EventDispatcher(null, new EventDispatcher.CallerRunsPolicy(),
				1, Runtime.getRuntime().availableProcessors() * 4, 128,
				60L, TimeUnit.SECONDS);		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(eventDispatcher);
		int originalSize = internalRegistry.get().size();

		eventDispatcher.registerListener(StaticAndInstanceEventHandler.class);
		assertEquals(originalSize + 2, internalRegistry.get().size());
	}

	@Test
	public void testRegisterStaticAndInstanceEventSubscribers() throws Exception {
		EventDispatcher eventDispatcher = new EventDispatcher(null, new EventDispatcher.CallerRunsPolicy(),
				1, Runtime.getRuntime().availableProcessors() * 4, 128,
				60L, TimeUnit.SECONDS);		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(eventDispatcher);
		int originalSize = internalRegistry.get().size();

		StaticAndInstanceEventHandler eventHandler = new StaticAndInstanceEventHandler();
		eventDispatcher.registerListener(eventHandler);
		assertEquals(originalSize + 2, internalRegistry.get().size());
		eventDispatcher.registerListener(StaticAndInstanceEventHandler.class);
		assertEquals(originalSize + 4, internalRegistry.get().size());

	}

	@Test
	public void testUnregisterIListener() throws Exception {
		EventDispatcher eventDispatcher = new EventDispatcher(null, new EventDispatcher.CallerRunsPolicy(),
				1, Runtime.getRuntime().availableProcessors() * 4, 128,
				60L, TimeUnit.SECONDS);		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(eventDispatcher);
		IListener<Event> listener = (Event event) -> {
		};
		eventDispatcher.registerListener(listener);
		assertEquals(1, internalRegistry.get().size());
		eventDispatcher.unregisterListener(listener);
		assertEquals(0, internalRegistry.get().size());
	}

	@Test
	public void testUnregisterInstanceEventSubscribers() throws Exception {
		EventDispatcher eventDispatcher = new EventDispatcher(null, new EventDispatcher.CallerRunsPolicy(),
				1, Runtime.getRuntime().availableProcessors() * 4, 128,
				60L, TimeUnit.SECONDS);		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(eventDispatcher);

		StaticAndInstanceEventHandler eventHandler = new StaticAndInstanceEventHandler();

		eventDispatcher.registerListener(eventHandler);
		assertEquals(2, internalRegistry.get().size());
		eventDispatcher.unregisterListener(eventHandler);
		assertEquals(0, internalRegistry.get().size());
	}

	@Test
	public void testUnregisterStaticEventSubscribers() throws Exception {
		EventDispatcher eventDispatcher = new EventDispatcher(null, new EventDispatcher.CallerRunsPolicy(),
				1, Runtime.getRuntime().availableProcessors() * 4, 128,
				60L, TimeUnit.SECONDS);		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(eventDispatcher);

		eventDispatcher.registerListener(StaticAndInstanceEventHandler.class);
		assertEquals(2, internalRegistry.get().size());
		eventDispatcher.unregisterListener(StaticAndInstanceEventHandler.class);
		assertEquals(0, internalRegistry.get().size());
	}

	@Test
	public void testUnregisterStaticAndInstanceEventSubscribers() throws Exception {
		EventDispatcher eventDispatcher = new EventDispatcher(null, new EventDispatcher.CallerRunsPolicy(),
				1, Runtime.getRuntime().availableProcessors() * 4, 128,
				60L, TimeUnit.SECONDS);		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(eventDispatcher);

		StaticAndInstanceEventHandler eventHandler = new StaticAndInstanceEventHandler();

		eventDispatcher.registerListener(eventHandler);
		eventDispatcher.registerListener(StaticAndInstanceEventHandler.class);
		assertEquals(4, internalRegistry.get().size());
		eventDispatcher.unregisterListener(eventHandler);
		assertEquals(2, internalRegistry.get().size());
		eventDispatcher.unregisterListener(StaticAndInstanceEventHandler.class);
		assertEquals(0, internalRegistry.get().size());
	}

	@Test
	public void testDispatch() throws Exception {
		EventDispatcher eventDispatcher = new EventDispatcher(null, new EventDispatcher.CallerRunsPolicy(),
				1, Runtime.getRuntime().availableProcessors() * 4, 128,
				60L, TimeUnit.SECONDS);		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(eventDispatcher);
		SynchronousQueue<MyEvent> interThreadExchange = new SynchronousQueue<>();
		IListener<MyEvent> l = e -> {
			try {
				interThreadExchange.put(e);
			} catch (InterruptedException ex) {
				throw new IllegalStateException(ex);
			}
		};
		eventDispatcher.registerListener(l);
		assertEquals(1, internalRegistry.get().size());
		MyEvent evt = new MyEvent();
		eventDispatcher.dispatch(evt);
		interThreadExchange.poll(1, TimeUnit.MINUTES);
	}

	@Test
	public void testRegisterTemporaryListener() throws Exception {
		EventDispatcher eventDispatcher = new EventDispatcher(null, new EventDispatcher.CallerRunsPolicy(),
				1, Runtime.getRuntime().availableProcessors() * 4, 128,
				60L, TimeUnit.SECONDS);		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(eventDispatcher);
		SynchronousQueue<MyEvent> interThreadExchange = new SynchronousQueue<>();
		IListener<MyEvent> l = e -> {
			try {
				interThreadExchange.put(e);
			} catch (InterruptedException ex) {
				throw new IllegalStateException(ex);
			}
		};
		eventDispatcher.registerTemporaryListener(l);
		assertEquals(1, internalRegistry.get().size());
		MyEvent evt = new MyEvent();
		eventDispatcher.dispatch(evt);
		interThreadExchange.poll(1, TimeUnit.MINUTES);
		assertEquals(0, internalRegistry.get().size());
	}

	@Test
	public void testWaitFor() throws Exception {
		EventDispatcher eventDispatcher = new EventDispatcher(null, new EventDispatcher.CallerRunsPolicy(),
				1, Runtime.getRuntime().availableProcessors() * 4, 128,
				60L, TimeUnit.SECONDS);		SynchronousQueue<MyEvent> interThreadExchange = new SynchronousQueue<>();
		Thread thread = new Thread(() -> {
			try {
				MyEvent e = eventDispatcher.waitFor(MyEvent.class);
				interThreadExchange.offer(e, 1, TimeUnit.MINUTES);
			} catch (InterruptedException ex) {
				throw new IllegalStateException(ex);
			}
		});
		thread.start();
		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(eventDispatcher);
		for (int i = 0; internalRegistry.get().isEmpty() && i < 10; i++) { //await for the listener to be registered
			Thread.sleep(50);
		}
		assertEquals(1, internalRegistry.get().size());
		eventDispatcher.dispatch(new MyEvent());
		MyEvent result = interThreadExchange.poll(1, TimeUnit.MINUTES);
		assertNotNull(result);
	}

	@Test
	public void testDispatchWithCustomExecutor() throws Exception {
		EventDispatcher eventDispatcher = new EventDispatcher(null, new EventDispatcher.CallerRunsPolicy(),
				1, Runtime.getRuntime().availableProcessors() * 4, 128,
				60L, TimeUnit.SECONDS);		AtomicBoolean handled = new AtomicBoolean(false); //doesn't need to be atomic at all, but it's the easiest mutable boolean I can use within a subclas
		Executor localThreadExecutor = (Runnable command) -> {
			command.run();
			handled.set(true);
		};
		eventDispatcher.registerListener(localThreadExecutor, (IListener<MyEvent>) (MyEvent event) -> {
		});
		eventDispatcher.dispatch(new MyEvent());
		assertTrue(handled.get());
	}

	@Test
	public void testDispatcherBackpressure() throws Exception {
		EventDispatcher eventDispatcher = new EventDispatcher(null, new EventDispatcher.CallerRunsPolicy(),
				1, Runtime.getRuntime().availableProcessors() * 4, 128,
				60L, TimeUnit.SECONDS);		AtomicBoolean backpressured = new AtomicBoolean(false);
		Thread thisThread = Thread.currentThread();

		eventDispatcher.registerListener((IListener<MyEvent>) evt -> {
			if (Thread.currentThread() == thisThread) {
				backpressured.set(true);
			} else {
				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		while (!backpressured.get()) {
			eventDispatcher.dispatch(new MyEvent());
		}
	}

	private AtomicReference<HashSet<Object>> getInternalRegistry(EventDispatcher dispatcher) throws Exception {
		Field declaredField = EventDispatcher.class.getDeclaredField("listenersRegistry");
		declaredField.setAccessible(true);
		return (AtomicReference<HashSet<Object>>) declaredField.get(dispatcher);
	}

	class MyEvent extends Event {
	}

	static class StaticAndInstanceEventHandler {

		@EventSubscriber public void handleInstance(MessageEvent e) {

		}

		@EventSubscriber public void handleInstance(MessageDeleteEvent e) {

		}

		@EventSubscriber public static void handleStatic(MessageEvent e) {

		}

		@EventSubscriber public static void handleStatic(MessageDeleteEvent e) {

		}
	}
}
