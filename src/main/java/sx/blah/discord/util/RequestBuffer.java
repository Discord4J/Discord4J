package sx.blah.discord.util;

import sx.blah.discord.Discord4J;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * This is utility class intended to help with dealing with {@link HTTP429Exception}s by queueing rate-limited
 * operations until they can be sent.
 */
public class RequestBuffer {

	private static final Timer requestTimer = new Timer("Request Buffer Timer", true);

	/**
	 * Here it is, the magical method that does it all.
	 *
	 * @param request The request to be carried out.
	 * @param <T> The expected object to be returned.
	 * @return The future value, the future will have a value after the request has been successfully executed.
	 */
	public static <T> RequestFuture<T> request(IRequest<T> request) {
		final RequestFuture<T> future = new RequestFuture<>(request);
		if (!future.isDone()) {
			Discord4J.LOGGER.debug("Attempted request rate-limited, queueing retry in {}ms",
					future.getDelay(TimeUnit.MILLISECONDS));
			requestTimer.schedule(new RequestTimerTask<>(future), future.getDelay(TimeUnit.MILLISECONDS));
		}
		return future;
	}

	/**
	 * This kills all currently queued requests.
	 *
	 * @return The number of requests killed.
	 */
	public static int killAllRequests() {
		return requestTimer.purge();
	}

	/**
	 * This is used to model the request, NOTE: it IS a functional interface so you are encouraged to use lambdas!
	 *
	 * @param <T> The type of object this request is expected to return.
	 */
	@FunctionalInterface
	public interface IRequest<T> {

		/**
		 * This is called when the request is attempted.
		 *
		 * @return The result of this request, if any.
		 *
		 * @throws HTTP429Exception
		 */
		T request() throws HTTP429Exception;
	}

	/**
	 * This is used to model a request which returns nothing, NOTE: it IS a functional interface so you are encouraged
	 * to use lambdas!
	 */
	@FunctionalInterface
	public interface IVoidRequest extends IRequest<Object> {

		default Object request() throws HTTP429Exception {
			doRequest();
			return null;
		}

		/**
		 * This is called when the request is attempted.
		 *
		 * @throws HTTP429Exception
		 */
		void doRequest() throws HTTP429Exception;
	}

	/**
	 * This represents a future request which may or may not have been executed at the time of construction.
	 *
	 * @param <T> The request return type.
	 */
	public static class RequestFuture<T> implements Future<T>, Delayed {

		private volatile boolean isDone = false;
		private volatile boolean cancelled = false;
		private volatile T value = null;
		private volatile long timeForNextRequest;
		private final IRequest<T> request;

		public RequestFuture(IRequest<T> request) {
			this.request = request;

			tryAgain();
		}

		/**
		 * Cancels the request if it hasn't been executed.
		 *
		 * @param mayInterruptIfRunning Non-applicable.
		 * @return True if cancelled, false if otherwise (like if the request was already executed).
		 */
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if (!isDone())
				cancelled = true;
			return isCancelled();
		}

		/**
		 * Returns whether this request has been cancelled or not.
		 *
		 * @return True if cancelled, false if otherwise.
		 */
		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		/**
		 * Returns whether or not the request has been executed.
		 *
		 * @return True if executed, false if otherwise.
		 */
		@Override
		public boolean isDone() {
			return isDone;
		}

		/**
		 * Gets the request return value.
		 *
		 * @return The value, or null if it hasn't been executed yet.
		 */
		@Override
		public T get() {
			return value;
		}

		/**
		 * NO-OP
		 */
		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			throw new UnsupportedOperationException();
		}

		/**
		 * Gets the delay until the next request is attempted.
		 *
		 * @param unit The time unit for the delay.
		 * @return The delay.
		 */
		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(timeForNextRequest-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

		/**
		 * Compares this to another delayed object.
		 *
		 * @param o The other object.
		 * @return Negative if the delay on this object is less than the other object, positive if the opposite or 0
		 * if otherwise.
		 */
		@Override
		public int compareTo(Delayed o) {
			return (int) (getDelay(TimeUnit.MILLISECONDS)-o.getDelay(TimeUnit.MILLISECONDS));
		}

		/**
		 * Attempts to execute the request. Queues it again if unable.
		 *
		 * @return True if successful, false if otherwise.
		 */
		protected boolean tryAgain() {
			if (!isCancelled()) {
				try {
					value = request.request();
					timeForNextRequest = -1;
					isDone = true;
				} catch (HTTP429Exception e) {
					timeForNextRequest = System.currentTimeMillis()+e.getRetryDelay();
				}
			}
			return isDone() || isCancelled();
		}
	}

	/**
	 * Manages the request future to ensure it executes the request eventually.
	 */
	private static class RequestTimerTask<T> extends TimerTask {

		private final RequestFuture<T> future;

		private RequestTimerTask(RequestFuture<T> future) {
			this.future = future;
		}

		@Override
		public void run() {
			if (!future.tryAgain()) {
				synchronized (requestTimer) {
					requestTimer.schedule(new RequestTimerTask<T>(future), future.getDelay(TimeUnit.MILLISECONDS));
				}
			}
		}
	}
}
