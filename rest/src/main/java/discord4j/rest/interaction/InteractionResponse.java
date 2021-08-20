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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.rest.interaction;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.WebhookExecuteRequest;
import discord4j.discordjson.json.WebhookMessageEditRequest;
import discord4j.rest.util.MultipartRequest;
import reactor.core.publisher.Mono;

/**
 * A handler for common operations related to an interaction followup response.
 */
@Experimental
public interface InteractionResponse {

    /**
     * Return a {@link Mono} that upon subscription, will retrieve the initial response sent when accepting this
     * interaction.
     *
     * @return a {@link Mono} where, upon successful completion, emits the original message. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    Mono<MessageData> getInitialResponse();

    /**
     * Return a {@link Mono} that upon subscription, will modify the initial response sent when accepting this
     * interaction with the given raw request content.
     *
     * @param request the raw request to be sent as new initial response content
     * @return a {@link Mono} where, upon successful completion, emits the updated message. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    Mono<MessageData> editInitialResponse(WebhookMessageEditRequest request);

    /**
     * Return a {@link Mono} that upon subscription, will delete the initial response sent when accepting this
     * interaction.
     *
     * @return a {@link Mono} where, upon successful completion, emits nothing, indicating the deletion was completed.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Void> deleteInitialResponse();

    /**
     * Create a new followup message with the given content. This uses a webhook tied to the interaction ID and token.
     *
     * @param content the text content included in the followup
     * @return a {@link Mono} where, upon successful completion, emits the sent message. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    Mono<MessageData> createFollowupMessage(String content);

    /**
     * Create a new followup message using the provided request. This uses a webhook tied to the interaction ID and
     * token.
     *
     * @param request the message request to be sent as followup
     * @return a {@link Mono} where, upon successful completion, emits the sent message. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    Mono<MessageData> createFollowupMessage(MultipartRequest<WebhookExecuteRequest> request);

    /**
     * Create a new ephemeral followup message with the given content. This uses a webhook tied to the interaction ID
     * and token.
     *
     * @param content the text content included in the followup
     * @return a {@link Mono} where, upon successful completion, emits the sent message. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    Mono<MessageData> createFollowupMessageEphemeral(String content);

    /**
     * Create a new ephemeral followup message using the provided request. This uses a webhook tied to the interaction
     * ID and token.
     *
     * @param request the message request to be sent as followup
     * @return a {@link Mono} where, upon successful completion, emits the sent message. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    Mono<MessageData> createFollowupMessageEphemeral(MultipartRequest<WebhookExecuteRequest> request);

    /**
     * Modify the given message by ID using the provided request. This uses a webhook tied to the interaction ID and
     * token.
     *
     * @param messageId the message ID to be modified. You can convert IDs using {@link Snowflake} methods.
     * @param request the message request to be sent as followup
     * @param wait whether to wait until the webhook is sent or fails, influences whether you can get an error
     * through the return {@code Mono}.
     * @return a {@link Mono} where, upon successful completion, emits the edited message. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    Mono<MessageData> editFollowupMessage(long messageId, WebhookMessageEditRequest request, boolean wait);

    /**
     * Delete a followup message created under this interaction.
     *
     * @param messageId the message ID to be deleted. You can convert IDs using {@link Snowflake} methods.
     * @return a {@link Mono} where, upon successful message deletion, returns a completion signal. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    Mono<Void> deleteFollowupMessage(long messageId);
}
