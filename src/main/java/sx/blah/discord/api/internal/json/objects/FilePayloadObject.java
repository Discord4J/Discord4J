package sx.blah.discord.api.internal.json.objects;

public class FilePayloadObject {
	public String content;
	public boolean tts;
	public EmbedObject embed;

	public FilePayloadObject() {}

	public FilePayloadObject(String content, boolean tts, EmbedObject embed) {
		this.content = content;
		this.tts = tts;
		this.embed = embed;
	}
}
