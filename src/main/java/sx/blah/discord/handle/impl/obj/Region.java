package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.handle.obj.IRegion;

import java.util.Objects;

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
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isVIPOnly() {
		return vip;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;

		return this.getClass().isAssignableFrom(other.getClass()) && ((IRegion) other).getID().equals(getID());
	}
}
