package sx.blah.discord.util.audio.providers;

import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.audio.impl.DefaultProvider;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The ProviderQueue is an {@link IAudioProvider} implementation which allows for audio providers to be queued so that
 * as soon as a provider cannot provided data (i.e. {@link IAudioProvider#isReady()} returns false) the next provider in
 * the queue is used.
 */
public class ProviderQueue implements IAudioProvider {

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
	 * Removes the provider from the internal provider queue at the provided index.
	 *
	 * @param index The index of the provider to remove.
	 * @return The provider removed.
	 */
	public synchronized IAudioProvider remove(int index) {
		return providers.remove(index);
	}

	/**
	 * Removes the provided provider from the internal provider queue.
	 *
	 * @param provider The provider to remove.
	 */
	public synchronized void remove(IAudioProvider provider) {
		providers.remove(provider);
	}

	/**
	 * Gets the position of the provided provider in the provider queue.
	 *
	 * @param provider The provider.
	 * @return The provider's index.
	 */
	public synchronized int indexOf(IAudioProvider provider) {
		return providers.indexOf(provider);
	}

	/**
	 * Gets the provider at the specified position.
	 *
	 * @param index The position to get the provider from.
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
	 * Inserts a provider in the specified position.
	 *
	 * @param index The position to insert the provider into.
	 * @param provider The provider to insert.
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
