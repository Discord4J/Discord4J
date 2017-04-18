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

package sx.blah.discord.handle.audio.impl;

import com.sun.jna.ptr.PointerByReference;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.OpusUtil;
import sx.blah.discord.handle.audio.*;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.Lazy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AudioManager implements IAudioManager {

	private final IGuild guild;
	private final IDiscordClient client;
	private final Map<IUser, List<IAudioReceiver>> userReceivers = new ConcurrentHashMap<>();
	private final List<IAudioReceiver> generalReceivers = new CopyOnWriteArrayList<>();
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
	public synchronized void subscribeReceiver(IAudioReceiver receiver) {
		subscribeReceiver(receiver, null);
	}

	@Override
	public synchronized void subscribeReceiver(IAudioReceiver receiver, IUser user) {
		if (user == null) {
			generalReceivers.add(receiver);
		} else {
			userReceivers.computeIfAbsent(user, u -> new CopyOnWriteArrayList<>()).add(receiver);
		}
	}

	@Override
	public synchronized void unsubscribeReceiver(IAudioReceiver receiver) {
		// Check general receivers
		generalReceivers.removeIf(r -> r.equals(receiver));
		// Check user receivers
		userReceivers.values().forEach(list -> list.removeIf(r -> r.equals(receiver)));
	}

	public synchronized byte[] sendAudio() {
		IAudioProcessor processor = getAudioProcessor();
		IAudioProvider provider = useProcessor ? processor : getAudioProvider();

		return getAudioDataForProvider(provider);
	}

	public synchronized void receiveAudio(byte[] opus, IUser user, char sequence, int timestamp) {
		// Initializing decoder is an expensive op. Don't do it if no one is listening
		if (generalReceivers.size() > 0 || userReceivers.size() > 0) {
			byte[] pcm = OpusUtil.decode(stereoDecoder.get(), opus);
			receiveAudio(opus, pcm, user, sequence, timestamp);
		}
	}

	private void receiveAudio(byte[] opusAudio, byte[] pcmAudio, IUser user, char sequence, int timestamp) {
		generalReceivers.parallelStream().forEach(r -> {
			if (r.getAudioEncodingType() == AudioEncodingType.OPUS) {
				r.receive(opusAudio, user, sequence, timestamp);
			} else {
				r.receive(pcmAudio, user, sequence, timestamp);
			}
		});

		if (userReceivers.containsKey(user)) {
			userReceivers.get(user).parallelStream().forEach(r -> {
				if (r.getAudioEncodingType() == AudioEncodingType.OPUS) {
					r.receive(opusAudio, user, sequence, timestamp);
				} else {
					r.receive(pcmAudio, user, sequence, timestamp);
				}
			});
		}
	}

	@Override
	public IGuild getGuild() {
		return guild;
	}

	private byte[] getAudioDataForProvider(IAudioProvider provider) {
		if (provider.isReady() && !Discord4J.audioDisabled.get()) {
			AudioEncodingType type = provider.getAudioEncodingType();
			int channels = provider.getChannels();
			byte[] data = provider.provide();
			if (data == null)
				data = new byte[0];

			if (type != AudioEncodingType.OPUS) {
				data = OpusUtil.encode(channels == 1 ? monoEncoder.get() : stereoEncoder.get(), data);
			}

			return data;
		}
		return new byte[0];
	}
}
