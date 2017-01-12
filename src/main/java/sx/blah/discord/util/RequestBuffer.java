package sx.blah.discord.util;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.DiscordUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is a utility class intended to help with dealing with {@link RateLimitException}s by queueing rate-limited
 * operations until they can be sent.
 */
public class RequestBuffer {

	private static final ExecutorService initialExecutor = Executors.newSingleThreadExecutor(DiscordUtils.createDaemonThreadFactory("RequestBuffer Initial Executor"));
	private static final Map<String, ScheduledExecutorService> requestServices = new ConcurrentHashMap<>();
	private static final Map<String, List<RequestFuture>> requests = new ConcurrentHashMap<>();

	/**
	 * Here it is, the magical method that does it all.
	 *
	 * @param request The request to be carried out.
	 * @param <T> The expected object to be returned.
	 * @return The future value, the future will have a value after the request has been successfully executed.
	 */
	public static <T> RequestFuture<T> request(IRequest<T> request) {
		final RequestFuture<T> future = new RequestFuture<>(request);
		initialExecutor.execute(() -> {
			if (!future.tryAgain()) {
				Discord4J.LOGGER.debug(LogMarkers.UTIL, "Attempted request rate-limited, queueing retry in {}ms",
						future.getDelay(TimeUnit.MILLISECONDS));
				
				synchronized (requests) {
					
					if (!requests.containsKey(future.getBucket())) {
						requests.put(future.getBucket(), new CopyOnWriteArrayList<>());
						requestServices.put(future.getBucket(), Executors.newSingleThreadScheduledExecutor(DiscordUtils.createDaemonThreadFactory("RequestBuffer Retry Handler")));
						requestServices.get(future.getBucket()).schedule(new RequestRunnable(future.getBucket()), future.getDelay(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
					}
					
					requests.get(future.getBucket()).add(future);
				}
			}
		});
		return future;
	}

	/**
	 * This is a version of {@link #request(IRequest)} without a return value. No functional difference, only more
	 * continence.
	 *
	 * @param request The request to be carried out.
	 * @return The request future.
	 */
	public static RequestFuture<Void> request(IVoidRequest request) {
		return request((IRequest<Void>) request); //Casted to use the correct request() method
	}

	/**
	 * This returns the number of incomplete requests.
	 *
	 * @return The number of incomplete requests.
	 */
	public static int getIncompleteRequestCount() {
		final AtomicInteger count = new AtomicInteger();
		synchronized (requests) {
			requests.values().parallelStream().forEach((requestFutures) -> count.addAndGet(requestFutures.size()));
		}
		return count.get();
	}

	/**
	 * This kills all currently queued requests.
	 *
	 * @return The number of requests killed.
	 */
	public static int killAllRequests() {
		final int toKill = getIncompleteRequestCount();
		//We are ignoring the initialExecutor because those requests haven't been ratelimited (yet)
		synchronized (requestServices) {
			requestServices.keySet().parallelStream().distinct().forEach(bucket -> {
				requestServices.get(bucket).shutdownNow();
				requestServices.remove(bucket);
			});
		}
		synchronized (requests) {
			requests.values().forEach(futures -> futures.forEach(future -> future.cancel(true)));
			requests.clear();
		}
		return toKill;
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
		 * @throws RateLimitException
		 */
		T request() throws RateLimitException;
		
		/**
		 * This is called when this request is retried. This should NOT block.
		 *
		 * @param requestFuture The future managing this request.
		 */
		default void onRetry(RequestFuture<T> requestFuture) {}
	}

	/**
	 * This is used to model a request which returns nothing, NOTE: it IS a functional interface so you are encouraged
	 * to use lambdas!
	 */
	@FunctionalInterface
	public interface IVoidRequest extends IRequest<Void> {

		default Void request() throws RateLimitException {
			doRequest();
			return null;
		}

		/**
		 * This is called when the request is attempted.
		 *
		 * @throws RateLimitException
		 */
		void doRequest() throws RateLimitException;
	}

	/**
	 * This represents a future request which may or may not have been executed at the time of construction.
	 *
	 * @param <T> The request return type.
	 */
	public static class RequestFuture<T> implements Future<T>, Delayed {

		private volatile boolean isDone = false;
		private volatile boolean cancelled = false;
		private volatile boolean firstAttempt = true;
		private volatile T value = null;
		private volatile long timeForNextRequest;
		private volatile String bucket;
		private final IRequest<T> request;
		private final CountDownLatch latch = new CountDownLatch(1);

		RequestFuture(IRequest<T> request) {
			this.request = request;
		}

		/**
		 * Cancels the request if it hasn't been executed.
		 *
		 * @param mayInterruptIfRunning Whether the future should be cancelled regardless of whether its running or not.
		 * @return True if cancelled, false if otherwise (like if the request was already executed).
		 */
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if (!isDone()) {
				if ((latch.getCount() != 0 && mayInterruptIfRunning) || latch.getCount() == 0) {
					latch.countDown();
					cancelled = true;
				}
			}
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
		 * Gets the request return value or blocks until the request is completed.
		 *
		 * @return The value.
		 */
		@Override
		public T get() {
			if (!isDone() && !isCancelled()) {
				try {
					latch.await();
				} catch (InterruptedException e) {
					Discord4J.LOGGER.error(LogMarkers.UTIL, "RequestFuture unexpectedly interrupted!", e);
				}
			}
			
			return value;
		}

		/**
		 * Gets the request return value if present, otherwise it blocks until the request is completed or the timeout
		 * is reached.
		 *
		 * @param timeout The timeout value.
		 * @param unit The timeout unit.
		 * @return The value.
		 *
		 * @throws InterruptedException
		 * @throws ExecutionException
		 * @throws TimeoutException
		 */
		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			if (!isDone() && !isCancelled())
				if (!latch.await(timeout, unit))
					throw new TimeoutException();
			
			return value;
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
		 * Gets the bucket this request was ratelimited for.
		 *
		 * @return The bucket.
		 */
		public String getBucket() {
			return bucket;
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
		boolean tryAgain() {
			try {
				if (!firstAttempt)
					request.onRetry(this);
				
				if (!isCancelled()) {
					value = request.request();
					timeForNextRequest = -1;
					isDone = true;
					latch.countDown();
				}
			} catch (RateLimitException e) {
				firstAttempt = false;
				timeForNextRequest = System.currentTimeMillis()+e.getRetryDelay();
				bucket = e.getMethod();
			}
			return isDone() || isCancelled();
		}
	}

	/**
	 * Manages request futures to ensure it executes the request eventually.
	 */
	private static class RequestRunnable implements Runnable {

		private final String bucket;

		private RequestRunnable(String bucket) {
			this.bucket = bucket;
		}

		@Override
		public void run() {
			synchronized (requests) {
				List<RequestFuture> futures = requests.get(bucket);
				
				if (futures != null) {
					List<RequestFuture> futuresToRetry = new CopyOnWriteArrayList<>();
					
					futures.forEach((RequestFuture future) -> {
						if (!future.tryAgain()) {
							futuresToRetry.add(future);
						}
					});
					
					if (futuresToRetry.size() > 0) {
						long delay = Math.max(0, futuresToRetry.get(0).getDelay(TimeUnit.MILLISECONDS));
						requests.replace(bucket, futuresToRetry);
						synchronized (requestServices) {
							requestServices.get(bucket).schedule(new RequestRunnable(bucket), delay, TimeUnit.MILLISECONDS);
						}
					} else {
						requests.remove(bucket);
						requestServices.remove(bucket).shutdownNow();
					}
				}
			}
		}
	}
}
