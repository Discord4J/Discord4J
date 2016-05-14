package sx.blah.discord.handle.audio.impl;

import sx.blah.discord.handle.audio.IAudioProvider;

/**
 * This is a NO-OP audio provider.
 */
public class DefaultProvider implements IAudioProvider {

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public byte[] provide() {
		return new byte[0];
	}

	@Override
	public int getChannels() {
		return 0;
	}

	@Override
	public AudioEncodingType getAudioEncodingType() {
		return AudioEncodingType.OPUS; //OPUS to ensure no processing is done to it.
	}
}
