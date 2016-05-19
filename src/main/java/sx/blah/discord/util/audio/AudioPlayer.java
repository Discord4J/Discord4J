package sx.blah.discord.util.audio;

import org.tritonus.dsp.ais.AmplitudeAudioInputStream;
import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.audio.IAudioProcessor;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.obj.IGuild;
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
 * NOTE: The goal of this class is to provide a wide variety of tools which works on a wide variety of use-cases. As
 * such, the feature set has a wide breadth but not depth.
 */
public class AudioPlayer implements IAudioProvider {

	private final static Map<IGuild, AudioPlayer> playerInstances = new ConcurrentHashMap<>();

	private final IAudioManager manager;

	private volatile IAudioProvider backupProvider;
	private volatile IAudioProcessor backupProcessor;

	private final MultiProcessor playerProcessor = new MultiProcessor();

	//Controls
	private final PauseableProcessor pauseController = new PauseableProcessor();

	private final List<Track> trackQueue = new CopyOnWriteArrayList<>();

	private volatile boolean loop = false;
	private volatile boolean wasReadyLast = false;

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
	}

	/**
	 * Sets whether this player is paused or not.
	 *
	 * @param isPaused True to pause, false to resume.
	 */
	public void setPaused(boolean isPaused) {
		pauseController.setPaused(isPaused);
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
	 *
	 * @param file The file to queue.
	 * @return The {@link Track} object representing this file.
	 *
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 */
	public Track queue(File file) throws IOException, UnsupportedAudioFileException {
		Track track = new Track(new FileProvider(file));
		queue(track);
		return track;
	}

	/**
	 * This queues a url for the AudioPlayer.
	 *
	 * @param url The url to queue.
	 * @return The {@link Track} object representing this url.
	 *
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 */
	public Track queue(URL url) throws IOException, UnsupportedAudioFileException {
		Track track = new Track(new URLProvider(url));
		queue(track);
		return track;
	}

	/**
	 * This queues an audio provider for the AudioPlayer.
	 *
	 * @param provider The audio provider to queue.
	 * @return The {@link Track} object representing this audio provider.
	 */
	public Track queue(IAudioProvider provider) {
		Track track = new Track(provider);
		queue(track);
		return track;
	}

	/**
	 * This queues a track for the AudioPlayer.
	 *
	 * @param track The track to queue.
	 */
	public void queue(Track track) {
		trackQueue.add(track);
	}

	/**
	 * This adds an {@link IAudioProcessor} to this player.
	 *
	 * @param processor The processor to add.
	 */
	public void addProcessor(IAudioProcessor processor) {
		playerProcessor.add(processor);
	}

	/**
	 * This removes an {@link IAudioProcessor} to this player.
	 *
	 * @param processor The processor to remove.
	 */
	public void removeProcessor(IAudioProcessor processor) {
		playerProcessor.remove(processor);
	}

	/**
	 * This sets whether this player should loop its playlist. This is disabled by default.
	 *
	 * @param loop True to loop, false to not.
	 */
	public void setLoop(boolean loop) {
		this.loop = loop;
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
		getCurrentTrack().rewindTo(0);
		Collections.shuffle(trackQueue);
	}

	/**
	 * This skips the current track.
	 */
	public void skip() {
		if (trackQueue.size() > 0) {
			Track track = trackQueue.remove(0);

			if (isLooping()) {
				track.rewindTo(0); //Have to reset the audio
				trackQueue.add(track);
			}
		}
	}

	/**
	 * This skips until the playlist is playing the specified track.
	 *
	 * @param desiredPosition The playlist spot to skip to.
	 */
	public void skipTo(int desiredPosition) {
		desiredPosition = Math.max(0, desiredPosition);
		for (int i = 0; i < desiredPosition; i++)
			skip();
	}

	/**
	 * Gets the size of the playlist.
	 *
	 * @return The playlist size.
	 */
	public int playlistSize() {
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
		return playlistSize() > 0 ? trackQueue.get(0) : null;
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
		this.volume = volume;
	}

	@Override
	public boolean isReady() {
		boolean ready = calculateReady();
		if (!ready && wasReadyLast) {
			skip();

			ready = calculateReady(); //Check again to allow for continuous playback
		}
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
	public static class Track implements IAudioProvider {

		private volatile long totalTrackTime = -1;
		private volatile long currentTrackTime = 0;
		private final IAudioProvider provider;
		private final AmplitudeAudioInputStream stream;
		private final List<byte[]> audioCache = new CopyOnWriteArrayList<>(); //key = ms timestamp / 20 ms

		public Track(IAudioProvider provider) {
			this.provider = provider;
			stream = null;
		}

		public Track(AudioInputStreamProvider provider) throws IOException {
			this(provider.getStream());
		}

		public Track(AudioInputStream stream) throws IOException {
			this.stream = new AmplitudeAudioInputStream(stream);
			this.provider = new AudioInputStreamProvider(this.stream);

			//Available Frames / frames per second = Available seconds. Available seconds * 1000 = available milliseconds.
			totalTrackTime = (stream.getFrameLength() / (long)this.stream.getFormat().getFrameRate())*1000;
			if (totalTrackTime == 0)
				totalTrackTime = -1;

			System.out.println(totalTrackTime);
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
		 * This fast forwards the strack by the specified amount of time.
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
			return provider.isReady() || audioCache.size() > ((int) (currentTrackTime/20))/*Arrays are 0-based*/;
		}

		@Override
		public synchronized byte[] provide() {
			currentTrackTime += 20; //provide() *should* be providing 20 ms of data
			if (currentTrackTime > totalTrackTime) { //When streaming or using a direct IAudioProvider, the total track time cannot be deduced. So lazily calculate instead.
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
