/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.rest;

import discord4j.rest.request.Router;
import discord4j.rest.service.*;

/**
 * An aggregation of all Discord REST API resources available. Each REST resource uses its own class and uses a
 * common {@link Router} to execute requests.
 */
public final class RestClient {

    private final ApplicationService applicationService;
    private final AuditLogService auditLogService;
    private final ChannelService channelService;
    private final EmojiService emojiService;
    private final GatewayService gatewayService;
    private final GuildService guildService;
    private final InviteService inviteService;
    private final UserService userService;
    private final VoiceService voiceService;
    private final WebhookService webhookService;

    public RestClient(final Router router) {
        applicationService = new ApplicationService(router);
        auditLogService = new AuditLogService(router);
        channelService = new ChannelService(router);
        emojiService = new EmojiService(router);
        gatewayService = new GatewayService(router);
        guildService = new GuildService(router);
        inviteService = new InviteService(router);
        userService = new UserService(router);
        voiceService = new VoiceService(router);
        webhookService = new WebhookService(router);
    }

    public ApplicationService getApplicationService() {
        return applicationService;
    }

    public AuditLogService getAuditLogService() {
        return auditLogService;
    }

    public ChannelService getChannelService() {
        return channelService;
    }

    public EmojiService getEmojiService() {
        return emojiService;
    }

    public GatewayService getGatewayService() {
        return gatewayService;
    }

    public GuildService getGuildService() {
        return guildService;
    }

    public InviteService getInviteService() {
        return inviteService;
    }

    public UserService getUserService() {
        return userService;
    }

    public VoiceService getVoiceService() {
        return voiceService;
    }

    public WebhookService getWebhookService() {
        return webhookService;
    }
}
