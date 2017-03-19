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

package sx.blah.discord.api.internal.json.responses.metrics;

/**
 * This is returned by discord when requesting API metrics.
 */
public class MetricsResponse {
	/**
	 * The metrics
	 */
	public MetricsOuterResponse[] metrics;

	/**
	 * The period the metrics is for
	 */
	public PeriodResponse period;

	/**
	 * The summary of the metrics
	 */
	public SummaryResponse summary;

	public static class PeriodResponse {
		/**
		 * The amount of data collected
		 */
		public int count;
		/**
		 * The identifier, either "day", "week" or "month"
		 */
		public String identifier;

		public int interval;
	}

	public static class SummaryResponse {
		/**
		 * The mean response time in ms.
		 */
		public double mean;
		/**
		 * The sum of all the response times.
		 */
		public double sum;
	}

	public static class MetricsOuterResponse {

		/**
		 * The actual data
		 */
		public DataResponse[] data;

		/**
		 * The information about the metric
		 */
		public MetricInnerResponse metric;

		/**
		 * THe metric summary
		 */
		public SummaryResponse summary;
	}

	public static class DataResponse {

		/**
		 * The time (in epoch milliseconds) the metric was taken
		 */
		public long timestamp;

		/**
		 * The time (in milliseconds) the api took to respond
		 */
		public long value;
	}

	/**
	 * statuspage.io information
	 */
	public static class MetricInnerResponse {

		public String application_id;
		public String application_name;
		public int backfill_percentage;
		public boolean backfilled;
		public String created_at;
		public String id;
		public String last_fetched_at;
		public String metric_identifier;
		public String metrics_display_id;
		public String metrics_provider_id;
		public String most_recent_data_at;
		public String name;
		public String updated_at;
	}
}
