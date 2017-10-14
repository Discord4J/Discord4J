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

package sx.blah.discord.util;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.internal.DiscordUtils;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A utility class to help create asynchronous and synchronous interactions with the Discord API.
 *
 * <p>This builder works on the following model:
 * <ul>
 *     <li>The builder has one "active" action, which is the action currently being modified by these builder methods.</li>
 *     <li>When either {@link #andThen(IRequestAction)} or {@link #elseDo(IRequestAction)} is called, the "active"
 *     action is then placed in a queue and is replaced by a new action to be modified.</li>
 *     <li>After {@link #execute()} is called, the builder then goes down the queue of actions. For each action, it will
 *     wait for an event before and/or after its main {@link IRequestAction} is executed. Once the main
 *     action is executed, it will handle then call the action created by {@link #elseDo(IRequestAction)}
 *     if an exception was encountered and call the correct exception handler, otherwise it will repeat this process
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
	private final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor(DiscordUtils.createDaemonThreadFactory("RequestBuilder Async Executor"));

	public RequestBuilder(IDiscordClient client) {
		this.client = client;
	}

	/**
	 * Sets whether the request should be buffered for rate limits. False by default.
	 *
	 * @param shouldBuffer Whether the request should be buffered for rate limits.
	 * @return The builder instance.
	 *
	 * @see RequestBuffer RequestBuffer
	 */
	public RequestBuilder shouldBufferRequests(boolean shouldBuffer) {
		this.bufferRequests = shouldBuffer;
		return this;
	}

	/**
	 * Sets whether the request should fail when an exception is encountered. It is essentially the same as
	 * {@link IRequestAction#execute()} returning false. This is true by default.
	 *
	 * @param shouldFail Whether the request should fail when an exception is encountered.
	 * @return The builder instance.
	 */
	public RequestBuilder shouldFailOnException(boolean shouldFail) {
		this.failOnException = shouldFail;
		return this;
	}

	/**
	 * Sets whether the request should be executed asynchronously. This is false by default.
	 *
	 * @param isAsync Whether the request should be executed asynchronously.
	 * @return The builder instance.
	 */
	public RequestBuilder setAsync(boolean isAsync) {
		this.isAsync = isAsync;
		return this;
	}

	/**
	 * Sets the currently "active" action's actual action.
	 *
	 * @param action The action to run.
	 * @return The builder instance.
	 */
	public RequestBuilder doAction(IRequestAction action) {
		activeAction.action = action;
		return this;
	}

	/**
	 * Sets the currently active action to execute AFTER the specified event filter is passed.
	 *
	 * @param eventFilter A function that returns true when the the action should execute.
	 * @param <T> The type of event to wait for.
	 * @return The builder instance.
	 */
	public <T extends Event> RequestBuilder doActionAfter(Predicate<T> eventFilter) {
		return doActionAfter(eventFilter, 0);
	}

	/**
	 * Sets the currently active action to execute AFTER the specified event filter is passed.
	 *
	 * @param eventFilter A function that returns true when the the action should execute.
	 * @param time The timeout, in milliseconds, after which the action should execute regardless of the event filter.
	 * @param <T> The type of event to wait for.
	 * @return The builder instance.
	 */
	public <T extends Event> RequestBuilder doActionAfter(Predicate<T> eventFilter, long time) {
		return doActionAfter(eventFilter, time, TimeUnit.MILLISECONDS);
	}

	/**
	 * Sets the currently active action to execute AFTER the specified event filter is passed.
	 *
	 * @param eventFilter A function that returns true when the the action should execute.
	 * @param time The timeout, in milliseconds, after which the action should execute regardless of the event filter.
	 * @param unit The time unit of the timeout.
	 * @param <T> The type of event to wait for.
	 * @return The builder instance.
	 */
	public <T extends Event> RequestBuilder doActionAfter(Predicate<T> eventFilter, long time, TimeUnit unit) {
		activeAction.waitBefore = eventFilter;
		activeAction.waitBeforeTimeout = unit.toMillis(time);
		return this;
	}

	/**
	 * Sets the currently active action to execute BEFORE the specified event filer is passed.
	 *
	 * @param eventFilter A function that returns true when the the action should execute.
	 * @param <T> The type of event to wait for.
	 * @return The builder instance.
	 */
	public <T extends Event> RequestBuilder doActionBefore(Predicate<T> eventFilter) {
		return doActionBefore(eventFilter, 0);
	}

	/**
	 * Sets the currently active action to execute BEFORE the specified event filer is passed.
	 *
	 * @param eventFilter A function that returns true when the the action should execute.
	 * @param time The timeout, in milliseconds, after which the action should execute regardless of the event filter.
	 * @param <T> The type of event to wait for.
	 * @return The builder instance.
	 */
	public <T extends Event> RequestBuilder doActionBefore(Predicate<T> eventFilter, long time) {
		return doActionBefore(eventFilter, time, TimeUnit.MILLISECONDS);
	}

	/**
	 * Sets the currently active action to execute BEFORE the specified event filer is passed.
	 *
	 * @param eventFilter A function that returns true when the the action should execute.
	 * @param time The timeout, in milliseconds, after which the action should execute regardless of the event filter.
	 * @param unit The time unit of the timeout.
	 * @param <T> The type of event to wait for.
	 * @return The builder instance.
	 */
	public <T extends Event> RequestBuilder doActionBefore(Predicate<T> eventFilter, long time, TimeUnit unit) {
		activeAction.waitAfter = eventFilter;
		activeAction.waitAfterTimeout = unit.toMillis(time);
		return this;
	}

	/**
	 * Sets the handler for when {@link DiscordException}s occur. By default the handler just logs the exception.
	 *
	 * @param exceptionConsumer The exception handler.
	 * @return The builder instance.
	 */
	public RequestBuilder onDiscordError(Consumer<DiscordException> exceptionConsumer) {
		activeAction.discordExceptionHandler = exceptionConsumer;
		return this;
	}

	/**
	 * Sets the handler for when {@link RateLimitException}s occur. By default the handler just logs the exception.
	 *
	 * @param exceptionConsumer The exception handler.
	 * @return The builder instance.
	 */
	public RequestBuilder onRatelimitError(Consumer<RateLimitException> exceptionConsumer) {
		activeAction.rateLimitHandler = exceptionConsumer;
		return this;
	}

	/**
	 * Sets the handler for when {@link MissingPermissionsException}s occur. By default the handler just logs the exception.
	 *
	 * @param exceptionConsumer The exception handler.
	 * @return The builder instance.
	 */
	public RequestBuilder onMissingPermissionsError(Consumer<MissingPermissionsException> exceptionConsumer) {
		activeAction.missingPermissionHandler = exceptionConsumer;
		return this;
	}

	/**
	 * Sets the handler for when external {@link Exception}s occur. By default the handler just logs the exception.
	 *
	 * @param exceptionConsumer The exception handler.
	 * @return The builder instance.
	 */
	public RequestBuilder onGeneralError(Consumer<Exception> exceptionConsumer) {
		activeAction.generalExceptionHandler = exceptionConsumer;
		return this;
	}

	/**
	 * Sets the handler for when the current action times out on doActionBefore or doActionAfter.
	 *
	 * @param timeoutProcedure The timeout handler.
	 * @return The builder instance.
	 */
	public RequestBuilder onTimeout(Procedure timeoutProcedure) {
		activeAction.timeoutHandler = timeoutProcedure;
		return this;
	}

	/**
	 * Sets the new active action which will be executed after the current one succeeds.
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
	 * Sets the new active action which will be executed after the current one succeeds.
	 *
	 * <p>This new action CANNOT have a following action, the following action would occur after the current one
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
	 * An alias for {@link #execute()}.
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
	 * Gets whether the request is cancelled
	 *
	 * @return Whether the request is cancelled.
	 */
	public boolean isCancelled() {
		return isCancelled;
	}

	/**
	 * Gets whether the request is finished executing or cancelled.
	 *
	 * @return Whether the request is finished executing or cancelled.
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
		 * Executes the action.
		 *
		 * @return Whether the execution was successful.
		 */
		public boolean execute() {
			if (action == null)
				throw new IllegalArgumentException("Action has no execution implementation!");

			boolean result = false;

			try {
				if (waitBefore != null) {
					if (client.getDispatcher().waitFor(waitBefore, waitBeforeTimeout, TimeUnit.MILLISECONDS) == null) {
						timeoutHandler.invoke();
					}
				}

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

				if (waitAfter != null) {
					if (client.getDispatcher().waitFor(waitAfter, waitAfterTimeout, TimeUnit.MILLISECONDS) == null) {
						timeoutHandler.invoke();
					}
				}

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
	 * The type of action an {@link Action} represents.
	 */
	private enum ActionMode {
		/**
		 * The action always occurs.
		 */
		ALWAYS,
		/**
		 * The action only occurs if the previous one didn't fail.
		 */
		NEXT,
		/**
		 * The action only occurs if the previous action failed.
		 */
		ELSE
	}

	/**
	 * A function that is executed by the request builder.
	 */
	@FunctionalInterface
	public interface IRequestAction {

		/**
		 * Executes the action.
		 *
		 * @return Whether the execution succeeded.
		 */
		boolean execute() throws Exception;
	}
}
