package sx.blah.discord.api.internal.json.responses;

/**
 * The response received when obtaining a websocket url with recommended shards count
 */
public class GatewayBotResponse extends GatewayResponse {
        
        /**
	 * The recommended number of shards to connect with
	 */
        public int shards;
}
