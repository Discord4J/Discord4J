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
 * This represents the response received from discord regarding the server's status
 */
public class StatusResponse {

	/**
	 * The page for this url.
	 */
	public PageResponse page;

	/**
	 * The scheduled maintenances
	 */
	public MaintenanceResponse[] scheduled_maintenances;

	/**
	 * Represents the "page" object.
	 */
	public static class PageResponse {

		/**
		 * The page id.
		 */
		public String id;

		/**
		 * The page's name.
		 */
		public String name;

		/**
		 * The page's base url.
		 */
		public String url;

		/**
		 * When this page was last updated.
		 */
		public String updated_at;
	}

	/**
	 * The maintenance response
	 */
	public static class MaintenanceResponse {

		/**
		 * Name of the maintenance.
		 */
		public String name;

		/**
		 * The status of the maintenance
		 * Known values: "scheduled", "investigating", "identified", "update", "monitoring", "resolved"
		 */
		public String status;

		/**
		 * When the maintenance was created
		 */
		public String created_at;

		/**
		 * When the maintenance was updated
		 */
		public String updated_at;

		/**
		 * When the maintenance was monitored to ensure it is alright
		 */
		public String monitoring_at;

		/**
		 * When the maintenance was resolved
		 */
		public String resolved_at;

		/**
		 * The shortened url leading to the maintenance page
		 */
		public String shortlink;

		/**
		 * When the maintenance will start
		 */
		public String scheduled_for;

		/**
		 * When the maintenance will end
		 */
		public String scheduled_until;

		/**
		 * The maintenance/incident id
		 */
		public String id;

		/**
		 * The page id for the maintenance
		 */
		public String page_id;

		/**
		 * Updates on the maintenance
		 */
		public IncidentUpdateResponse[] incident_updates;

		/**
		 * The impact the maintenance will have //FIXME ??
		 * Known values: "none" TODO: More
		 */
		public String impact;
	}

	/**
	 * The incident update response
	 */
	public static class IncidentUpdateResponse {

		/**
		 * The status of the incident
		 * Known values: "scheduled", "investigating", "identified", "update", "monitoring", "resolved"
		 */
		public String status;

		/**
		 * The message about the update
		 */
		public String body;

		/**
		 * When the update was created
		 */
		public String created_at;

		/**
		 * When the update was last updated
		 */
		public String updated_at;

		/**
		 * When this would be displayed
		 */
		public String display_at;

		/**
		 * The update id
		 */
		public String id;

		/**
		 * The incident id
		 */
		public String incident_id;
	}
}
