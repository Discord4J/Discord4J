package discord4j.route;

import discord4j.pojo.*;

/**
 * A collection of {@link Route} object definitions.
 *
 * @since 3.0
 */
public abstract class NoJavadocRoutes {

    public static final String BASE_URL = "https://discordapp.com/api/v{version_number}";

    ////////////// Gateway Resource //////////////
    public static final Route<GatewayPojo> GATEWAY_GET     = Route.get("/gateway",     GatewayPojo.class);
    public static final Route<GatewayPojo> GATEWAY_BOT_GET = Route.get("/gateway/bot", GatewayPojo.class);

    ////////////// Channel Resource //////////////
    public static final Route<ChannelPojo>   CHANNEL_GET               = Route.get   ("/channels/{channel.id}",                                                   ChannelPojo.class);
    public static final Route<ChannelPojo>   CHANNEL_MODIFY            = Route.put   ("/channels/{channel.id}",                                                   ChannelPojo.class);
    public static final Route<ChannelPojo>   CHANNEL_MODIFY_PARTIAL    = Route.patch ("/channels/{channel.id}",                                                   ChannelPojo.class);
    public static final Route<ChannelPojo>   CHANNEL_DELETE            = Route.delete("/channels/{channel.id}",                                                   ChannelPojo.class);
    public static final Route<MessagePojo[]> MESSAGES_GET              = Route.get   ("/channels/{channel.id}/messages",                                          MessagePojo[].class);
    public static final Route<MessagePojo>   MESSAGE_GET               = Route.get   ("/channels/{channel.id}/messages/{message.id}",                             MessagePojo.class);
    public static final Route<MessagePojo>   MESSAGE_CREATE            = Route.post  ("/channels/{channel.id}/messages",                                          MessagePojo.class);
    public static final Route<Empty>         REACTION_CREATE           = Route.put   ("/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/@me",       Empty.class);
    public static final Route<Empty>         REACTION_DELETE_OWN       = Route.delete("/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/@me",       Empty.class);
    public static final Route<Empty>         REACTION_DELETE           = Route.delete("/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/{user.id}", Empty.class);
    public static final Route<UserPojo[]>    REACTIONS_GET             = Route.get   ("/channels/{channel.id}/messages/{message.id}/reactions/{emoji}",           UserPojo[].class);
    public static final Route<Empty>         REACTIONS_DELETE_ALL      = Route.delete("/channels/{channel.id}/messages/{message.id}/reactions",                   Empty.class);
    public static final Route<MessagePojo>   MESSAGE_EDIT              = Route.patch ("/channels/{channel.id}/messages/{message.id}",                             MessagePojo.class);
    public static final Route<Empty>         MESSAGE_DELETE            = Route.delete("/channels/{channel.id}/messages/{message.id}",                             Empty.class);
    public static final Route<Empty>         MESSAGE_DELETE_BULK       = Route.delete("/channels/{channel.id}/messages/bulk-delete",                              Empty.class);
    public static final Route<Empty>         CHANNEL_PERMISSIONS_EDIT  = Route.put   ("/channels/{channel.id}/permissions/{overwrite.id}",                        Empty.class);
    public static final Route<InvitePojo[]>  CHANNEL_INVITES_GET       = Route.get   ("/channels/{channel.id}/invites",                                           InvitePojo[].class);
    public static final Route<InvitePojo>    CHANNEL_INVITE_CREATE     = Route.post  ("/channels/{channel.id}/invites",                                           InvitePojo.class);
    public static final Route<Empty>         CHANNEL_PERMISSION_DELETE = Route.delete("/channels/{channel.id}/permissions/{overwrite.id}",                        Empty.class);
    public static final Route<Empty>         TYPING_INDICATOR_TRIGGER  = Route.post  ("/channels/{channel.id}/typing",                                            Empty.class);
    public static final Route<MessagePojo[]> MESSAGES_PINNED_GET       = Route.get   ("/channels/{channel.id}/pins",                                              MessagePojo[].class);
    public static final Route<Empty>         MESSAGES_PINNED_ADD       = Route.put   ("/channels/{channel.id}/pins/{message.id}",                                 Empty.class);
    public static final Route<Empty>         MESSAGES_PINNED_DELETE    = Route.delete("/channels/{channel.id}/pins/{message.id}",                                 Empty.class);
    public static final Route<Empty>         GROUP_DM_RECIPIENT_ADD    = Route.put   ("/channels/{channel.id}/recipients/{user.id}",                              Empty.class);
    public static final Route<Empty>         GROUP_DM_RECIPIENT_DELETE = Route.delete("/channels/{channel.id}/recipients/{user.id}",                              Empty.class);

    ////////////// Guild Resource //////////////
    public static final Route<GuildPojo>         GUILD_CREATE                   = Route.post  ("/guilds",                                               GuildPojo.class);
    public static final Route<GuildPojo>         GUILD_GET                      = Route.get   ("/guilds/{guild.id}",                                    GuildPojo.class);
    public static final Route<GuildPojo>         GUILD_MODIFY                   = Route.patch ("/guilds/{guild.id}",                                    GuildPojo.class);
    public static final Route<Empty>             GUILD_DELETE                   = Route.delete("/guilds/{guild.id}",                                    Empty.class);
    public static final Route<ChannelPojo[]>     GUILD_CHANNELS_GET             = Route.get   ("/guilds/{guild.id}/channels",                           ChannelPojo[].class);
    public static final Route<ChannelPojo>       GUILD_CHANNEL_CREATE           = Route.post  ("/guilds/{guild.id}/channels",                           ChannelPojo.class);
    public static final Route<Empty>             GUILD_CHANNEL_POSITIONS_MODIFY = Route.patch ("/guilds/{guild.id}/channels",                           Empty.class);
    public static final Route<GuildMemberPojo>   GUILD_MEMBER_GET               = Route.get   ("/guilds/{guild.id}/members/{user.id}",                  GuildMemberPojo.class);
    public static final Route<GuildMemberPojo[]> GUILD_MEMBERS_LIST             = Route.get   ("/guilds/{guild.id}/members",                            GuildMemberPojo[].class);
    public static final Route<GuildMemberPojo>   GUILD_MEMBER_ADD               = Route.put   ("/guilds/{guild.id}/members/{user.id}",                  GuildMemberPojo.class);
    public static final Route<Empty>             GUILD_MEMBER_MODIFY            = Route.patch ("/guilds/{guild.id}/members/{user.id}",                  Empty.class);
    public static final Route<String>            NICKNAME_MODIFY_OWN            = Route.patch ("/guilds/{guild.id}/members/@me/nick",                   String.class);
    public static final Route<Empty>             GUILD_MEMBER_ROLE_ADD          = Route.put   ("/guilds/{guild.id}/members/{user.id}/roles/{role.id}",  Empty.class);
    public static final Route<Empty>             GUILD_MEMBER_ROLE_REMOVE       = Route.delete("/guilds/{guild.id}/members/{user.id}/roles/{role.id}",  Empty.class);
    public static final Route<Empty>             GUILD_MEMBER_REMOVE            = Route.delete("/guilds/{guild.id}/members/{user.id}",                  Empty.class);
    public static final Route<UserPojo[]>        GUILD_BANS_GET                 = Route.get   ("/guilds/{guild.id}/bans",                               UserPojo[].class);
    public static final Route<Empty>             GUILD_BAN_CREATE               = Route.put   ("/guilds/{guild.id}/bans/{user.id}",                     Empty.class);
    public static final Route<Empty>             GUILD_BAN_REMOVE               = Route.delete("/guilds/{guild.id}/bans/{user.id}",                     Empty.class);
    public static final Route<RolePojo[]>        GUILD_ROLES_GET                = Route.get   ("/guilds/{guild.id}/roles",                              RolePojo[].class);
    public static final Route<RolePojo>          GUILD_ROLE_CREATE              = Route.post  ("/guilds/{guild.id}/roles",                              RolePojo.class);
    public static final Route<RolePojo[]>        GUILD_ROLE_POSITIONS_MODIFY    = Route.patch ("/guilds/{guild.id}/roles",                              RolePojo[].class);
    public static final Route<RolePojo>          GUILD_ROLE_MODIFY              = Route.patch ("/guilds/{guild.id}/roles/{role.id}",                    RolePojo.class);
    public static final Route<Empty>             GUILD_ROLE_DELETE              = Route.delete("/guilds/{guild.id}/roles/{role.id}",                    Empty.class);
    public static final Route<PruneResponse>     GUILD_PRUNE_COUNT_GET          = Route.get   ("/guilds/{guild.id}/prune",                              PruneResponse.class);
    public static final Route<PruneResponse>     GUILD_PRUNE_BEGIN              = Route.post  ("/guilds/{guild.id}/prune",                              PruneResponse.class);
    public static final Route<VoiceRegionPojo[]> GUILD_VOICE_REGION_GET         = Route.get   ("/guilds/{guild.id}/regions",                            VoiceRegionPojo[].class);
    public static final Route<InvitePojo[]>      GUILD_INVITES_GET              = Route.get   ("/guilds/{guild.id}/invites",                            InvitePojo[].class);
    public static final Route<IntegrationPojo[]> GUILD_INTEGRATIONS_GET         = Route.get   ("/guilds/{guild.id}/integrations",                       IntegrationPojo[].class);
    public static final Route<Empty>             GUILD_INTEGRATION_CREATE       = Route.post  ("/guilds/{guild.id}/integrations",                       Empty.class);
    public static final Route<Empty>             GUILD_INTEGRATION_MODIFY       = Route.patch ("/guilds/{guild.id}/integrations/{integration.id}",      Empty.class);
    public static final Route<Empty>             GUILD_INTEGRATION_DELETE       = Route.delete("/guilds/{guild.id}/integrations/{integration.id}",      Empty.class);
    public static final Route<Empty>             GUILD_INTEGRATION_SYNC         = Route.post  ("/guilds/{guild.id}/integrations/{integration.id}/sync", Empty.class);
    public static final Route<GuildEmbedPojo>    GUILD_EMBED_GET                = Route.get   ("/guilds/{guild.id}/embed",                              GuildEmbedPojo.class);
    public static final Route<GuildEmbedPojo>    GUILD_EMBED_MODIFY             = Route.patch ("/guilds/{guild.id}/embed",                              GuildEmbedPojo.class);

    ////////////// Invite Resource //////////////
    public static final Route<InvitePojo> INVITE_GET    = Route.get   ("/invites/{invite.code}", InvitePojo.class);
    public static final Route<InvitePojo> INVITE_DELETE = Route.delete("/invites/{invite.code}", InvitePojo.class);
    public static final Route<InvitePojo> INVITE_ACCEPT = Route.post  ("/invites/{invite.code}", InvitePojo.class);

    ////////////// User Resource //////////////
    public static final Route<UserPojo>             CURRENT_USER_GET        = Route.get   ("/users/@me",                   UserPojo.class);
    public static final Route<UserPojo>             USER_GET                = Route.get   ("/users/{user.id}",             UserPojo.class);
    public static final Route<UserPojo>             CURRENT_USER_MODIFY     = Route.patch ("/users/@me",                   UserPojo.class);
    public static final Route<PartialGuildPojo>     CURRENT_USER_GUILDS_GET = Route.get   ("/users/@me/guilds",            PartialGuildPojo.class);
    public static final Route<Empty>                GUILD_LEAVE             = Route.delete("/users/@me/guilds/{guild.id}", Empty.class);
    public static final Route<PrivateChannelPojo[]> USER_DMS_GET            = Route.get   ("/users/@me/channels",          PrivateChannelPojo[].class);
    public static final Route<PrivateChannelPojo>   USER_DM_CREATE          = Route.post  ("/users/@me/channels",          PrivateChannelPojo.class);
    public static final Route<PrivateChannelPojo>   GROUP_DM_CREATE         = Route.post  ("/users/@me/channels",          PrivateChannelPojo.class);
    public static final Route<ConnectionPojo[]>     USER_CONNECTIONS_GET    = Route.get   ("/users/@me/connections",       ConnectionPojo[].class);

    ////////////// Voice Resource //////////////
    public static final Route<VoiceRegionPojo[]> VOICE_REGION_LIST = Route.get("/voice/regions", VoiceRegionPojo[].class);

    ////////////// Webhook Resource //////////////
    public static final Route<WebhookPojo>   CHANNEL_WEBHOOK_CREATE = Route.post  ("/channels/{channel.id}/webhooks",               WebhookPojo.class);
    public static final Route<WebhookPojo[]> CHANNEL_WEBHOOKS_GET   = Route.get   ("/channels/{channel.id}/webhooks",               WebhookPojo[].class);
    public static final Route<WebhookPojo[]> GUILD_WEBHOOKS_GET     = Route.get   ("/guilds/{guild.id}/webhooks",                   WebhookPojo[].class);
    public static final Route<WebhookPojo>   WEBHOOK_GET            = Route.get   ("/webhooks/{webhook.id}",                        WebhookPojo.class);
    public static final Route<WebhookPojo>   WEBHOOK_TOKEN_GET      = Route.get   ("/webhooks/{webhook.id}/{webhook.token}",        WebhookPojo.class);
    public static final Route<WebhookPojo>   WEBHOOK_MODIFY         = Route.patch ("/webhooks/{webhook.id}",                        WebhookPojo.class);
    public static final Route<WebhookPojo>   WEBHOOK_TOKEN_MODIFY   = Route.patch ("/webhooks/{webhook.id}/{webhook.token}",        WebhookPojo.class);
    public static final Route<Empty>         WEBHOOK_DELETE         = Route.delete("/webhooks/{webhook.id}",                        Empty.class);
    public static final Route<Empty>         WEBHOOK_TOKEN_DELETE   = Route.delete("/webhooks/{webhook.id}/{webhook.token}",        Empty.class);
    public static final Route<Empty>         WEBHOOK_EXECUTE        = Route.post  ("/webhooks/{webhook.id}/{webhook.token}",        Empty.class);
    public static final Route<Empty>         WEBHOOK_EXECUTE_SLACK  = Route.post  ("/webhooks/{webhook.id}/{webhook.token}/slack",  Empty.class);
    public static final Route<Empty>         WEBHOOK_EXECUTE_GITHUB = Route.post  ("/webhooks/{webhook.id}/{webhook.token}/github", Empty.class);

}