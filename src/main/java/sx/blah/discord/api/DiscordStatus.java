package sx.blah.discord.api;

import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.json.responses.MetricResponse;
import sx.blah.discord.json.responses.StatusResponse;
import sx.blah.discord.util.HTTP403Exception;
import sx.blah.discord.util.Requests;

import java.time.LocalDateTime;

/**
 * This class is used to get general information about the status regarding discord servers
 */
public class DiscordStatus {
	
	/**
	 * Fetches the mean discord api response time for today.
	 *
	 * @return The mean response time (in milliseconds).
	 *
	 * @throws HTTP403Exception
	 */
	public static double getAPIResponseTimeForDay() throws HTTP403Exception {
		MetricResponse response = DiscordUtils.GSON.fromJson(Requests.GET.makeRequest(
				String.format(DiscordEndpoints.METRICS, "day")), MetricResponse.class);
		return response.summary.mean;
	}
	
	/**
	 * Fetches the mean discord api response time for this week.
	 *
	 * @return The mean response time (in milliseconds).
	 *
	 * @throws HTTP403Exception
	 */
	public static double getAPIResponseTimeForWeek() throws HTTP403Exception {
		MetricResponse response = DiscordUtils.GSON.fromJson(Requests.GET.makeRequest(
				String.format(DiscordEndpoints.METRICS, "week")), MetricResponse.class);
		return response.summary.mean;
	}
	
	/**
	 * Fetches the mean discord api response time for this month.
	 *
	 * @return The mean response time (in milliseconds).
	 *
	 * @throws HTTP403Exception
	 */
	public static double getAPIResponseTimeForMonth() throws HTTP403Exception {
		MetricResponse response = DiscordUtils.GSON.fromJson(Requests.GET.makeRequest(
				String.format(DiscordEndpoints.METRICS, "month")), MetricResponse.class);
		return response.summary.mean;
	}
	
	/**
	 * Gets the active maintenance statuses.
	 *
	 * @return The maintenance statuses.
	 *
	 * @throws HTTP403Exception
	 */
	public static Maintenance[] getActiveMaintenances() throws HTTP403Exception {
		StatusResponse response = DiscordUtils.GSON.fromJson(Requests.GET.makeRequest(
				String.format(DiscordEndpoints.STATUS, "active")), StatusResponse.class);
		Maintenance[] maintenances = new Maintenance[response.scheduled_maintenances.length];
		
		for (int i = 0; i < maintenances.length; i++) {
			StatusResponse.MaintenanceResponse maintenanceResponse = response.scheduled_maintenances[i];
			maintenances[i] = new Maintenance(maintenanceResponse.name, maintenanceResponse.incident_updates[0].body,
					maintenanceResponse.id, DiscordUtils.convertFromTimestamp(maintenanceResponse.scheduled_for),
					DiscordUtils.convertFromTimestamp(maintenanceResponse.scheduled_until));
		}
		return maintenances;
	}
	
	/**
	 * Gets the upcoming maintenance statuses.
	 *
	 * @return The maintenance statuses.
	 *
	 * @throws HTTP403Exception
	 */
	public static Maintenance[] getUpcomingMaintenances() throws HTTP403Exception {
		StatusResponse response = DiscordUtils.GSON.fromJson(Requests.GET.makeRequest(
				String.format(DiscordEndpoints.STATUS, "upcoming")), StatusResponse.class);
		Maintenance[] maintenances = new Maintenance[response.scheduled_maintenances.length];
		
		for (int i = 0; i < maintenances.length; i++) {
			StatusResponse.MaintenanceResponse maintenanceResponse = response.scheduled_maintenances[i];
			maintenances[i] = new Maintenance(maintenanceResponse.name, maintenanceResponse.incident_updates[0].body,
					maintenanceResponse.id, DiscordUtils.convertFromTimestamp(maintenanceResponse.scheduled_for.substring(0, 23)),
					DiscordUtils.convertFromTimestamp(maintenanceResponse.scheduled_until.substring(0, 23)));
		}
		return maintenances;
	}
	
	/**
	 * This object represents a scheduled maintenance
	 */
	public static class Maintenance {
		
		private final String name, description, id;
		private final LocalDateTime start, stop;
		
		protected Maintenance(String name, String description, String id, LocalDateTime start, LocalDateTime stop) {
			this.name = name;
			this.description = description;
			this.id = id;
			this.start = start;
			this.stop = stop;
		}
		
		/**
		 * Gets the name of the maintenance.
		 *
		 * @return The name.
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Gets the maintenance description.
		 *
		 * @return The description.
		 */
		public String getDescription() {
			return description;
		}
		
		/**
		 * Gets the maintenance id.
		 *
		 * @return The id.
		 */
		public String getID() {
			return id;
		}
		
		/**
		 * Gets when the maintenance is scheduled to start.
		 *
		 * @return The start time.
		 */
		public LocalDateTime getStart() {
			return start;
		}
		
		/**
		 * Gets when the maintenance is scheduled to end.
		 *
		 * @return The end time.
		 */
		public LocalDateTime getEnd() {
			return stop;
		}
	}
}
