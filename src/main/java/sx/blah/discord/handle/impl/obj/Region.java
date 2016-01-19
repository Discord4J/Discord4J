package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.handle.obj.IRegion;

public class Region implements IRegion {
	
	private final String id, name;
	private final boolean vip;
	
	public Region(String id, String name, boolean vip) {
		this.id = id;
		this.name = name;
		this.vip = vip;
	}
	
	@Override
	public String getID() {
		return null;
	}
	
	@Override
	public String getName() {
		return null;
	}
	
	@Override
	public boolean isVIPOnly() {
		return false;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public boolean equals(Object other) {
		return this.getClass().isAssignableFrom(other.getClass()) && ((IRegion) other).getID().equals(getID());
	}
}
