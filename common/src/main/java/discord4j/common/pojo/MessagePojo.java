package discord4j.common.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessagePojo {
	public String content;

	public MessagePojo(String content) {
		this.content = content;
	}
}
