package sx.blah.discord.handle.audio.impl;

import com.sun.jna.ptr.PointerByReference;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.Opus;
import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.audio.IAudioProcessor;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.LogMarkers;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ConcurrentHashMap;

public class AudioManager implements IAudioManager {

	public static final int OPUS_SAMPLE_RATE = 48000;   //(Hz) We want to use the highest of qualities! All the bandwidth!
	public static final int OPUS_FRAME_SIZE = 960;
	public static final int OPUS_FRAME_TIME_AMOUNT = OPUS_FRAME_SIZE*1000/OPUS_SAMPLE_RATE;
	public static final int OPUS_MONO_CHANNEL_COUNT = 1;
	public static final int OPUS_STEREO_CHANNEL_COUNT = 2;

	private final ConcurrentHashMap<Integer, PointerByReference> encoders = new ConcurrentHashMap<>();

	private final IGuild guild;
	private final IDiscordClient client;
	private volatile IAudioProvider provider = new DefaultProvider();
	private volatile IAudioProcessor processor = new DefaultProcessor();
	private volatile boolean useProcessor = true;

	public AudioManager(IGuild guild) {
		this.guild = guild;
		client = guild.getClient();

		if (!Discord4J.audioDisabled.get()) {
			//Creates encoders for the 2 most common channel counts. The rest are uncommon enough that it's better to use lazy initialization for them.
			getEncoderForChannels(1);
			getEncoderForChannels(2);
		}
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
				data = convertToOpus(data, channels);
			}

			return data;
		}
		return new byte[0];
	}

	private byte[] convertToOpus(byte[] in, int channels) {
		try {
			ShortBuffer nonEncodedBuffer = ShortBuffer.allocate(in.length/2);
			ByteBuffer encoded = ByteBuffer.allocate(4096);
			for (int i = 0; i < in.length; i += 2) {
				int firstByte = (0x000000FF & in[i]);      //Promotes to int and handles the fact that it was unsigned.
				int secondByte = (0x000000FF & in[i+1]);  //

				//Combines the 2 bytes into a short. Opus deals with unsigned shorts, not bytes.
				short toShort = (short) ((firstByte << 8) | secondByte);

				nonEncodedBuffer.put(toShort);
			}
			nonEncodedBuffer.flip();

			//TODO: check for 0 / negative value for error.
			int result = Opus.INSTANCE.opus_encode(getEncoderForChannels(channels), nonEncodedBuffer, AudioManager.OPUS_FRAME_SIZE, encoded, encoded.capacity());

			byte[] audio = new byte[result];
			encoded.get(audio);
			return audio;
		} catch (UnsatisfiedLinkError | Exception e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE, "Discord4J Internal Exception", e);
			return new byte[0];
		}
	}

	//Caching encoder objects is more efficient than dynamically creating/destroying them.
	private PointerByReference getEncoderForChannels(int channels) {
		if (!encoders.containsKey(channels)) {
			try {
				IntBuffer error = IntBuffer.allocate(4);
				PointerByReference encoder = Opus.INSTANCE.opus_encoder_create(OPUS_SAMPLE_RATE, channels, Opus.OPUS_APPLICATION_AUDIO, error);
				encoders.put(channels, encoder);
			} catch (UnsatisfiedLinkError | Exception e) {
				Discord4J.LOGGER.error(LogMarkers.VOICE, "Discord4J Internal Exception", e);
			}
		}

		return encoders.get(channels);
	}
}
