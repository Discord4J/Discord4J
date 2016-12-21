package sx.blah.discord.api.internal.json.requests.voice;

public class SelectProtocolRequest {
	private String protocol = "udp";
	private Data data;

	public SelectProtocolRequest(String address, int port) {
		this.data = new Data(address, port);
	}

	private static class Data {
		private String address;
		private int port;
		private String mode = "xsalsa20_poly1305";

		private Data(String address, int port) {
			this.address = address;
			this.port = port;
		}
	}
}
