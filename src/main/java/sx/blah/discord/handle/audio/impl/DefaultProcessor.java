package sx.blah.discord.handle.audio.impl;

import sx.blah.discord.handle.audio.IAudioProcessor;
import sx.blah.discord.handle.audio.IAudioProvider;

/**
 * This is a processor which just relays provider data.
 */
public class DefaultProcessor implements IAudioProcessor {

	private volatile IAudioProvider provider = new DefaultProvider();

	@Override
	public boolean setProvider(IAudioProvider provider) {
		this.provider = provider;
		return true;
	}

	@Override
	public boolean isReady() {
		return provider.isReady();
	}

	@Override
	public byte[] provide() {
		return provider.provide();
	}

	@Override
	public int getChannels() {
		return provider.getChannels();
	}

	@Override
	public AudioEncodingType getAudioEncodingType() {
		return provider.getAudioEncodingType();
	}
}
