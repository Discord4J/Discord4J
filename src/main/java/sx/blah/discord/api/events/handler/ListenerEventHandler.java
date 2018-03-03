package sx.blah.discord.api.events.handler;

import java.util.concurrent.Executor;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.IListener;

/**
 * An event handler implementation that delegates the event invocation to
 * {@link IListener#handle(Event)} method.
 * 
 * @param <T>
 *            the event type.
 */
public final class ListenerEventHandler<T extends Event> implements EventHandler {

	/**
	 * The reflected class type of the event.
	 */
	private final Class<?> eventClass;

	/**
	 * The listener which we are delegating to.
	 */
	private final IListener<T> listener;

	/**
	 * The executor which will be used to execute the event.
	 */
	private final Executor executor;

	/**
	 * Tells whether this handler is a temporary handler or not.
	 */
	private final boolean temporary;

	/**
	 * Constructs a new {@link ListenerEventHandler} object instance.
	 * 
	 * @param eventClass
	 *            the reflected class type of the event.
	 * @param listener
	 *            the listener which we are delegating to.
	 * @param executor
	 *            the executor which will be used to execute the event.
	 * @param temporary
	 *            tells whether this handler is a temporary handler or not.
	 */
	public ListenerEventHandler(Class<?> eventClass, IListener<T> listener, Executor executor, boolean temporary) {
		this.temporary = temporary;
		this.eventClass = eventClass;
		this.listener = listener;
		this.executor = executor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sx.blah.discord.api.events.handler.EventHandler#handle(sx.blah.discord.api.events.Event)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void handle(Event e) throws Throwable {
		listener.handle((T) e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sx.blah.discord.api.events.handler.EventHandler#accepts(sx.blah.discord.api.events.Event)
	 */
	@Override
	public boolean accepts(Event e) {
		return eventClass.isInstance(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return listener.getClass().getSimpleName();
	}

	/**
	 * Gets the listener which we will delegate the invocation to.
	 * 
	 * @return the listener object.
	 */
	public IListener<T> getListener() {
		return listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sx.blah.discord.api.events.handler.EventHandler#getExecutor()
	 */
	@Override
	public Executor getExecutor() {
		return executor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sx.blah.discord.api.events.handler.EventHandler#isTemporary()
	 */
	@Override
	public boolean isTemporary() {
		return temporary;
	}

}