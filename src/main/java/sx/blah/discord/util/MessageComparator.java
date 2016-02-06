package sx.blah.discord.util;

import sx.blah.discord.handle.obj.IMessage;

import java.util.Comparator;

/**
 * This is a comparator built to compare messages based on their timestamps
 */
public class MessageComparator implements Comparator<IMessage> {
	
	/**
	 * The singleton instance of the message comparator
	 */
	public static final MessageComparator INSTANCE = new MessageComparator();
	
	@Override
	public int compare(IMessage o1, IMessage o2) {
		return o1.equals(o2) ? 0 : 0-o1.getTimestamp().compareTo(o2.getTimestamp());
	}
}
