package sx.blah.discord.util;

import sx.blah.discord.handle.obj.IMessage;

import java.util.Comparator;

/**
 * This is a comparator built to compare messages based on their timestamps
 */
public class MessageComparator implements Comparator<IMessage> {

	/**
	 * The singleton instance of the reversed message comparator
	 */
	public static final MessageComparator REVERSED = new MessageComparator(true);

	/**
	 * The singleton instance of the default message comparator
	 */
	public static final MessageComparator DEFAULT = new MessageComparator(false);

	private boolean reverse;

	public MessageComparator(boolean reverse) {
		this.reverse = reverse;
	}

	@Override
	public int compare(IMessage o1, IMessage o2) {
		return o1.equals(o2) ? 0 : (reverse ? -1 : 1) * o1.getTimestamp().compareTo(o2.getTimestamp());
	}
}
