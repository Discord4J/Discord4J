package sx.blah.discord.api.internal.json;

import sx.blah.discord.api.internal.GatewayOps;

public class GatewayPayload {
	private final String t;
	private final Integer s;
	private final Integer op;
	private final Object d;

	public GatewayPayload(GatewayOps op, Object request) {
		this(null, null, op.ordinal(), request);
	}

	private GatewayPayload(String t, Integer s, Integer op, Object d) {
		this.t = t;
		this.s = s;
		this.op = op;
		this.d = d;
	}
}
