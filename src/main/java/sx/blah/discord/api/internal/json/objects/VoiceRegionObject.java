package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json voice region object.
 */
public class VoiceRegionObject {
	/**
	 * The id of the region.
	 */
	public String id;
	/**
	 * The name of the object.
	 */
	public String name;
	/**
	 * An example of the hostname for the region.
	 */
	public String sample_hostname;
	/**
	 * An example of the port for the region.
	 */
	public int sample_port;
	/**
	 * Whether the region is vip-only.
	 */
	public boolean vip;
	/**
	 * Whether discord considers this region to be optimal for the guild.
	 */
	public boolean optimal;
}
