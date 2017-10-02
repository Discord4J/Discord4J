package discord4j.common.jackson;

public class PossibleFilter {

	@Override
	public boolean equals(Object obj) {
		// Nulls should be included. Only exclude values which are Possible.absent
		return obj != null && Possible.class.isAssignableFrom(obj.getClass()) && ((Possible) obj).isAbsent();
	}
}
