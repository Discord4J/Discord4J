package sx.blah.discord.json.requests;

public class RegisterRequest {
	public String username;
	public String invite;
	
	public RegisterRequest(String usernamme, String invite) {
		this.username = usernamme;
		this.invite = invite;
	}
}
