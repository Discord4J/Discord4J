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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;

public class EventDispatcherTest {

	@BeforeClass
	public static void setupLogger() {
		Discord4J.Discord4JLogger l = (Discord4J.Discord4JLogger) Discord4J.LOGGER;
		l.setLevel(Discord4J.Discord4JLogger.Level.TRACE);
	}

	@Test
	public void testRegisterIListener() throws Exception {
		EventDispatcher instance = new EventDispatcher(null);
		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(instance);
		int originalSize = internalRegistry.get().size();
		instance.registerListener((IListener<Event>) (Event event) -> {
		});
		assertEquals(originalSize + 1, internalRegistry.get().size());
	}

	@Test
	public void testRegisterInstanceEventSubscribers() throws Exception {
		EventDispatcher instance = new EventDispatcher(null);
		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(instance);
		int originalSize = internalRegistry.get().size();

		StaticAndInstanceEventHandler eventHandler = new StaticAndInstanceEventHandler();

		instance.registerListener(eventHandler);
		assertEquals(originalSize + 2, internalRegistry.get().size());
	}

	@Test
	public void testRegisterStaticEventSubscribers() throws Exception {
		EventDispatcher instance = new EventDispatcher(null);
		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(instance);
		int originalSize = internalRegistry.get().size();

		instance.registerListener(StaticAndInstanceEventHandler.class);
		assertEquals(originalSize + 2, internalRegistry.get().size());
	}

	@Test
	public void testRegisterStaticAndInstanceEventSubscribers() throws Exception {
		EventDispatcher instance = new EventDispatcher(null);
		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(instance);
		int originalSize = internalRegistry.get().size();

		StaticAndInstanceEventHandler eventHandler = new StaticAndInstanceEventHandler();
		instance.registerListener(eventHandler);
		assertEquals(originalSize + 2, internalRegistry.get().size());
		instance.registerListener(StaticAndInstanceEventHandler.class);
		assertEquals(originalSize + 4, internalRegistry.get().size());

	}

	@Test
	public void testUnregisterIListener() throws Exception {
		EventDispatcher instance = new EventDispatcher(null);
		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(instance);
		IListener<Event> listener = (Event event) -> {
		};
		instance.registerListener(listener);
		assertEquals(1, internalRegistry.get().size());
		instance.unregisterListener(listener);
		assertEquals(0, internalRegistry.get().size());
	}

	@Test
	public void testUnregisterInstanceEventSubscribers() throws Exception {
		EventDispatcher instance = new EventDispatcher(null);
		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(instance);

		StaticAndInstanceEventHandler eventHandler = new StaticAndInstanceEventHandler();

		instance.registerListener(eventHandler);
		assertEquals(2, internalRegistry.get().size());
		instance.unregisterListener(eventHandler);
		assertEquals(0, internalRegistry.get().size());
	}

	@Test
	public void testUnregisterStaticEventSubscribers() throws Exception {
		EventDispatcher instance = new EventDispatcher(null);
		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(instance);

		instance.registerListener(StaticAndInstanceEventHandler.class);
		assertEquals(2, internalRegistry.get().size());
		instance.unregisterListener(StaticAndInstanceEventHandler.class);
		assertEquals(0, internalRegistry.get().size());
	}

	@Test
	public void testUnregisterStaticAndInstanceEventSubscribers() throws Exception {
		EventDispatcher instance = new EventDispatcher(null);
		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(instance);

		StaticAndInstanceEventHandler eventHandler = new StaticAndInstanceEventHandler();

		instance.registerListener(eventHandler);
		instance.registerListener(StaticAndInstanceEventHandler.class);
		assertEquals(4, internalRegistry.get().size());
		instance.unregisterListener(eventHandler);
		assertEquals(2, internalRegistry.get().size());
		instance.unregisterListener(StaticAndInstanceEventHandler.class);
		assertEquals(0, internalRegistry.get().size());
	}

	@Test
	public void testDispatch() throws Exception {
		EventDispatcher instance = new EventDispatcher(null);
		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(instance);
		SynchronousQueue<MyEvent> q = new SynchronousQueue<>();
		IListener<MyEvent> l = e -> {
			try {
				q.put(e);
			} catch (InterruptedException ex) {
				throw new IllegalStateException(ex);
			}
		};
		instance.registerListener(l);
		assertEquals(1, internalRegistry.get().size());
		MyEvent evt = new MyEvent();
		instance.dispatch(evt);
		q.poll(1, TimeUnit.MINUTES);
	}

	@Test
	public void testRegisterTemporaryListener() throws Exception {
		EventDispatcher instance = new EventDispatcher(null);
		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(instance);
		SynchronousQueue<MyEvent> q = new SynchronousQueue<>();
		IListener<MyEvent> l = e -> {
			try {
				q.put(e);
			} catch (InterruptedException ex) {
				throw new IllegalStateException(ex);
			}
		};
		instance.registerTemporaryListener(l);
		assertEquals(1, internalRegistry.get().size());
		MyEvent evt = new MyEvent();
		instance.dispatch(evt);
		q.poll(1, TimeUnit.MINUTES);
		assertEquals(0, internalRegistry.get().size());
	}

	@Test
	public void testWaitFor() throws Exception {
		EventDispatcher instance = new EventDispatcher(null);
		SynchronousQueue<MyEvent> q = new SynchronousQueue<>();
		Thread thread = new Thread(() -> {
			try {
				MyEvent e = instance.waitFor(MyEvent.class);
				q.offer(e, 1, TimeUnit.MINUTES);
			} catch (InterruptedException ex) {
				throw new IllegalStateException(ex);
			}
		});
		thread.start();
		AtomicReference<HashSet<Object>> internalRegistry = getInternalRegistry(instance);
		for (int i = 0; internalRegistry.get().isEmpty() && i < 10; i++) { //await for the listener to be registered
			Thread.sleep(50);
		}
		assertEquals(1, internalRegistry.get().size());
		instance.dispatch(new MyEvent());
		MyEvent result = q.poll(1, TimeUnit.MINUTES);
		assertNotNull(result);
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
