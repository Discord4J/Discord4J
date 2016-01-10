package sx.blah.discord.json.responses;

/**
 * This is returned by discord when requesting API metrics.
 */
public class MetricResponse {
	
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
	
	public class PeriodResponse {
		/**
		 * The amount of data collected
		 */
		public int count;
		/**
		 * The identifier, either "day", "week" or "month"
		 */
		public String identifier;
		//FIXME: ??
		public int interval;
	}
	
	public class SummaryResponse {
		/**
		 * The mean response time in ms.
		 */
		public double mean;
		/**
		 * The sum of all the response times.
		 */
		public double sum;
	}
	
	public class MetricsOuterResponse {
		
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
	
	public class DataResponse {
		
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
	public class MetricInnerResponse {
		
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
