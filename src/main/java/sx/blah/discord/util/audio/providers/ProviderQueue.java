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

package sx.blah.discord.util.audio.providers;

import sx.blah.discord.handle.audio.AudioEncodingType;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.audio.impl.DefaultProvider;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An audio provider which allows for audio providers to be queued one after another.
 *
 * <p>As soon as a provider cannot provided data ({@link IAudioProvider#isReady()} returns false), the next provider in
 * the queue is used.
 */
public class ProviderQueue implements IAudioProvider {

	/**
	 * The queue of audio providers.
	 */
	private final CopyOnWriteArrayList<IAudioProvider> providers = new CopyOnWriteArrayList<>();

	public ProviderQueue() {

	}

	public ProviderQueue(List<IAudioProvider> initialProviders) {
		this();
		providers.addAll(initialProviders);
	}

	/**
	 * Adds a provider to the internal provider queue.
	 *
	 * @param provider The provider to add.
	 * @return The index of the provider.
	 */
	public synchronized int add(IAudioProvider provider) {
		providers.add(provider);
		return providers.indexOf(provider);
	}

	/**
	 * Removes the provider from the internal provider queue at the given index.
	 *
	 * @param index The index of the provider to remove.
	 * @return The provider removed.
	 */
	public synchronized IAudioProvider remove(int index) {
		return providers.remove(index);
	}

	/**
	 * Removes the given provider from the internal provider queue.
	 *
	 * @param provider The provider to remove.
	 */
	public synchronized void remove(IAudioProvider provider) {
		providers.remove(provider);
	}

	/**
	 * Gets the position of the given provider in the provider queue.
	 *
	 * @param provider The provider.
	 * @return The provider's index.
	 */
	public synchronized int indexOf(IAudioProvider provider) {
		return providers.indexOf(provider);
	}

	/**
	 * Gets the provider at the given index.
	 *
	 * @param index The index to get the provider from.
	 * @return The provider.
	 */
	public synchronized IAudioProvider get(int index) {
		return providers.get(index);
	}

	/**
	 * Gets the size of the provider queue.
	 *
	 * @return The size of the provider queue.
	 */
	public synchronized int size() {
		return providers.size();
	}

	/**
	 * Sets the provider in the given position.
	 *
	 * @param index The index to set the provider at.
	 * @param provider The provider to set.
	 */
	public synchronized void set(int index, IAudioProvider provider) {
		providers.set(index, provider);
	}

	@Override
	public synchronized boolean isReady() {
		boolean isReady = false;
		for (IAudioProvider provider : providers) {
			if (provider.isReady()) {
				isReady = true;
				break;
			}
		}

		return providers.size() > 0 && isReady;
	}

	/**
	 * Gets the first provider in the queue that can provide audio.
	 *
	 * @return The first provider in the queue that can provide audio (or {@link DefaultProvider} if there is none).
	 */
	private IAudioProvider findSuitableProvider() {
		IAudioProvider usableProvider = null;
		for (IAudioProvider provider : providers) {
			if (!provider.isReady()) {
				providers.remove(provider); //This provider can't be used, no need to save it anymore
			} else {
				usableProvider = provider;
				break;
			}
		}

		if (usableProvider == null)
			usableProvider = new DefaultProvider(); //No provider found, use a NO-OP one.

		return usableProvider;
	}

	@Override
	public synchronized byte[] provide() {
		return findSuitableProvider().provide();
	}

	@Override
	public synchronized int getChannels() {
		return findSuitableProvider().getChannels();
	}

	@Override
	public synchronized AudioEncodingType getAudioEncodingType() {
		return findSuitableProvider().getAudioEncodingType();
	}
}
