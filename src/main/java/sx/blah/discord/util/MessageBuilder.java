/*
 * Discord4J - Unofficial wrapper for Discord API
 * Copyright (c) 2015
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package sx.blah.discord.util;

import sx.blah.discord.Discord4J;
import sx.blah.discord.DiscordClient;
import sx.blah.discord.handle.obj.Channel;

/**
 * @author x
 * @since 10/2/2015
 * <p>
 * Utility class designed to make message sending easier.
 */
public class MessageBuilder {
	private String content = "";
	private String channelID;

    /**
     * Sets the content of the message.
     * @param content
     * @return
     */
	public MessageBuilder withContent(String content) {
		this.content = content;
		return this;
	}

    /**
     * Sets the content of the message with a given style.
     * @param content
     * @param styles
     * @return
     */
    public MessageBuilder withContent(String content, Styles styles) {
		this.content = styles.getMarkdown() + content + styles.getReverseMarkdown();
		return this;
	}

    /**
     * Appends extra text to the current content.
     * @param content
     * @return
     */
    public MessageBuilder appendContent(String content) {
        this.content += content;
        return this;
    }

    /**
     * Appends extra text to the current content with given style.
     * @param content
     * @param styles
     * @return
     */
    public MessageBuilder appendContent(String content, Styles styles) {
        this.content += (styles.getMarkdown() + content + styles.getReverseMarkdown());
        return this;
    }

    /**
     * Sets the channel that the message should go to.
     * @param channelID
     * @return
     */
	public MessageBuilder withChannel(String channelID) {
		this.channelID = channelID;
		return this;
	}

    /**
     * Sets the channel that the message should go to.
     * @param channel
     * @return
     */
	public MessageBuilder withChannel(Channel channel) {
		this.channelID = channel.getID();
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
				DiscordClient.get().sendMessage(content, channelID);
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

    /**
     * Enum describing Markdown formatting that can be used in chat.
     */
    public enum Styles {
        ITALICS("*"),
        BOLD("**"),
        BOLD_ITALICS("***"),
        STRIKEOUT("~~"),
        CODE("```"),
        UNDERLINE("__"),
        UNDERLINE_ITALICS("__*"),
        UNDERLINE_BOLD("__**"),
        UNDERLINE_BOLD_ITALICS("__***");

        final String markdown, reverseMarkdown;
        Styles(String markdown) {
            this.markdown = markdown;
            this.reverseMarkdown = new StringBuilder(markdown).reverse().toString();
        }

        public String getMarkdown() {
            return markdown;
        }

        public String getReverseMarkdown() {
            return reverseMarkdown;
        }
    }
}
