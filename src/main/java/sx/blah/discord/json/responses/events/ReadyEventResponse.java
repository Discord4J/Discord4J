package sx.blah.discord.json.responses.events;

import sx.blah.discord.json.responses.GuildResponse;
import sx.blah.discord.json.responses.PrivateChannelResponse;
import sx.blah.discord.json.responses.UserResponse;

public class ReadyEventResponse {

	/**
	 * Should be 4, version of the gateway
	 */
	public int v;

	/**
	 * The user's discord client settings
	 */
	public UserSettingsResponse user_settings;

	/**
	 * The user's settings for each guild
	 */
	public UserGuildSettingsResponse[] user_guild_settings;

	/**
	 * The user this event is for
	 */
	public UserResponse user;

	/**
	 * The unique id for the session
	 */
	public String session_id;

	/**
	 * The private channels the user is connected to
	 */
	public PrivateChannelResponse[] private_channels;

	/**
	 * How long to wait before refreshing statuses
	 */
	public long heartbeat_interval;

	/**
	 * The guilds the user is a part of
	 */
	public GuildResponse[] guilds;

	/**
	 * Represents a user's client settings
	 */
	public static class UserSettingsResponse {

		/**
		 * Currently either "light" or "dark"
		 */
		public String theme;

		/**
		 * Whether to display the current game
		 */
		public boolean show_current_game;

		/**
		 * Whether to render embed data
		 */
		public boolean render_embeds;

		/**
		 * The muted channels
		 */
		public String[] muted_channels;

		/**
		 * Whether to use the compact chat room format
		 */
		public boolean message_display_compact;

		/**
		 * The locale for the user, i.e. "en-US"
		 */
		public String locale;

		/**
		 * Whether to embed media inline with a message
		 */
		public boolean inline_embed_media;

		/**
		 * Whether to embed attachments inline with a message
		 */
		public boolean inline_attachment_media;

		/**
		 * Whether to enable tts
		 */
		public boolean enable_tts_command;

		/**
		 * Whether to convert ascii emoticons into emoji characters
		 */
		public boolean convert_emoticons;
	}

	/**
	 * Represents a user's settings for a guild
	 */
	public static class UserGuildSettingsResponse {

		/**
		 * Whether to suppress @everyone notifications
		 */
		public boolean suppress_everyone;

		/**
		 * Whether the guild is muted
		 */
		public boolean muted;

		/**
		 * Whether to push notifications from this guild to mobile
		 */
		public boolean mobile_push;

		/**
		 * The type of notifications to receive
		 * 0 = All
		 * 1 = Mentions
		 * 2 = Nothing
		 * 3 = Muted
		 */
		public int message_notifications;

		/**
		 * The Guild's id
		 */
		public String guild_id;

		/**
		 * The specific channel overrides to the guild settings
		 */
		public ChannelOverrideResponse[] channel_overrides;

		/**
		 * A specific channel override setting
		 */
		public class ChannelOverrideResponse {

			/**
			 * Whether the channel is muted
			 */
			public boolean muted;

			/**
			 * The type of notifications to receive
			 * 0 = All
			 * 1 = Mentions
			 * 2 = Nothing
			 * 3 = Muted
			 */
			public int message_notifications;

			/**
			 * The channel's id
			 */
			public String channel_id;
		}
	}
}
