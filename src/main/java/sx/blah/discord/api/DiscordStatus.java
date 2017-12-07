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

package sx.blah.discord.api;

import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.Requests;
import sx.blah.discord.api.internal.json.responses.metrics.MetricsResponse;
import sx.blah.discord.api.internal.json.responses.metrics.StatusResponse;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

import java.time.Instant;

/**
 * Utility class used for fetching status information about the Discord API.
 */
public class DiscordStatus {

	/**
	 * Fetches the mean discord api response time for today.
	 *
	 * @return The mean response time (in milliseconds).
	 */
	public static double getAPIResponseTimeForDay() {
		MetricsResponse response = Requests.GENERAL_REQUESTS.GET.makeRequest(
				String.format(DiscordEndpoints.METRICS, "day"), MetricsResponse.class);

		return response.summary.mean;
	}

	/**
	 * Fetches the mean discord api response time for this week.
	 *
	 * @return The mean response time (in milliseconds).
	 */
	public static double getAPIResponseTimeForWeek() {
		MetricsResponse response = Requests.GENERAL_REQUESTS.GET.makeRequest(
				String.format(DiscordEndpoints.METRICS, "week"), MetricsResponse.class);

		return response.summary.mean;
	}

	/**
	 * Fetches the mean discord api response time for this month.
	 *
	 * @return The mean response time (in milliseconds).
	 */
	public static double getAPIResponseTimeForMonth() {
		MetricsResponse response = Requests.GENERAL_REQUESTS.GET.makeRequest(
				String.format(DiscordEndpoints.METRICS, "month"), MetricsResponse.class);

		return response.summary.mean;
	}

	/**
	 * Gets the active maintenance statuses.
	 *
	 * @return The maintenance statuses.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	public static Maintenance[] getActiveMaintenances() {
		StatusResponse response = Requests.GENERAL_REQUESTS.GET.makeRequest(
				String.format(DiscordEndpoints.STATUS, "active"), StatusResponse.class);

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
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	public static Maintenance[] getUpcomingMaintenances() {
		StatusResponse response = Requests.GENERAL_REQUESTS.GET.makeRequest(
				String.format(DiscordEndpoints.STATUS, "upcoming"), StatusResponse.class);

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
		private final Instant start, stop;

		protected Maintenance(String name, String description, String id, Instant start, Instant stop) {
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
		public Instant getStart() {
			return start;
		}

		/**
		 * Gets when the maintenance is scheduled to end.
		 *
		 * @return The end time.
		 */
		public Instant getEnd() {
			return stop;
		}
	}
}
