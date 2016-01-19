package sx.blah.discord.json.responses;

/**
 * The response received when requesting discord regions.
 */
public class RegionResponse {
	
	/**
	 * A sample hostname for the region.
	 */
	public String sample_hostname;
	/**
	 * A sample port for the region.
	 */
	public int sample_port;
	/**
	 * Whether the region is for vips only.
	 */
	public boolean vip;
	/**
	 * The region id.
	 */
	public String id;
	/**
	 * The region name.
	 */
	public String name;
}
