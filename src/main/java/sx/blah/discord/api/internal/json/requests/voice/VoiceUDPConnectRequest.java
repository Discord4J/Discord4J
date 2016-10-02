package sx.blah.discord.api.internal.json.requests.voice;

public class VoiceUDPConnectRequest {
	private String address;
	private int port;
	private String mode = "xsalsa20_poly1305";

	public VoiceUDPConnectRequest(String address, int port) {
		this.address = address;
		this.port = port;
	}
}
