package sx.blah.discord.util.audio.events;

import sx.blah.discord.handle.audio.IAudioProcessor;
import sx.blah.discord.util.audio.AudioPlayer;

/**
 * This event is dispatched whenever {@link AudioPlayer#removeProcessor(IAudioProcessor)} is called.
 */
public class ProcessorRemoveEvent extends AudioPlayerEvent {

	private final IAudioProcessor processor;

	public ProcessorRemoveEvent(AudioPlayer player, IAudioProcessor processor) {
		super(player);
		this.processor = processor;
	}

	/**
	 * This gets the processor removed to the {@link AudioPlayer} instance.
	 *
	 * @return The processor.
	 */
	public IAudioProcessor getProcessor() {
		return processor;
	}
}
