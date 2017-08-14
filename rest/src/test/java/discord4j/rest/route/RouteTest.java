package discord4j.rest.route;

import discord4j.common.pojo.ChannelPojo;
import discord4j.common.pojo.MessagePojo;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RouteTest {

	@Test
	public void testMajorVar() {
		Route<ChannelPojo> route = Routes.CHANNEL_GET;
		String majorVar = "123456";

		assertEquals(route.complete(majorVar).getMajorVar(), majorVar);
	}

	@Test
	public void testMajorVarWithOtherVars() {
		Route<MessagePojo> route = Routes.MESSAGE_GET;
		String majorVar = "12345";
		String otherVar = "6789";

		assertEquals(route.complete(majorVar, otherVar).getMajorVar(), majorVar);
	}

}
