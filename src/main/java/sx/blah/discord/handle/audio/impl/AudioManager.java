package sx.blah.discord.handle.audio.impl;

import com.sun.jna.ptr.PointerByReference;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.Opus;
import sx.blah.discord.api.internal.OpusUtil;
import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.audio.IAudioProcessor;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.Lazy;

public class AudioManager implements IAudioManager {

	private final IGuild guild;
	private final IDiscordClient client;
	private volatile IAudioProvider provider = new DefaultProvider();
	private volatile IAudioProcessor processor = new DefaultProcessor();
	private volatile boolean useProcessor = true;

	private final Lazy<PointerByReference> monoEncoder = new Lazy<>(() -> OpusUtil.newEncoder(1));
	private final Lazy<PointerByReference> stereoEncoder = new Lazy<>(() -> OpusUtil.newEncoder(2));
	private final Lazy<PointerByReference> stereoDecoder = new Lazy<>(() -> OpusUtil.newDecoder(2));

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
		if (provider.isReady() && !Discord4J.audioDisabled.get()) {
			IAudioProvider.AudioEncodingType type = provider.getAudioEncodingType();
			int channels = provider.getChannels();
			byte[] data = provider.provide();
			if (data == null)
				data = new byte[0];

			if (type != IAudioProvider.AudioEncodingType.OPUS) {
				data = OpusUtil.encode(channels == 1 ? monoEncoder.get() : stereoEncoder.get(), data);
			}

			return data;
		}
		return new byte[0];
	}
}
