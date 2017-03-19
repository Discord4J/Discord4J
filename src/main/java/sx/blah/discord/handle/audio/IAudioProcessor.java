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

/**
 * This represents an audio processor (something that manipulates raw audio data before it is sent to discord).
 */
public interface IAudioProcessor extends IAudioProvider {

	/**
	 * This sets the {@link IAudioProvider} this processor is wrapping and returns whether this processor is compatible
	 * with the provided provider instance.
	 *
	 * @param provider The provider to use. If a previous provider was wrapped, it should be replaced by this object.
	 * @return True if compatible, false if otherwise (in this case the processor will not be called to retrieve audio
	 * data.
	 */
	boolean setProvider(IAudioProvider provider);
}
