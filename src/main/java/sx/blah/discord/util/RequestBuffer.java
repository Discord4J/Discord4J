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
import sx.blah.discord.api.internal.DiscordUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

/**
 * A utility class intended to deal with {@link RateLimitException}s by queueing rate-limited operations until they can
 * be completed.
 */
public class RequestBuffer {

	private static final ExecutorService initialExecutor = Executors.newFixedThreadPool(2, DiscordUtils.createDaemonThreadFactory("RequestBuffer Initial Executor"));
	private static final Map<String, ScheduledExecutorService> requestServices = new ConcurrentHashMap<>();
	private static final Map<String, List<RequestFuture>> requests = new ConcurrentHashMap<>();

	/**
	 * Queues a request.
	 *
	 * @param request The request to be carried out.
	 * @param <T> The type of the object returned by the request.
	 * @return The result of the request.
	 */
	public static <T> RequestFuture<T> request(IRequest<T> request) {
		final RequestFuture<T> future = new RequestFuture<>(request);
		initialExecutor.execute(() -> {
			try {
				future.run();
				if (future.callable.rateLimited && future.getDelay(TimeUnit.MILLISECONDS) >= 0) {
					Discord4J.LOGGER.debug(LogMarkers.UTIL, "Attempted request rate-limited, queueing retry in {}ms",
							future.getDelay(TimeUnit.MILLISECONDS));

					if (future.getBucket() != null) {
						synchronized (requests) {
							if (future.getBucket() != null) {
								if (!requests.containsKey(future.getBucket())) {
									requests.put(future.getBucket(), new CopyOnWriteArrayList<>());
									requestServices.put(future.getBucket(), Executors.newSingleThreadScheduledExecutor(DiscordUtils.createDaemonThreadFactory("RequestBuffer Retry Handler")));
									requestServices.get(future.getBucket()).schedule(new RequestRunnable(future.getBucket()), future.getDelay(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
								}

								requests.get(future.getBucket()).add(future);
							}
						}
					}
				}
			} catch (Exception e) {
				Discord4J.LOGGER.error(LogMarkers.UTIL, "Exception caught while attempting to execute a request", e);
			}
		});
		return future;
	}

	/**
	 * Queues a request.
	 *
	 * @param request The request to be carried out.
	 * @return The result of the request.
	 */
	public static RequestFuture<Void> request(IVoidRequest request) {
		return request(() -> {
			request.doRequest();
			return null;
		});
	}

	/**
	 * Gets the number of incomplete requests in the queue.
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
	 * Kills all currently queued requests.
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
	 * A request to be carried out by the buffer.
	 *
	 * @param <T> The type of the object returned by the request.
	 */
	@FunctionalInterface
	public interface IRequest<T> {

		/**
		 * Attempts the request.
		 *
		 * @return The result of this request.
		 */
		T request();

		/**
		 * Retries the request. This should NOT block the thread.
		 *
		 * @param requestFuture The future managing this request.
		 */
		default void onRetry(RequestFuture<T> requestFuture) {}
	}

	/**
	 * A request to be carried out by the buffer that has no result.
	 */
	@FunctionalInterface
	public interface IVoidRequest {

		/**
		 * Attempts the request.
		 */
		void doRequest();
	}

	/**
	 * A future that controls the execution of a request.
	 * @param <T> The type of the object the request returns.
	 */
	public static class RequestFuture<T> implements Future<T>, Delayed {

		private final IRequest<T> request;
		private final RequestCallable<T> callable;
		private volatile FutureTask<T> backing;
		final StampedLock lock = new StampedLock();

		RequestFuture(IRequest<T> request) {
			this.request = request;
			this.callable = new RequestCallable<>(request, this);
			backing = new FutureTask<>(callable);
		}

		/**
		 * Gets the delay until the next request is attempted.
		 *
		 * @param unit The time unit for the delay.
		 * @return The delay until the next request is attempted.
		 */
		@Override
		public long getDelay(TimeUnit unit) {
			if (isDone() || isCancelled())
				return 0;

			return unit.convert(callable.timeForNextRequest-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

		/**
		 * Gets the bucket the request was ratelimited for.
		 *
		 * @return The bucket the request was ratelimited for.
		 */
		public String getBucket() {
			return callable.bucket;
		}

		@Override
		public int compareTo(Delayed o) {
			return (int) (getDelay(TimeUnit.MILLISECONDS)-o.getDelay(TimeUnit.MILLISECONDS));
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return backing.cancel(mayInterruptIfRunning);
		}

		@Override
		public boolean isCancelled() {
			return backing.isCancelled();
		}

		@Override
		public boolean isDone() {
			return backing.isDone() && !callable.rateLimited;
		}

		@Override
		public T get() {
			long stamp = lock.readLock();
			try {
				return backing.get();
			} catch (Exception e) {
				Discord4J.LOGGER.error(LogMarkers.UTIL, "Exception caught attempting to handle a ratelimited request", e);
			} finally {
				lock.unlockRead(stamp);
			}

			return null;
		}

		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
			long timeoutTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timeout, unit);
			long stamp = lock.tryReadLock(timeout, unit);

			if (!lock.validate(stamp)) {
				if (System.currentTimeMillis() > timeoutTime)
					throw new TimeoutException();

				if (isCancelled())
					throw new InterruptedException();
			}
			try {
				return backing.get();
			} catch (Exception e) {
				Discord4J.LOGGER.error(LogMarkers.UTIL, "Exception caught attempting to handle a ratelimited request", e);
			} finally {
				lock.unlockRead(stamp);
			}

			return null;
		}

		/**
		 * Wrapper for the backing {@link FutureTask}'s run method.
		 */
		private void run() {
			backing.run();
		}

		private static class RequestCallable <T> implements Callable<T> {

			final IRequest<T> request;
			final RequestFuture<T> future;
			final long stamp;

			volatile boolean firstAttempt = true;
			volatile long timeForNextRequest = -1;
			volatile String bucket = null;
			volatile boolean rateLimited = false;

			RequestCallable(IRequest<T> request, RequestFuture<T> future) {
				this.request = request;
				this.future = future;
				stamp = future.lock.writeLock();
			}

			@Override
			public T call() {
				try {
					if (!firstAttempt)
						request.onRetry(future);

					if (!future.isCancelled()) {
						T value = request.request();
						timeForNextRequest = -1;
						rateLimited = false;
						future.lock.unlockWrite(stamp);
						return value;
					}
				} catch (RateLimitException e) {
					firstAttempt = false;
					timeForNextRequest = System.currentTimeMillis()+e.getRetryDelay();
					bucket = e.getMethod();
					rateLimited = true;
				} catch (Exception e) {
					Discord4J.LOGGER.warn(LogMarkers.UTIL, "RequestBuffer handled an uncaught exception!", e);
				}

				if (!rateLimited && future.lock.validate(stamp))
					future.lock.unlockWrite(stamp);

				return null;
			}
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
				try {
					List<RequestFuture> futures = requests.get(bucket);

					if (futures != null) {
						List<RequestFuture> futuresToRetry = new CopyOnWriteArrayList<>();

						futures.forEach((RequestFuture future) -> {
							try {
								if (!future.isCancelled()) {
									future.run();
									if (future.callable.rateLimited) {
										future.backing = new FutureTask<>(future.callable);
										futuresToRetry.add(future);
									}
								}
							} catch (Exception e) {
								Discord4J.LOGGER.error(LogMarkers.UTIL, "Exception caught while attempting to execute a request", e);
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
				} catch (Exception e) {
					Discord4J.LOGGER.error(LogMarkers.UTIL, "Exception caught while attempting to retry requests", e);
				}
			}
		}
	}
}
