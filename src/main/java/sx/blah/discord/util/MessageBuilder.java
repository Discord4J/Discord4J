// Discord4J - Unofficial wrapper for Discord API
// Copyright (c) 2015
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

package sx.blah.discord.util;

import sx.blah.discord.DiscordClient;
import sx.blah.discord.handle.obj.Channel;
import sx.blah.discord.handle.obj.User;

/**
 * @author x
 * @since 10/2/2015
 * <p>
 * Utility class designed to make message sending easier.
 */
public class MessageBuilder {
	private String content;
	private String[] mentionedIDs = new String[]{};
	private String channelID;

	public MessageBuilder withContent(String content) {
		this.content = content;
		return this;
	}

	public MessageBuilder withMentions(String... mentionedIDs) {
		this.mentionedIDs = mentionedIDs;
		return this;
	}

	public MessageBuilder withMentions(User... mentions) {
		String[] mentionedIDs = new String[mentions.length];
		for (int i = 0; i < mentions.length; i++) {
			mentionedIDs[i] = mentions[i].getID();
		}
		this.mentionedIDs = mentionedIDs;

		return this;
	}

	public MessageBuilder withChannel(String channelID) {
		this.channelID = channelID;
		return this;
	}

	public MessageBuilder withChannel(Channel channel) {
		this.channelID = channel.getChannelID();
		return this;
	}

	/**
	 * Galactic law requires I have a build() method in
	 * my builder classes.
	 */
	public void build() {
		if (null == content || null == channelID) {
			throw new RuntimeException("You need content and a channel ID to send a message!");
		} else {
			try {
				DiscordClient.get().sendMessage(content, channelID, mentionedIDs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Alternate name, in case people don't know that build() sends.
	 */
	public void send() {
		build();
	}
}
