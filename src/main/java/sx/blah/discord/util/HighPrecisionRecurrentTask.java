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

import java.util.concurrent.locks.LockSupport;

/**
 * Custom Thread that executes the passed task every specified period.
 *
 * Time of execution is held in account when calculating the time to sleep until the next execution.
 */
public class HighPrecisionRecurrentTask extends Thread {

	private final int periodInNanos;
	private final int spinningTimeNanos;
	private final Runnable target;
	private volatile boolean stop;

	/**
	 * @param periodInMilis The period in milliseconds of the task execution.
	 * @param percentageOfSleepSpinning The percentage of total sleeping that should be done spinning to increase accuracy.
	 * @param target The task to run.
	 */
	public HighPrecisionRecurrentTask(int periodInMilis, float percentageOfSleepSpinning, Runnable target) {
		super("D4J AudioThread");
		if (percentageOfSleepSpinning < 0 || percentageOfSleepSpinning > 1) throw new IllegalArgumentException("percentageOfSleepSpinning < 0 | percentageOfSleepSpinning > 1");
		this.periodInNanos = periodInMilis * 1_000_000;
		this.spinningTimeNanos = (int) (periodInNanos * percentageOfSleepSpinning);
		this.target = target;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	@Override
	@SuppressWarnings(value = "empty-statement")
	public void run() {
		long nextTarget = 0;
		while (!stop) {
			long now = System.nanoTime();
			target.run();
			long total = System.nanoTime() - now;
			nextTarget = now + periodInNanos - total;
			sleepFor(periodInNanos - total - spinningTimeNanos);
			while (nextTarget > System.nanoTime()) {
				; //consume cycles
			}
		}
	}

	private void sleepFor(long nanos) {
		if (nanos > 0) {
			long elapsed = 0;
			while (elapsed < nanos) {
				long t0 = System.nanoTime();
				LockSupport.parkNanos(nanos - elapsed);
				elapsed += System.nanoTime() - t0;
			}
		}
	}

}
