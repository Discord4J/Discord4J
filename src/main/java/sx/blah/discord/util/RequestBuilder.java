package sx.blah.discord.util;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.IDiscordClient;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This is a utility class to help create asynchronous and synchronous interactions with the discord api.<br>
 * This builder works on the following model:<br>
 * <ul>
 *     <li>The builder has one "active" action, which is the action currently being modified by these builder methods.</li>
 *     <li>When either {@link #andThen(IRequestAction)} or {@link #elseDo(IRequestAction)} are called, the "active"
 *     action is then placed on a queue and is replaced by a new action to be modified.</li>
 *     <li>After {@link #execute()} is called, the builder then goes down the queue of actions. For each action, it will
 *     wait for an event before and/or after its main {@link IRequestAction} is executed. Once the main
 *     {@link IRequestAction} is executed, it will handle then call the action created by {@link #elseDo(IRequestAction)}
 *     if an exception was encountered and call the correct exception handler, otherwise it'll then repeat this process
 *     on the next action as provided by the {@link #andThen(IRequestAction)} method.</li>
 * </ul>
 */
public class RequestBuilder {

	private final IDiscordClient client;
	private volatile boolean bufferRequests = false;
	private volatile boolean failOnException = true;
	private volatile boolean isAsync = false;
	private volatile boolean isDone = false;
	private volatile boolean isCancelled = false;
	private volatile Action activeAction = new Action();
	private final ConcurrentLinkedQueue<Action> actions = new ConcurrentLinkedQueue<>();
	private final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor(runnable -> { //Ensures all threads are daemons
		Thread thread = Executors.defaultThreadFactory().newThread(runnable);
		thread.setName("Request Builder Async Executor");
		thread.setDaemon(true);
		return thread;
	});

	public RequestBuilder(IDiscordClient client) {
		this.client = client;
	}

	/**
	 * Determines whether this request should be buffered for rate limits.
	 * This is false by default.
	 *
	 * @param shouldBuffer If true, rate limits will be handled automatically, if false, {@link RateLimitException}s have
	 * to be handled manually.
	 * @return The builder instance.
	 *
	 * @see RequestBuffer RequestBuffer
	 */
	public RequestBuilder shouldBufferRequests(boolean shouldBuffer) {
		this.bufferRequests = shouldBuffer;
		return this;
	}

	/**
	 * Determines whether this request should fail when an exception is encountered. It is essentially the same as
	 * {@link IRequestAction#execute()} returning false.
	 * This is true by default.
	 *
	 * @param shouldFail If true, the request will be cancelled on an exception, if false the request will continue on
	 * an exception.
	 * @return The builder instance.
	 */
	public RequestBuilder shouldFailOnException(boolean shouldFail) {
		this.failOnException = shouldFail;
		return this;
	}

	/**
	 * Determines whether or not this request should be asynchronous.
	 * This is false by default.
	 *
	 * @param isAsync If true, the request will be executed on a separate thread, if false the request will be executed
	 * on the current thread.
	 * @return The builder instance.
	 */
	public RequestBuilder setAsync(boolean isAsync) {
		this.isAsync = isAsync;
		return this;
	}

	/**
	 * This sets the currently "active" action's actual action.
	 *
	 * @param action The action to run.
	 * @return The builder instance.
	 */
	public RequestBuilder doAction(IRequestAction action) {
		activeAction.action = action;
		return this;
	}

	/**
	 * This makes the currently active action execute AFTER the specified event filter is passed.
	 *
	 * @param eventFilter The event filter, it should return true when the action should proceed.
	 * @param <T> The type of event to wait for.
	 * @return The builder instance.
	 */
	public <T extends Event> RequestBuilder doActionAfter(Predicate<T> eventFilter) {
		return doActionAfter(eventFilter, 0);
	}

	/**
	 * This makes the currently active action execute AFTER the specified event filter is passed.
	 *
	 * @param eventFilter The event filter, it should return true when the action should proceed.
	 * @param time The timeout, in milliseconds. After this amount of time is reached, the thread is notified regardless
	 * of whether the event fired.
	 * @param <T> The type of event to wait for.
	 * @return The builder instance.
	 */
	public <T extends Event> RequestBuilder doActionAfter(Predicate<T> eventFilter, long time) {
		return doActionAfter(eventFilter, time, TimeUnit.MILLISECONDS);
	}

	/**
	 * This makes the currently active action execute AFTER the specified event filter is passed.
	 *
	 * @param eventFilter The event filter, it should return true when the action should proceed.
	 * @param time The timeout. After this amount of time is reached, the thread is notified regardless of whether the
	 * event fired.
	 * @param unit The unit for the time parameter.
	 * @param <T> The type of event to wait for.
	 * @return The builder instance.
	 */
	public <T extends Event> RequestBuilder doActionAfter(Predicate<T> eventFilter, long time, TimeUnit unit) {
		activeAction.waitBefore = eventFilter;
		activeAction.waitBeforeTimeout = unit.toMillis(time);
		return this;
	}

	/**
	 * This makes the currently active action execute BEFORE the specified event filter is passed.
	 *
	 * @param eventFilter The event filter, it should return true when the action should proceed.
	 * @param <T> The type of event to wait for.
	 * @return The builder instance.
	 */
	public <T extends Event> RequestBuilder doActionBefore(Predicate<T> eventFilter) {
		return doActionBefore(eventFilter, 0);
	}

	/**
	 * This makes the currently active action execute BEFORE the specified event filter is passed.
	 *
	 * @param eventFilter The event filter, it should return true when the action should proceed.
	 * @param time The timeout, in milliseconds. After this amount of time is reached, the thread is notified regardless
	 * of whether the event fired.
	 * @param <T> The type of event to wait for.
	 * @return The builder instance.
	 */
	public <T extends Event> RequestBuilder doActionBefore(Predicate<T> eventFilter, long time) {
		return doActionBefore(eventFilter, time, TimeUnit.MILLISECONDS);
	}

	/**
	 * This makes the currently active action execute BEFORE the specified event filter is passed.
	 *
	 * @param eventFilter The event filter, it should return true when the action should proceed.
	 * @param time The timeout. After this amount of time is reached, the thread is notified regardless of whether the
	 * event fired.
	 * @param unit The unit for the time parameter.
	 * @param <T> The type of event to wait for.
	 * @return The builder instance.
	 */
	public <T extends Event> RequestBuilder doActionBefore(Predicate<T> eventFilter, long time, TimeUnit unit) {
		activeAction.waitAfter = eventFilter;
		activeAction.waitAfterTimeout = unit.toMillis(time);
		return this;
	}

	/**
	 * This creates a handler for when {@link DiscordException}s occur. By default the handler just logs the exception.
	 *
	 * @param exceptionConsumer The exception handler.
	 * @return The builder instance.
	 */
	public RequestBuilder onDiscordError(Consumer<DiscordException> exceptionConsumer) {
		activeAction.discordExceptionHandler = exceptionConsumer;
		return this;
	}

	/**
	 * This creates a handler for when {@link RateLimitException}s occur. By default the handler just logs the exception.
	 *
	 * @param exceptionConsumer The exception handler.
	 * @return The builder instance.
	 */
	public RequestBuilder onRatelimitError(Consumer<RateLimitException> exceptionConsumer) {
		activeAction.rateLimitHandler = exceptionConsumer;
		return this;
	}

	/**
	 * This creates a handler for when {@link MissingPermissionsException}s occur. By default the handler just logs the exception.
	 *
	 * @param exceptionConsumer The exception handler.
	 * @return The builder instance.
	 */
	public RequestBuilder onMissingPermissionsError(Consumer<MissingPermissionsException> exceptionConsumer) {
		activeAction.missingPermissionHandler = exceptionConsumer;
		return this;
	}

	/**
	 * This creates a handler for when external {@link Exception}s occur. By default the handler just logs the exception.
	 *
	 * @param exceptionConsumer The exception handler.
	 * @return The builder instance.
	 */
	public RequestBuilder onGeneralError(Consumer<Exception> exceptionConsumer) {
		activeAction.generalExceptionHandler = exceptionConsumer;
		return this;
	}

	/**
	 * This creates a handler for when the current action times out on doActionBefore or doActionAfter
	 *
	 * @param timeoutProcedure The timeout handler.
	 * @return The builder instance.
	 */
	public RequestBuilder onTimeout(Procedure timeoutProcedure) {
		activeAction.timeoutHandler = timeoutProcedure;
		return this;
	}

	/**
	 * This creates a new active action which will be executed after the current one succeeds.
	 *
	 * @param action The action to run.
	 * @return The builder instance.
	 */
	public RequestBuilder andThen(IRequestAction action) {
		actions.add(activeAction);
		activeAction = new Action();
		activeAction.mode = failOnException ? ActionMode.NEXT : ActionMode.ALWAYS;
		return doAction(action);
	}

	/**
	 * This creates a new active action which will be executed after the current one succeeds.
	 * Note: this new action CANNOT have a following action, the following action would occur after the current one
	 * succeeds.
	 *
	 * @param action The action to run.
	 * @return The builder instance.
	 */
	public RequestBuilder elseDo(IRequestAction action) {
		actions.add(activeAction);
		activeAction = new Action();
		activeAction.mode = ActionMode.ELSE;
		return doAction(action);
	}

	/**
	 * Same as {@link #execute()} since apparently there's supposed to be a build method in every builder class.
	 */
	public void build() {
		execute();
	}

	/**
	 * Executes the built request.
	 */
	public void execute() {
		actions.add(activeAction);
		Runnable requestRunnable = () -> {
			boolean previousResult = true;
			loop: for (Action action : actions) {
				if (isCancelled())
					return;

				switchStatement: switch (action.mode) {
					case NEXT:
						if (!previousResult)
							break switchStatement;
					case ALWAYS:
						previousResult = action.execute();
						break switchStatement;
					case ELSE:
						if (!previousResult) {
							action.execute();
							break loop;
						}
				}
			}
			isDone = true;
			asyncExecutor.shutdown();
		};
		if (isAsync) {
			asyncExecutor.submit(requestRunnable);
		} else {
			requestRunnable.run();
		}
	}

	/**
	 * Cancels the request.
	 */
	public void cancel() {
		isCancelled = true;
		asyncExecutor.shutdownNow();
	}

	/**
	 * Gets whether or not this request has been cancelled.
	 *
	 * @return True if cancelled, false if otherwise.
	 */
	public boolean isCancelled() {
		return isCancelled;
	}

	/**
	 * Gets whether or not this request is still in the process of executing.
	 *
	 * @return True if cancelled or finished, false if otherwise.
	 */
	public boolean isDone() {
		return isDone || isCancelled();
	}

	/**
	 * Internal class managing request actions.
	 */
	private class Action {

		private volatile IRequestAction action;
		private volatile Predicate<? extends Event> waitBefore;
		private volatile long waitBeforeTimeout = 0;
		private volatile Predicate<? extends Event> waitAfter;
		private volatile long waitAfterTimeout = 0;
		private volatile Consumer<Exception> generalExceptionHandler = (Exception e) -> Discord4J.LOGGER.error(LogMarkers.UTIL, "Exception caught executing action!", e);
		private volatile Consumer<RateLimitException> rateLimitHandler = (RateLimitException e) -> Discord4J.LOGGER.error(LogMarkers.UTIL, "Exception caught executing action!", e);
		private volatile Consumer<MissingPermissionsException> missingPermissionHandler = (MissingPermissionsException e) -> Discord4J.LOGGER.error(LogMarkers.UTIL, "Exception caught executing action!", e);
		private volatile Consumer<DiscordException> discordExceptionHandler = (DiscordException e) -> Discord4J.LOGGER.error(LogMarkers.UTIL, "Exception caught executing action!", e);
		private volatile Procedure timeoutHandler = () -> Discord4J.LOGGER.debug(LogMarkers.UTIL, "Action timed out.");
		private volatile ActionMode mode = ActionMode.ALWAYS;

		/**
		 * This is called to actually execute this action.
		 *
		 * @return True if successful, false if otherwise.
		 */
		public boolean execute() {
			if (action == null)
				throw new IllegalArgumentException("Action has no execution implementation!");

			boolean result = false;

			try {
				if (waitBefore != null)
					client.getDispatcher().waitFor(waitBefore, waitBeforeTimeout, TimeUnit.MILLISECONDS, timeoutHandler);

				if (bufferRequests) {
					Future<Boolean> futureResult = RequestBuffer.request(() -> {
						try {
							return action.execute();
						} catch (RateLimitException e) {
							throw e;
						} catch (MissingPermissionsException e) {
							missingPermissionHandler.accept(e);
						} catch (DiscordException e) {
							discordExceptionHandler.accept(e);
						} catch (Exception e) {
							generalExceptionHandler.accept(e);
						}
						return !failOnException;
					});
					while (!futureResult.isDone()) {}
					result = futureResult.get();
				} else {
					result = action.execute();
				}

				if (waitAfter != null)
					client.getDispatcher().waitFor(waitAfter, waitAfterTimeout, TimeUnit.MILLISECONDS, timeoutHandler);

			} catch (RateLimitException e) {
				rateLimitHandler.accept(e);
				result = !failOnException;
			} catch (MissingPermissionsException e) {
				missingPermissionHandler.accept(e);
				result = !failOnException;
			} catch (DiscordException e) {
				discordExceptionHandler.accept(e);
			} catch (Exception e) {
				generalExceptionHandler.accept(e);
				result = !failOnException;
			}

			return result;
		}
	}

	/**
	 * Represents the type of action an {@link Action} represents.
	 */
	private enum ActionMode {
		/**
		 * This action always occurs.
		 */
		ALWAYS,
		/**
		 * This action only occurs if the previous one didn't fail.
		 */
		NEXT,
		/**
		 * This action only occurs if the previous action failed.
		 */
		ELSE
	}

	/**
	 * This represents an action done for a request.
	 */
	@FunctionalInterface
	public interface IRequestAction {

		/**
		 * This is called to execute the action.
		 *
		 * @return True if the request was a success, false if otherwise.
		 *
		 * @throws RateLimitException
		 * @throws MissingPermissionsException
		 * @throws DiscordException
		 * @throws Exception
		 */
		boolean execute() throws RateLimitException, MissingPermissionsException, DiscordException, Exception;
	}
}
