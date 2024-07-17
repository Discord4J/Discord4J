package discord4j.core.event.domain.automod;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.object.automod.AutoModRule;
import discord4j.core.object.automod.AutoModRuleAction;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.gateway.AutoModActionExecution;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.intent.Intent;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Dispatched when an automod action is executed.
 * This event is dispatched by Discord.
 *
 * @see
 * <a href="https://discord.com/developers/docs/topics/gateway-events#auto-moderation-action-execution">Auto Moderation Action Execution</a>
 */
public class AutoModActionExecutedEvent extends Event {

    private final AutoModActionExecution data;
    private final AutoModRuleAction autoModRuleAction;

    public AutoModActionExecutedEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, AutoModActionExecution autoModActionExecution) {
        super(gateway, shardInfo);
        this.data = autoModActionExecution;
        this.autoModRuleAction = new AutoModRuleAction(gateway, autoModActionExecution.action());
    }

    /**
     * Gets the data of the AutoMod action involved in the event.
     *
     * @return The data of the AutoMod action.
     */
    public AutoModActionExecution getData() {
        return this.data;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} involved in the event.
     *
     * @return The ID of the {@link Guild}.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(this.data.guildId());
    }

    /**
     * Requests to retrieve the {@link Guild} involved in the event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the automod rule action involved in the event.
     *
     * @return The action of the automod rule.
     */
    public AutoModRuleAction getAction() {
        return autoModRuleAction;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link AutoModRule} related to this event.
     *
     * @return The ID of the {@link AutoModRule} related to this event.
     */
    public Snowflake getAutoModRuleId() {
        return Snowflake.of(this.data.ruleId());
    }

    /**
     * Requests to retrieve the {@link AutoModRule} related to this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link AutoModRule} involved in this event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<AutoModRule> getAutoModRule() {
        return getGuild().flatMap(guild -> guild.getAutoModRule(this.getAutoModRuleId()));
    }

    /**
     * Gets the trigger type of rule which was triggered
     *
     * @return The trigger type of the automod rule.
     */
    public AutoModRule.TriggerType getTriggerType() {
        return AutoModRule.TriggerType.of(this.data.ruleTriggerType());
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link User} who's generated the content which triggered the rule.
     *
     * @return The ID of the {@link User} who's triggered the rule.
     */
    public Snowflake getUserId() {
        return Snowflake.of(this.data.userId());
    }

    /**
     * Requests to retrieve the {@link User} who's generated the content which triggered the rule.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} involved in this event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser() {
        return getClient().getUserById(getUserId());
    }

    /**
     * Requests to retrieve the {@link Member} object of the {@link User} involved in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} involved in this event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getMember() {
        return getClient().getMemberById(getGuildId(), getUserId());
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link MessageChannel} involved in this event, if present.
     * {@link discord4j.core.object.entity.Message} is in.
     *
     * @return The ID of the {@link MessageChannel} involved, if present.
     */
    public Optional<Snowflake> getChannelId() {
        return this.data.channelId().toOptional().map(Snowflake::of);
    }

    /**
     * Requests to retrieve the {@link MessageChannel} involved in this event, if present.
     * {@link discord4j.core.object.entity.Message} is in.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<MessageChannel> getChannel() {
        return Mono.justOrEmpty(getChannelId()).flatMap(getClient()::getChannelById).cast(MessageChannel.class);
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link MessageChannel} involved in this event, if present.
     * {@link discord4j.core.object.entity.Message} is in.
     *
     * @return the ID of the {@link MessageChannel} involved, if present.
     */
    public Optional<Snowflake> getMessageId() {
        return this.data.messageId().toOptional().map(Snowflake::of);
    }

    /**
     * Requests to retrieve the {@link Message} involved in this event, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> getMessage() {
        if (!this.getChannelId().isPresent() || !this.getMessageId().isPresent()) {
            return Mono.empty();
        }
        return getClient().getMessageById(this.getChannelId().get(), this.getMessageId().get());
    }

    /**
     * Gets the {@link Snowflake} ID of the system {@link Message} involved in this event, if present.
     * {@link discord4j.core.object.entity.Message} is in.
     *
     * @return the ID of the system {@link Message} involved, if present.
     */
    public Optional<Snowflake> getSystemMessageId() {
        return this.data.alertSystemMessageId().toOptional().map(Snowflake::of);
    }

    /**
     * Gets the User-generated text content.
     *
     * @throws UnsupportedOperationException if the MESSAGE_CONTENT intent is not enabled and the content is empty
     * @return The contents.
     */
    public String getContent() {
        String content = this.data.content();

        if (!content.isEmpty()) {
            return content;
        }

        if (!super.getClient().getGatewayResources().getIntents().contains(Intent.MESSAGE_CONTENT)) {
            throw new UnsupportedOperationException("The MESSAGE_CONTENT intent is required to access message content!" +
                "\nSee https://github.com/Discord4J/Discord4J?tab=readme-ov-file#calling-messagegetcontent-without-enabling-the-message-content-intent" +
                " for more information.");
        }

        // We have access to the message content, but the content is empty.
        return content;
    }

    /**
     * Gets the Word or phrase configured in the rule that triggered the rule, if present.
     *
     * @return The matched keyword, if present.
     */
    public Optional<String> getMatchedKeyword() {
        return this.data.matchedKeyword();
    }

    /**
     * Gets the Substring in content that triggered the rule, if present.
     *
     * @throws UnsupportedOperationException if the MESSAGE_CONTENT intent is not enabled and the content is empty
     * @return The matched content, if present.
     */
    public Optional<String> getMatchedContent() {
        Optional<String> content = this.data.matchedContent();

        if (content.isPresent() && !content.get().isEmpty()) {
            return content;
        }

        if (!super.getClient().getGatewayResources().getIntents().contains(Intent.MESSAGE_CONTENT)) {
            throw new UnsupportedOperationException("The MESSAGE_CONTENT intent is required to access message content!" +
                "\nSee https://github.com/Discord4J/Discord4J?tab=readme-ov-file#calling-messagegetcontent-without-enabling-the-message-content-intent" +
                " for more information.");
        }

        // We have access to the message content, but the content is empty.
        return content;
    }
}
