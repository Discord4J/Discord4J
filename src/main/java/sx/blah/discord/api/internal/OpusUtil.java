package sx.blah.discord.api.internal;

import com.sun.jna.ptr.PointerByReference;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.Opus;
import sx.blah.discord.util.LogMarkers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A util class for interacting with {@link Opus}.
 * For internal use only.
 */
public class OpusUtil {

	public static final int OPUS_SAMPLE_RATE = 48000;
	public static final int OPUS_FRAME_SIZE = 960;
	public static final int OPUS_FRAME_TIME_AMOUNT = OPUS_FRAME_SIZE * 1000 / OPUS_SAMPLE_RATE;

	private static final ConcurrentHashMap<Integer, PointerByReference> encoders = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Integer, PointerByReference> decoders = new ConcurrentHashMap<>();

	// FIXME: I broke #53 by making this static again. Need to figure out a nice way to make it per-guild. (per-user for decoding?)
	static {
		// Creates encoders for the 2 most common channel counts. The rest are uncommon enough that it's better to use lazy initialization for them.
		getEncoderForChannels(1);
		getEncoderForChannels(2);

		// The same for decoders.
		// FIXME: Is mono necessary? I think Discord always sends us stereo.
		getDecoderForChannels(1);
		getDecoderForChannels(2);
	}

	/**
	 * Encode the given raw PCM data to Opus.
	 * @param pcm The raw PCM data.
	 * @param channels The number of channels it should be encoded for.
	 * @return The opus-encoded audio.
	 */
	public static byte[] encodeToOpus(byte[] pcm, int channels) {
		try {
			ShortBuffer nonEncodedBuffer = ShortBuffer.allocate(pcm.length / 2);
			ByteBuffer encoded = ByteBuffer.allocate(4096);
			for (int i = 0; i < pcm.length; i += 2) {
				int firstByte = (0x000000FF & pcm[i]);      // Promotes to int and handles the fact that it was unsigned.
				int secondByte = (0x000000FF & pcm[i + 1]);   //

				// Combines the 2 bytes into a short. Opus deals with unsigned shorts, not bytes.
				short toShort = (short) ((firstByte << 8) | secondByte);

				nonEncodedBuffer.put(toShort);
			}
			nonEncodedBuffer.flip();

			// TODO: check for 0 / negative value for error.
			int result = Opus.INSTANCE.opus_encode(getEncoderForChannels(channels), nonEncodedBuffer, OPUS_FRAME_SIZE, encoded, encoded.capacity());

			byte[] audio = new byte[result];
			encoded.get(audio);
			return audio;
		} catch (UnsatisfiedLinkError | Exception e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE, "Discord4J Internal Exception", e);
			return new byte[0];
		}
	}

	/**
	 * Decodes opus-encoded audio to raw PCM data. (Signed 16-bit, Big-Endian, mono/stereo, 48000 HZ)
	 * @param opusAudio The opus-encoded audio.
	 * @param channels The number of channels to decode for.
	 * @return The raw PCM data.
	 */
	public static byte[] decodeToPCM(byte[] opusAudio, int channels) {
		ShortBuffer decodedBuffer = ShortBuffer.allocate(4096);
		int result = Opus.INSTANCE.opus_decode(getDecoderForChannels(channels), opusAudio, opusAudio.length, decodedBuffer, OPUS_FRAME_SIZE, 0);
		short[] shortAudio = new short[result * 2];
		decodedBuffer.get(shortAudio);

		ByteBuffer byteBuffer = ByteBuffer.allocate(shortAudio.length * 2); //
		byteBuffer.order(ByteOrder.BIG_ENDIAN);                             // Convert to bytes (Big Endian format) TODO: Specify format?
		byteBuffer.asShortBuffer().put(shortAudio);							//
		return byteBuffer.array();
	}

	// Caching encoder objects is more efficient than dynamically creating/destroying them.
	private static PointerByReference getEncoderForChannels(int channels) {
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

	// Caching decoder objects is more efficient than dynamically creating/destroying them.
	private static PointerByReference getDecoderForChannels(int channels) {
		if (!decoders.containsKey(channels)) {
			try {
				IntBuffer error = IntBuffer.allocate(4);
				PointerByReference decoder = Opus.INSTANCE.opus_decoder_create(OPUS_SAMPLE_RATE, channels, error);
				decoders.put(channels, decoder);
			} catch (UnsatisfiedLinkError | Exception e) {
				Discord4J.LOGGER.error(LogMarkers.VOICE, "Discord4J Internal Exception", e);
			}
		}

		return decoders.get(channels);
	}
}
