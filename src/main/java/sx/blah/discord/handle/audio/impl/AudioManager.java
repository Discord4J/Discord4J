package sx.blah.discord.handle.audio.impl;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.audio.IAudioProcessor;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.audio.OpusUtil;

public class AudioManager implements IAudioManager {

	private final IGuild guild;
	private final IDiscordClient client;
	private volatile IAudioProvider provider = new DefaultProvider();
	private volatile IAudioProcessor processor = new DefaultProcessor();
	private volatile boolean useProcessor = true;

	public AudioManager(IGuild guild) {
		this.guild = guild;
		client = guild.getClient();
	}

	@Override
	public void setAudioProvider(IAudioProvider provider) {
		if (provider == null)
			provider = new DefaultProvider();

		this.provider = provider;
		useProcessor = getAudioProcessor().setProvider(provider);
	}

	@Override
	public IAudioProvider getAudioProvider() {
		return provider;
	}

	@Override
	public void setAudioProcessor(IAudioProcessor processor) {
		if (processor == null)
			processor = new DefaultProcessor();

		this.processor = processor;
		useProcessor = processor.setProvider(getAudioProvider());
	}

	@Override
	public IAudioProcessor getAudioProcessor() {
		return processor;
	}

	@Override
	public byte[] getAudio() { //TODO: Audio padding
		IAudioProcessor processor = getAudioProcessor();
		IAudioProvider provider = useProcessor ? processor : getAudioProvider();

		return getAudioDataForProvider(provider);
	}

	@Override
	public IGuild getGuild() {
		return guild;
	}

	private byte[] getAudioDataForProvider(IAudioProvider provider) {
		if (provider.isReady()) {
			IAudioProvider.AudioEncodingType type = provider.getAudioEncodingType();
			int channels = provider.getChannels();
			byte[] data = provider.provide();

			if (type != IAudioProvider.AudioEncodingType.OPUS) {
				data = OpusUtil.encodeToOpus(data, channels);
			}

			return data;
		}
		return new byte[0];
	}
}
