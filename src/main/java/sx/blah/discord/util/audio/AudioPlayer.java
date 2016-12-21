package sx.blah.discord.util.audio;

import org.tritonus.dsp.ais.AmplitudeAudioInputStream;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordUtils;
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
 * This is a general purpose audio player. This wraps the provided {@link sx.blah.discord.handle.audio.IAudioManager}.
 * So if you are using this, it is recommended (all though not necessary) to not interact with the audio manager after
 * an AudioPlayer instance is created.
 *
 * AudioPlayer also dispatches events. These events are located in the {@link sx.blah.discord.util.audio.events} package.
 *
 * NOTE: The goal of this class is to provide a wide variety of tools which works on a wide variety of use-cases. As
 * such, the feature set has a wide breadth but not necessarily depth.
 */
public class AudioPlayer implements IAudioProvider {

	private final static Map<IGuild, AudioPlayer> playerInstances = new ConcurrentHashMap<>();

	private final IAudioManager manager;
	private final IDiscordClient client;

	private volatile IAudioProvider backupProvider;
	private volatile IAudioProcessor backupProcessor;

	private final MultiProcessor playerProcessor = new MultiProcessor();

	//Controls
	private final PauseableProcessor pauseController = new PauseableProcessor();

	private final List<Track> trackQueue = new CopyOnWriteArrayList<>();

	private volatile boolean loop = false;
	private volatile boolean wasReadyLast = false;
	private volatile Track lastTrack;

	private volatile float volume = 1.0F;

	/**
	 * This gets an AudioPlayer instance for the given {@link IGuild}. It will first attempt to find an injected player
	 * instance cached in an internal map, otherwise it'll construct a new instance.
	 *
	 * @param guild The guild for which the player belongs.
	 * @return The player.
	 */
	public static AudioPlayer getAudioPlayerForGuild(IGuild guild) {
		if (playerInstances.containsKey(guild)) {
			return playerInstances.get(guild);
		} else {
			return new AudioPlayer(guild);
		}
	}

	/**
	 * This gets an AudioPlayer instance for the given {@link IAudioManager}. It will first attempt to find an injected
	 * player instance cached in an internal map, otherwise it'll construct a new instance.
	 *
	 * @param manager The manager for which the player belongs.
	 * @return The player.
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
	 * This is used to wrap this player around the audio manager. This recreates all the internal controls and caches.
	 * NOTE: This is automatically called in the constructor, you should only manually call this if {@link #clean()}
	 * was previously called.
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

	private void setupControls() {
		playerProcessor.add(pauseController);
	}

	/**
	 * This removes all references of this player from the {@link IAudioManager} this is associated to. It additionally
	 * loads the previously used {@link sx.blah.discord.handle.audio.IAudioProvider} and
	 * {@link sx.blah.discord.handle.audio.IAudioProcessor}.
	 * NOTE: You will need to call {@link #inject()} if you wish to use this player instance again.
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
	 * This clears the current playlist. It should be noted that this closes the stream that provides the audio to each
	 * {@link Track} object (if it exists). Which prevents these objects from being reused reliably.
	 */
	public void clear() {
		trackQueue.forEach(Track::close);
		trackQueue.clear();
	}

	/**
	 * This gets the guild this AudioPlayer instance is associated to.
	 *
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return manager.getGuild();
	}

	/**
	 * Sets whether this player is paused or not.
	 *
	 * @param isPaused True to pause, false to resume.
	 */
	public void setPaused(boolean isPaused) {
		if (isPaused != this.isPaused()) {
			pauseController.setPaused(isPaused);
			client.getDispatcher().dispatch(new PauseStateChangeEvent(this, isPaused));
		}
	}

	/**
	 * This toggles the pause state on or off.
	 * This is a convenience shortcut for {@code setPaused(!isPaused())}.
	 *
	 * @return The new pause state.
	 */
	public boolean togglePause() {
		boolean newPauseState = !isPaused();
		setPaused(newPauseState);
		return newPauseState;
	}

	/**
	 * Gets whether this player is paused or not.
	 *
	 * @return True if paused, false if not.
	 */
	public boolean isPaused() {
		return pauseController.isPaused();
	}

	/**
	 * This queues an AudioInputStream for the AudioPlayer.
	 * <br>Supports: ogg, mp3, flac, wav
	 *
	 * @param stream The stream to queue.
	 * @return The {@link Track} object representing this stream.
	 *
	 * @throws IOException
	 */
	public Track queue(AudioInputStream stream) throws IOException {
		Track track = new Track(stream);
		queue(track);
		return track;
	}

	/**
	 * This queues a file for the AudioPlayer.
	 * <br>Supports: ogg, mp3, flac, wav
	 *
	 * @param file The file to queue.
	 * @return The {@link Track} object representing this file.
	 *
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 */
	public Track queue(File file) throws IOException, UnsupportedAudioFileException {
		Track track = new Track(new FileProvider(file));
		track.getMetadata().put("file", file);
		queue(track);
		return track;
	}

	/**
	 * This queues a url for the AudioPlayer.
	 * <br>Supports: ogg, mp3, flac, wav
	 *
	 * @param url The url to queue.
	 * @return The {@link Track} object representing this url.
	 *
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 */
	public Track queue(URL url) throws IOException, UnsupportedAudioFileException {
		Track track = new Track(new URLProvider(url));
		track.getMetadata().put("url", url);
		queue(track);
		return track;
	}

	/**
	 * This queues an audio provider for the AudioPlayer.
	 * <br>Supports: ogg, mp3, flac, wav
	 *
	 * @param provider The audio provider to queue.
	 * @return The {@link Track} object representing this audio provider.
	 */
	public Track queue(IAudioProvider provider) {
		Track track;

		if (provider instanceof AudioInputStreamProvider)
			track = new Track((AudioInputStreamProvider) provider);
		else
			track = new Track(provider);

		queue(track);
		return track;
	}

	/**
	 * This queues a track for the AudioPlayer.
	 * <br>Supports: ogg, mp3, flac, wav
	 *
	 * @param track The track to queue.
	 */
	public void queue(Track track) {
		trackQueue.add(track);

		client.getDispatcher().dispatch(new TrackQueueEvent(this, track));
	}

	/**
	 * This adds an {@link IAudioProcessor} to this player.
	 *
	 * @param processor The processor to add.
	 */
	public void addProcessor(IAudioProcessor processor) {
		playerProcessor.add(processor);

		client.getDispatcher().dispatch(new ProcessorAddEvent(this, processor));
	}

	/**
	 * This removes an {@link IAudioProcessor} to this player.
	 *
	 * @param processor The processor to remove.
	 */
	public void removeProcessor(IAudioProcessor processor) {
		playerProcessor.remove(processor);

		client.getDispatcher().dispatch(new ProcessorRemoveEvent(this, processor));
	}

	/**
	 * This sets whether this player should loop its playlist. This is disabled by default.
	 *
	 * @param loop True to loop, false to not.
	 */
	public void setLoop(boolean loop) {
		if (this.loop != loop) {
			this.loop = loop;

			client.getDispatcher().dispatch(new LoopStateChangeEvent(this, loop));
		}
	}

	/**
	 * This gets whether this playlist is being looped.
	 *
	 * @return True if looped, false if otherwise.
	 */
	public boolean isLooping() {
		return loop;
	}

	/**
	 * This shuffles the playlist in the queue.
	 */
	public synchronized void shuffle() {
		if (trackQueue.size() > 0) {
			getCurrentTrack().rewindTo(0);
			Collections.shuffle(trackQueue);

			client.getDispatcher().dispatch(new ShuffleEvent(this));
		}
	}

	/**
	 * This skips the current track.
	 *
	 * @return The track skipped. (null if the playlist is empty)
	 */
	public Track skip() {
		if (trackQueue.size() > 0) {
			Track track = trackQueue.remove(0);

			if (track.isReady() && track.getCurrentTrackTime() == track.getTotalTrackTime()) { //The track was actually skipped, not skipped due to the way my logic works
				client.getDispatcher().dispatch(new TrackSkipEvent(this, track));
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
	 * This skips until the playlist is playing the specified track.
	 *
	 * @param desiredPosition The playlist spot to skip to.
	 * @return A list of all tracks skipped (if any)
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
	 * Gets the list representing the playlist.
	 *
	 * @return The playlist. NOTE: This is mutable and is the same instance used in the player.
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
	 * Sets the volume of the player. NOTE: This only works on tracks with an {@link AudioInputStream} rather than a
	 * direct {@link IAudioProvider}.
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
	 * This object represents the audio being played by this player.
	 */
	public static class Track implements IAudioProvider { //TODO: Figure out a way to dispatch events on track scrubbing

		private volatile long totalTrackTime = -1;
		private volatile long currentTrackTime = 0;
		private final IAudioProvider provider;
		private final AmplitudeAudioInputStream stream;
		private final List<byte[]> audioCache = new CopyOnWriteArrayList<>(); //key = ms timestamp / 20 ms
		private final Map<String, Object> metadata = new ConcurrentHashMap<>();

		public Track(IAudioProvider provider) {
			this.provider = provider;
			stream = null;
		}

		public Track(AudioInputStreamProvider provider) {
			this(provider.getStream());
		}

		public Track(AudioInputStream stream) {
			this.stream = new AmplitudeAudioInputStream(DiscordUtils.getPCMStream(stream));
			this.provider = new AudioInputStreamProvider(this.stream);

			//Available Frames / frames per second = Available seconds. Available seconds * 1000 = available milliseconds.
			totalTrackTime = (stream.getFrameLength() / (long)this.stream.getFormat().getFrameRate())*1000;
			if (totalTrackTime == 0)
				totalTrackTime = -1;
		}

		protected void close() {
			if (stream != null && provider.isReady())
				try {
					stream.close();
				} catch (IOException e) {
					Discord4J.LOGGER.error(LogMarkers.VOICE, "Discord4J Internal Exception", e);
				}
		}

		/**
		 * This returns a mutable map representing arbitrary metadata attached to this track.
		 * If the track was created through a File, a key of "file" will have a {@link File} object which represents the
		 * source of the audio.
		 * If the track was created through a URL, a key of "url" will have a {@link URL} object which represents the
		 * source of the audio.
		 *
		 * @return The metadata.
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
		 * @return The stream. This can be null!
		 */
		public AudioInputStream getStream() {
			return stream;
		}

		/**
		 * This gets the total track time in milliseconds.
		 *
		 * @return The total track time. If -1, it is unknown at the time, else it should always be >= currentTrack
		 * time. NOTE: This value can change. If the track is able to continue playing past the original total track,
		 * the total track time will be updated.
		 */
		public long getTotalTrackTime() {
			return totalTrackTime;
		}

		/**
		 * This gets the current timestamp the player.
		 *
		 * @return The current timestamp.
		 */
		public long getCurrentTrackTime() {
			return currentTrackTime;
		}

		/**
		 * This rewinds the track by the specified amount of time.
		 *
		 * @param rewindTime The amount of time (in ms) to rewind by.
		 */
		public synchronized void rewind(long rewindTime) {
			rewindTo(currentTrackTime-rewindTime);
		}

		/**
		 * This rewinds the track to a specified time.
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
		 * This fast forwards the track by the specified amount of time.
		 *
		 * @param fastForwardTime The amount of time (in ms) to fast forward by.
		 */
		public synchronized void fastForward(long fastForwardTime) {
			fastForwardTo(currentTrackTime+fastForwardTime);
		}

		/**
		 * This tries to fast forward the track to a specified time.
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
