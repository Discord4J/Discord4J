package discord4j.core.object.entity.channel;

import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.ChannelData;

// TODO naming
class BaseGuildChannelNotAThread extends BaseGuildChannel implements GuildChannelNotAThread {

    BaseGuildChannelNotAThread(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
    }

    // TODO: In 3.3, implementations of deprecated methods in BaseGuildChannel will be moved here
}
