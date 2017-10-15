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

package sx.blah.discord.util.audio;

import org.tritonus.dsp.ais.AmplitudeAudioInputStream;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.audio.AudioEncodingType;
import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.audio.IAudioProcessor;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.audio.events.*;
import sx.blah.discord.util.audio.processors.MultiProcessor;
import sx.blah.discord.util.audio.processors.PauseableProcessor;
import sx.blah.discord.util.audio.providers.AudioInputStreamProvider;
import sx.blah.discord.util.audio.providers.FileProvider;
import sx.blah.discord.util.audio.providers.URLProvider;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A general purpose audio player.
 *
 * <p>Because the player wraps the given {@link IAudioManager}, it is recommended (although not required) to not
 * interact with the audio manager after creating an audio player with it.
 *
 * <p>AudioPlayer dispatches events located in {@link sx.blah.discord.util.audio.events}.
 *
 * <p>NOTE: The goal of this class is to provide a wide variety of tools which work for a wide variety of use-cases. As
 * such, the feature set has a wide breadth but not necessarily depth.
 *
 * <p>The player supports all audio formats supported by {@link javax.sound.sampled.AudioSystem} in addition to MPEG and
 * FLAC formats.
 */
public class AudioPlayer implements IAudioProvider {

	/**
	 * The audio players for each guild.
	 */
	private final static Map<IGuild, AudioPlayer> playerInstances = new ConcurrentHashMap<>();

	/**
	 * The underlying audio manager.
	 */
	private final IAudioManager manager;
	/**
	 * The client the guild belongs to.
	 */
	private final IDiscordClient client;

	/**
	 * The audio manager's original provider.
	 */
	private volatile IAudioProvider backupProvider;
	/**
	 * The audio manager's original processor.
	 */
	private volatile IAudioProcessor backupProcessor;

	/**
	 * The audio player's audio processor.
	 */
	private final MultiProcessor playerProcessor = new MultiProcessor();

	//Controls
	/**
	 * The audio player's pause processor.
	 */
	private final PauseableProcessor pauseController = new PauseableProcessor();

	/**
	 * The queue of tracks in the audio player.
	 */
	private final List<Track> trackQueue = new CopyOnWriteArrayList<>();

	/**
	 * Whether the tracks are looping.
	 */
	private volatile boolean loop = false;
	/**
	 * Whether the audio player was ready as of the last isReady() check.
	 */
	private volatile boolean wasReadyLast = false;
	/**
	 * The track of the audio player as of the last isReady() check.
	 */
	private volatile Track lastTrack;

	/**
	 * The volume of the audio player.
	 */
	private volatile float volume = 1.0F;

	/**
	 * Gets an audio player for the given guild. It will first attempt to find an injected player cached in the internal
	 * map, otherwise it will construct a new instance.
	 *
	 * @param guild The guild the audio player belongs to.
	 * @return The audio player for the given guild.
	 */
	public static AudioPlayer getAudioPlayerForGuild(IGuild guild) {
		if (playerInstances.containsKey(guild)) {
			return playerInstances.get(guild);
		} else {
			return new AudioPlayer(guild);
		}
	}

	/**
	 * Gets an audio player for the given audio manager. It will first attempt to find an injected player cached in the
	 * internal map, otherwise it will construct a new instance.
	 *
	 * @param manager The audio manager the audio player belongs to.
	 * @return The audio player for the given audio manager.
	 */
	public static AudioPlayer getAudioPlayerForAudioManager(IAudioManager manager) {
		return getAudioPlayerForGuild(manager.getGuild());
	}

	public AudioPlayer(IAudioManager manager) {
		this.manager = manager;
		this.client = manager.getGuild().getClient();
		inject();
	}

	public AudioPlayer(IGuild guild) {
		this(guild.getAudioManager());
	}

	/**
	 * Wraps the audio manager. This recreates all the internal controls and caches.
	 *
	 * <p>This is automatically called in the constructor. It should only be called again if {@link #clean()} is called.
	 */
	public void inject() {
		//Backs up the previous provider and processor
		backupProvider = manager.getAudioProvider();
		backupProcessor = manager.getAudioProcessor();

		//Sets up the player's provider and processor
		setupControls();

		//Inserting the controls for the player
		manager.setAudioProvider(this);
		manager.setAudioProcessor(playerProcessor);

		playerInstances.put(manager.getGuild(), this);

		client.getDispatcher().dispatch(new AudioPlayerInitEvent(this));
	}

	/**
	 * Adds the pause processor to the player's multiprocessor.
	 */
	private void setupControls() {
		playerProcessor.add(pauseController);
	}

	/**
	 * This removes all references of this player from the {@link IAudioManager} this is associated to. It additionally
	 * loads the previously used {@link sx.blah.discord.handle.audio.IAudioProvider} and
	 * {@link sx.blah.discord.handle.audio.IAudioProcessor}.
	 * NOTE: You will need to call {@link #inject()} if you wish to use this player instance again.
	 *
	 * Removes
	 */
	public void clean() {
		//Restores the previous provider and processor
		manager.setAudioProvider(backupProvider);
		manager.setAudioProcessor(backupProcessor);

		playerInstances.remove(manager.getGuild(), this);

		clear();

		client.getDispatcher().dispatch(new AudioPlayerCleanEvent(this));
	}

	/**
	 * Closes every track in the queue and clears the queue.
	 */
	public void clear() {
		trackQueue.forEach(Track::close);
		trackQueue.clear();
	}

	/**
	 * Gets the parent guild of the audio player.
	 *
	 * @return The parent guild of the audio player.
	 */
	public IGuild getGuild() {
		return manager.getGuild();
	}

	/**
	 * Sets whether the player is paused.
	 *
	 * @param isPaused Whether the player is paused.
	 */
	public void setPaused(boolean isPaused) {
		if (isPaused != this.isPaused()) {
			pauseController.setPaused(isPaused);
			client.getDispatcher().dispatch(new PauseStateChangeEvent(this, isPaused));
		}
	}

	/**
	 * Toggles the pause state.
	 *
	 * <p>This is equivalent to <code>setPaused(!isPaused())</code>
	 *
	 * @return The new pause state.
	 */
	public boolean togglePause() {
		boolean newPauseState = !isPaused();
		setPaused(newPauseState);
		return newPauseState;
	}

	/**
	 * Gets whether the player is paused.
	 *
	 * @return Whether the player is paused.
	 */
	public boolean isPaused() {
		return pauseController.isPaused();
	}

	/**
	 * Queues an audio track.

	 * @param stream The stream to wrap in a track and queue.
	 * @return The track that was queued.
	 */
	public Track queue(AudioInputStream stream) {
		Track track = new Track(stream);
		queue(track);
		return track;
	}

	/**
	 * Queues an audio track.

	 * @param file The file to wrap in a track and queue.
	 * @return The track that was queued.
	 *
	 * @throws IOException If an error occurs while constructing the FileProvider.
	 * @throws UnsupportedAudioFileException The the provided file is of an unsupported audio format.
	 */
	public Track queue(File file) throws IOException, UnsupportedAudioFileException {
		Track track = new Track(new FileProvider(file));
		track.getMetadata().put("file", file);
		queue(track);
		return track;
	}

	/**
	 * Queues an audio track.

	 * @param url The url to wrap in a track and queue.
	 * @return The track that was queued.
	 *
	 * @throws IOException If an error occurs while constructing the URLProvider.
	 * @throws UnsupportedAudioFileException The the provided file is of an unsupported audio format.
	 */
	public Track queue(URL url) throws IOException, UnsupportedAudioFileException {
		Track track = new Track(new URLProvider(url));
		track.getMetadata().put("url", url);
		queue(track);
		return track;
	}

	/**
	 * Queues an audio track.

	 * @param provider The provider to wrap in a track and queue.
	 * @return The track that was queued.
	 */
	public Track queue(IAudioProvider provider) {
		Track track = new Track(provider);
		queue(track);
		return track;
	}

	/**
	 * Queues an audio track.
	 *
	 * @param track The track to queue.
	 */
	public void queue(Track track) {
		trackQueue.add(track);

		client.getDispatcher().dispatch(new TrackQueueEvent(this, track));
	}

	/**
	 * Adds an audio processor to the player.
	 *
	 * @param processor The processor to add.
	 */
	public void addProcessor(IAudioProcessor processor) {
		playerProcessor.add(processor);

		client.getDispatcher().dispatch(new ProcessorAddEvent(this, processor));
	}

	/**
	 * Removes an audio processor from the player.
	 *
	 * @param processor The processor to remove.
	 */
	public void removeProcessor(IAudioProcessor processor) {
		playerProcessor.remove(processor);

		client.getDispatcher().dispatch(new ProcessorRemoveEvent(this, processor));
	}

	/**
	 * Sets whether the player should loop its queue. This is disabled by default.
	 *
	 * @param loop Whether the player should loop its queue.
	 */
	public void setLoop(boolean loop) {
		if (this.loop != loop) {
			this.loop = loop;

			client.getDispatcher().dispatch(new LoopStateChangeEvent(this, loop));
		}
	}

	/**
	 * Gets whether the player's queue is being looped.
	 *
	 * @return Whether the player's queue is being looped.
	 */
	public boolean isLooping() {
		return loop;
	}

	/**
	 * Shuffles the player's track queue.
	 */
	public synchronized void shuffle() {
		if (trackQueue.size() > 0) {
			getCurrentTrack().rewindTo(0);
			Collections.shuffle(trackQueue);

			client.getDispatcher().dispatch(new ShuffleEvent(this));
		}
	}

	/**
	 * Skips the current track.
	 *
	 * @return The track skipped (or null if the queue is empty).
	 */
	public Track skip() {
		if (trackQueue.size() > 0) {
			Track track = trackQueue.remove(0);

			if (track.isReady() && track.getCurrentTrackTime() == track.getTotalTrackTime()) { //The track was actually skipped, not skipped due to the way my logic works
				client.getDispatcher().dispatch(new TrackSkipEvent(this, track, trackQueue.size() > 0 ? trackQueue.get(0) : null));
			}

			if (isLooping()) {
				track.rewindTo(0); //Have to reset the audio
				trackQueue.add(track);
			} else {
				track.close();
			}
			return track;
		}
		return null;
	}

	/**
	 * Skips all tracks until the given given queue index.
	 *
	 * @param desiredPosition The index to skip to.
	 * @return The list of tracks that were skipped.
	 */
	public List<Track> skipTo(int desiredPosition) {
		desiredPosition = Math.max(0, desiredPosition);
		List<Track> skipped = new ArrayList<>();
		for (int i = 0; i < desiredPosition; i++)
			skipped.add(skip());
		return skipped;
	}

	/**
	 * Gets the size of the playlist.
	 *
	 * @return The playlist size.
	 */
	public int getPlaylistSize() {
		return trackQueue.size();
	}

	/**
	 * Gets the track queue.
	 *
	 * @return The track queue.
	 */
	public List<Track> getPlaylist() {
		return trackQueue;
	}

	/**
	 * Gets the current track playing.
	 *
	 * @return The current track.
	 */
	public Track getCurrentTrack() {
		return getPlaylistSize() > 0 ? trackQueue.get(0) : null;
	}

	/**
	 * Gets the volume of the player.
	 *
	 * @return The volume.
	 */
	public float getVolume() {
		return volume;
	}

	/**
	 * Sets the volume of the player.
	 *
	 * <p>This only works on tracks with an {@link AudioInputStream} rather than a direct {@link IAudioProvider}.
	 *
	 * @param volume The volume (1.0 is the default value).
	 */
	public void setVolume(float volume) { //Volume here rather than a processor due to how the volume mechanism works
		if (volume != this.volume) {
			float oldVolume = this.volume;
			this.volume = volume;
			client.getDispatcher().dispatch(new VolumeChangeEvent(this, oldVolume, volume));
		}
	}

	@Override
	public boolean isReady() {
		boolean ready = calculateReady();
		if (!ready && wasReadyLast) {
			Track original = getCurrentTrack();
			if (original != null) { //Check if there is no track that is supposed to be playing
				skip();
				Track next = getCurrentTrack();

				ready = calculateReady(); //Check again to allow for continuous playback

				client.getDispatcher().dispatch(new TrackFinishEvent(this, original, next));

				if (next != null)
					client.getDispatcher().dispatch(new TrackStartEvent(this, next)); //New track is now playing.
			}
		} else if (!wasReadyLast && ready || (lastTrack != getCurrentTrack() && getCurrentTrack() != null)) { //Track started playing for the first time
			client.getDispatcher().dispatch(new TrackStartEvent(this, getCurrentTrack()));
		}

		lastTrack = getCurrentTrack();

		return wasReadyLast = ready;
	}

	/**
	 * Gets whether the player is ready.
	 *
	 * @return Whether the player is ready.
	 */
	private boolean calculateReady() {
		return trackQueue.size() > 0 && getCurrentTrack().isReady();
	}

	@Override
	public byte[] provide() {
		Track currentTrack = getCurrentTrack();
		if (currentTrack.getStream() != null)
			((AmplitudeAudioInputStream) currentTrack.getStream()).setAmplitudeLinear(volume);

		return currentTrack.provide();
	}

	@Override
	public int getChannels() {
		return getCurrentTrack().getChannels();
	}

	@Override
	public AudioEncodingType getAudioEncodingType() {
		return getCurrentTrack().getAudioEncodingType();
	}

	/**
	 * An audio track for the player.
	 */
	public static class Track implements IAudioProvider { //TODO: Figure out a way to dispatch events on track scrubbing

		/**
		 * The length of the track in milliseconds.
		 */
		private volatile long totalTrackTime = -1;
		/**
		 * The amount of time in milliseconds that the track as progressed.
		 */
		private volatile long currentTrackTime = 0;
		/**
		 * The underlying provider of audio.
		 */
		private final IAudioProvider provider;
		/**
		 * The underlying audio stream.
		 */
		private final AmplitudeAudioInputStream stream;
		/**
		 * The audio that has been cached for the track.
		 */
		private final List<byte[]> audioCache = new CopyOnWriteArrayList<>(); //key = ms timestamp / 20 ms
		/**
		 * A map that can be used to store arbitrary metadata with the track.
		 */
		private final Map<String, Object> metadata = new ConcurrentHashMap<>();

		public Track(IAudioProvider provider) {
			if (provider instanceof AudioInputStreamProvider) {
				AudioInputStreamProvider streamProvider = (AudioInputStreamProvider) provider;

				// No need to call DiscordUtils#getPCMStream again, since it's already called on AudioInputStreamProvider's constructor.
				this.stream = new AmplitudeAudioInputStream(streamProvider.getStream());
				this.provider = new AudioInputStreamProvider(this.stream);

				// Available Frames / frames per second = Available seconds. Available seconds * 1000 = available milliseconds.
				totalTrackTime = (stream.getFrameLength() / (long)this.stream.getFormat().getFrameRate())*1000;
				if (totalTrackTime == 0)
					totalTrackTime = -1;
			} else {
				this.provider = provider;
				this.stream = null;
			}
		}

		public Track(AudioInputStream stream) {
			this(new AudioInputStreamProvider(stream));
		}

		/**
		 * Closes the underlying audio stream.
		 */
		protected void close() {
			if (stream != null && provider.isReady())
				try {
					stream.close();
				} catch (IOException e) {
					Discord4J.LOGGER.error(LogMarkers.VOICE, "Discord4J Internal Exception", e);
				}
		}

		/**
		 * Returns a mutable map that can be used to store arbitrary metadata with the track.
		 *
		 * <p>Default metadata includes:
		 * <ul>
		 *     <li>Key "file" to a {@link File} if the track was queued as a file.</li>
		 *     <li>Key "url" to a {@link URL} if the track was queued as a URL.</li>
		 * </ul>
		 *
		 * @return A map that can be used to store arbitrary metadata with the track.
		 */
		public Map<String, Object> getMetadata() {
			return metadata;
		}

		/**
		 * Gets the provider backing the track.
		 *
		 * @return The provider.
		 */
		public IAudioProvider getProvider() {
			return provider;
		}

		/**
		 * Gets the stream backing the track.
		 *
		 * @return The nullable stream.
		 */
		public AudioInputStream getStream() {
			return stream;
		}

		/**
		 * The length of the track in milliseconds.
		 *
		 * <p>If this value is <code>-1</code>, it is unknown at the time. Otherwise, it is always >= currentTrackTime.
		 * NOTE: This value can change. If the track is able to continue playing past the original total track,
		 * the total track time will be updated.
		 *
		 * @return The length of the track in milliseconds.
		 */
		public long getTotalTrackTime() {
			return totalTrackTime;
		}

		/**
		 * Gets the amount of time in milliseconds that the track as progressed.
		 *
		 * @return The amount of time in milliseconds that the track as progressed.
		 */
		public long getCurrentTrackTime() {
			return currentTrackTime;
		}

		/**
		 * Rewinds the track by the given amount of time.
		 *
		 * @param rewindTime The amount of time (in ms) to rewind by.
		 */
		public synchronized void rewind(long rewindTime) {
			rewindTo(currentTrackTime-rewindTime);
		}

		/**
		 * Rewinds the track to the given time.
		 *
		 * @param time The time (in ms).
		 */
		public synchronized void rewindTo(long time) {
			if (time > currentTrackTime)
				throw new IllegalArgumentException("Cannot rewind to a future timestamp (requested time: "+time+", current time: "+currentTrackTime+")");

			time = Math.max(0, time);
			time -= time % 20;
			currentTrackTime = time;
		}

		/**
		 * Fast forwards the track by the given amount of time.
		 *
		 * @param fastForwardTime The amount of time (in ms) to fast forward by.
		 */
		public synchronized void fastForward(long fastForwardTime) {
			fastForwardTo(currentTrackTime+fastForwardTime);
		}

		/**
		 * Fast forwards the track to the given time.
		 *
		 * @param time The time (in ms).
		 */
		public synchronized void fastForwardTo(long time) {
			if (time < currentTrackTime)
				throw new IllegalArgumentException("Cannot fast forward to a previous timestamp (requested time: "+time+", current time: "+currentTrackTime+")");

			time -= time % 20;
			while (isReady() && currentTrackTime != time) {
				provide();
			}
		}

		@Override
		public synchronized boolean isReady() {
			return provider.isReady() || audioCache.size() > ((int) (currentTrackTime/20));
		}

		@Override
		public synchronized byte[] provide() {
			currentTrackTime += 20; //provide() *should* be providing 20 ms of data
			if (currentTrackTime > totalTrackTime) { //When streaming, using a direct IAudioProvider, or using some file formats (like mp3), the total track time cannot be deduced. So lazily calculate instead.
				totalTrackTime = currentTrackTime;
			}

			int key = (int) (currentTrackTime/20) - 1; //Arrays are 0-based
			byte[] provided;

			if (audioCache.size() > key) {
				provided = audioCache.get(key);
			} else {
				provided = provider.provide();
				audioCache.add(key, provided);
			}

			return provided;
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
}
