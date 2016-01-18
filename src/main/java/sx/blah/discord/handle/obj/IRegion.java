package sx.blah.discord.handle.obj;

/**
 * This represents a discord server region, used for voice and guild management.
 */
public interface IRegion {
	
	/**
	 * Gets the region id.
	 * 
	 * @return The id.
	 */
	String getID();
	
	/**
	 * Gets the name of the region.
	 * 
	 * @return The region name.
	 */
	String getName();
	
	/**
	 * Gets whether the region is for VIPs.
	 * 
	 * @return True if it's for VIPs, false if otherwise.
	 */
	boolean isVIPOnly();
}
