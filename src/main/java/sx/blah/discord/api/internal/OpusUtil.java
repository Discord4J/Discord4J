package sx.blah.discord.api.internal;

import com.sun.jna.ptr.PointerByReference;
import org.apache.commons.lang3.tuple.Pair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.ValuePool;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * A util class for interacting with {@link Opus}.
 * For internal use only.
 */
public class OpusUtil {

	/**
	 * Measured in Hz, the number of samples of audio per second.
	 */
	public static final int OPUS_SAMPLE_RATE = 48000;

	/**
	 * The size of each frame of audio.
	 */
	public static final int OPUS_FRAME_SIZE = 960;

	/**
	 * How long, in milliseconds, each frame is.
	 */
	public static final int OPUS_FRAME_TIME_AMOUNT = 20; // OPUS_FRAME_SIZE * 1000 / OPUS_SAMPLE_RATE;

	private static ValuePool<IGuild, Pair<PointerByReference, PointerByReference>> encoderPool = new ValuePool<IGuild, Pair<PointerByReference, PointerByReference>>() {
		@Override
		public Pair<PointerByReference, PointerByReference> newObject() {
			PointerByReference mono = Opus.INSTANCE.opus_encoder_create(OPUS_SAMPLE_RATE, 1, Opus.OPUS_APPLICATION_AUDIO, IntBuffer.allocate(4));
			PointerByReference stereo = Opus.INSTANCE.opus_encoder_create(OPUS_SAMPLE_RATE, 2, Opus.OPUS_APPLICATION_AUDIO, IntBuffer.allocate(4));
			return Pair.of(mono, stereo);
		}
	};

	private static ValuePool<IUser, PointerByReference> decoderPool = new ValuePool<IUser, PointerByReference>() {
		@Override
		public PointerByReference newObject() {
			return Opus.INSTANCE.opus_decoder_create(OPUS_SAMPLE_RATE, 2, IntBuffer.allocate(4));
		}
	};

	/**
	 * Encodes raw PCM data to opus.
	 * @param pcm The PCM data.
	 * @param channels The number of channels the audio should be encoded for.
	 * @param guild The guild this audio is being encoded for. This is used to decide which encoder instance to use.
	 * @return The opus-encoded audio.
	 */
	public static byte[] encodeToOpus(byte[] pcm, int channels, IGuild guild) {
		return encodeToOpus(pcm, getEncoder(channels, guild));
	}

	/**
	 * Decodes opus-encoded audio to raw PCM data.
	 * @param opusAudio The opus-encoded audio.
	 * @param user The user from which this audio was received. Used to decide which decoder to use.
	 * @return The raw PCM data.
	 */
	public static byte[] decodeToPCM(byte[] opusAudio, IUser user) {
		return decodeToPCM(opusAudio, getDecoder(user));
	}

	/**
	 * Encodes raw PCM data to opus.
	 * @param pcm The PCM data.
	 * @param encoder The encoder to use. Should be decided by {@link #getEncoder}.
	 * @return The opus-encoded audio.
	 */
	private static byte[] encodeToOpus(byte[] pcm, PointerByReference encoder) {
		try {
			ShortBuffer nonEncodedBuffer = ShortBuffer.allocate(pcm.length / 2);
			ByteBuffer encoded = ByteBuffer.allocate(4096);
			for (int i = 0; i < pcm.length; i += 2) {
				int firstByte = (0x000000FF & pcm[i]);      // Promotes to int and handles the fact that it was unsigned.
				int secondByte = (0x000000FF & pcm[i + 1]); //

				// Combines the 2 bytes into a short. Opus deals with unsigned shorts, not bytes.
				short toShort = (short) ((firstByte << 8) | secondByte);

				nonEncodedBuffer.put(toShort);
			}
			nonEncodedBuffer.flip();

			int result = Opus.INSTANCE.opus_encode(encoder, nonEncodedBuffer, OPUS_FRAME_SIZE, encoded, encoded.capacity());
			if (result < 0) throw new RuntimeException("Failed to encode audio to opus. Error code: " + result);

			byte[] audio = new byte[result];
			encoded.get(audio);
			return audio;
		} catch (UnsatisfiedLinkError | Exception e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE, "Discord4J Internal Exception", e);
			return new byte[0];
		}
	}

	/**
	 * Decode opus-encoded audio to raw PCM data.
	 * @param opusAudio The opus-encoded audio.
	 * @param decoder The decoder to use for decoding. Should be decided by {@link #getDecoder}.
	 * @return The raw PCM data.
	 */
	private static byte[] decodeToPCM(byte[] opusAudio, PointerByReference decoder) {
		ShortBuffer decodedBuffer = ShortBuffer.allocate(4096);

		int result = Opus.INSTANCE.opus_decode(decoder, opusAudio, opusAudio.length, decodedBuffer, OPUS_FRAME_SIZE, 0);
		if (result < 0) throw new RuntimeException("Failed to decode opus to pcm. Error code: " + result);

		ByteBuffer byteBuffer = ByteBuffer.allocate(result * 2); //
		byteBuffer.order(ByteOrder.BIG_ENDIAN);                             // Convert to bytes (Big Endian format)
		byteBuffer.asShortBuffer().put(decodedBuffer.array(), 0, result);                         //
		return byteBuffer.array();
	}

	/**
	 * Gets the appropriate encoder instance from the number of channels and guild.
	 * @param channels The number of channels the audio is being encoded for.
	 * @param guild The guild this audio is being sent to.
	 * @return The appropriate encoder.
	 */
	private static PointerByReference getEncoder(int channels, IGuild guild) {
		Pair encoders = encoderPool.get(guild);
		return (PointerByReference) (channels == 1 ? encoders.getLeft() : encoders.getRight());
	}

	/**
	 * Gets the appropriate decoder instance from the number of channels and guild.
	 * @param user The user from which this audio was received.
	 * @return The appropriate decoder.
	 */
	private static PointerByReference getDecoder(IUser user) {
		return decoderPool.get(user);
	}
}
