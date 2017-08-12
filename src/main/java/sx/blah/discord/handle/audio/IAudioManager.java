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

package sx.blah.discord.handle.audio;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * Manages the front-facing portion of audio sending and receiving for a guild.
 */
public interface IAudioManager {

	/**
	 * Sets the audio provider from which audio will be pulled and sent to Discord.
	 *
	 * @param provider The audio provider from which audio will be pulled and sent to Discord.
	 */
	void setAudioProvider(IAudioProvider provider);

	/**
	 * Gets the current audio provider.
	 *
	 * @return The current audio provider.
	 */
	IAudioProvider getAudioProvider();

	/**
	 * Sets the audio processor which processes/manipulates audio data before it is sent to Discord.
	 *
	 * @param processor The audio processor which processes/manipulates audio data before it is sent to Discord.
	 */
	void setAudioProcessor(IAudioProcessor processor);

	/**
	 * Gets the current audio processor.
	 *
	 * @return The current audio processor.
	 */
	IAudioProcessor getAudioProcessor();

	/**
	 * Subscribes an {@link IAudioReceiver} to receive audio from all sources regardless of user.
	 * The {@link IAudioReceiver#receive(byte[], IUser, char, int)} method of every receiver is called every 20ms that
	 * a user is speaking.
	 *
	 * @param receiver The receiver to subscribe.
	 */
	void subscribeReceiver(IAudioReceiver receiver);

	/**
	 * Subscribes an {@link IAudioReceiver} to receive audio from a specific user.
	 * The {@link IAudioReceiver#receive(byte[], IUser, char, int)} method of every receiver is called every 20ms that
	 * the user is speaking.
	 *
	 * @param receiver The receiver to subscribe.
	 * @param user The user to receive audio from.
	 */
	void subscribeReceiver(IAudioReceiver receiver, IUser user);

	/**
	 * Removes the given receiver instance from the list of subscribed receivers. This affects both general and user
	 * receivers.
	 *
	 * @param receiver The receiver to unsubscribe.
	 */
	void unsubscribeReceiver(IAudioReceiver receiver);

	/**
	 * Gets the parent guild of the audio manager.
	 *
	 * @return The parent guild of the audio manager.
	 */
	IGuild getGuild();
}
