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

package sx.blah.discord.util.audio.processors;

import sx.blah.discord.handle.audio.AudioEncodingType;
import sx.blah.discord.handle.audio.IAudioProcessor;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.audio.impl.DefaultProcessor;
import sx.blah.discord.handle.audio.impl.DefaultProvider;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is an {@link IAudioProcessor} implementation which can combine multiple processors.
 * Note: The higher the processor index, the later the processor is used in the queue.
 */
public class MultiProcessor implements IAudioProcessor {

	private final CopyOnWriteArrayList<IAudioProcessor> processors = new CopyOnWriteArrayList<>();
	private volatile IAudioProvider provider = new DefaultProvider();
	private volatile IAudioProcessor finalProcessor;

	public MultiProcessor() {
		buildFinalProcessor();
	}

	public MultiProcessor(List<IAudioProcessor> initialProcessors) {
		this();
		processors.addAll(initialProcessors);
	}

	/**
	 * Adds a processor to the internal processor queue.
	 *
	 * @param processor The processor to add.
	 * @return The index of the processor.
	 */
	public synchronized int add(IAudioProcessor processor) {
		processors.add(processor);
		buildFinalProcessor();
		return processors.indexOf(processor);
	}

	/**
	 * Removes the processor from the internal processor queue at the provided index.
	 *
	 * @param index The index of the processor to remove.
	 * @return The processor removed.
	 */
	public synchronized IAudioProcessor remove(int index) {
		IAudioProcessor processor = processors.remove(index);
		buildFinalProcessor();
		return processor;
	}

	/**
	 * Removes the provided processor from the internal processor queue.
	 *
	 * @param processor The processor to remove.
	 */
	public synchronized void remove(IAudioProcessor processor) {
		processors.remove(processor);
		buildFinalProcessor();
	}

	/**
	 * Gets the position of the provided processor in the processor queue.
	 *
	 * @param processor The processor.
	 * @return The processor's index.
	 */
	public synchronized int indexOf(IAudioProcessor processor) {
		return processors.indexOf(processor);
	}

	/**
	 * Gets the processor at the specified position.
	 *
	 * @param index The position to get the processor from.
	 * @return The processor.
	 */
	public synchronized IAudioProcessor get(int index) {
		return processors.get(index);
	}

	/**
	 * Gets the size of the processor queue.
	 *
	 * @return The size of the processor queue.
	 */
	public synchronized int size() {
		return processors.size();
	}

	/**
	 * Inserts a processor in the specified position.
	 *
	 * @param index The position to insert the processor into.
	 * @param processor The processor to insert.
	 */
	public synchronized void set(int index, IAudioProcessor processor) {
		processors.set(index, processor);
		buildFinalProcessor();
	}

	private void buildFinalProcessor() {
		finalProcessor = null;

		if (processors.size() == 0) {
			finalProcessor = new DefaultProcessor();
			finalProcessor.setProvider(provider);
		} else {
			processors.forEach(processor -> {
				if (processor.setProvider(finalProcessor == null ? provider : finalProcessor)) {
					finalProcessor = processor;
				}
			});
		}
	}

	@Override
	public synchronized boolean isReady() {
		return finalProcessor.isReady();
	}

	@Override
	public synchronized byte[] provide() {
		return finalProcessor.provide();
	}

	@Override
	public synchronized int getChannels() {
		return finalProcessor.getChannels();
	}

	@Override
	public synchronized AudioEncodingType getAudioEncodingType() {
		return finalProcessor.getAudioEncodingType();
	}

	@Override
	public synchronized boolean setProvider(IAudioProvider provider) {
		this.provider = provider;
		buildFinalProcessor();
		return true;
	}
}
