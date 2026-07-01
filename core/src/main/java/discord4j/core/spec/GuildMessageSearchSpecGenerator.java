package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.guildmessagesearch.AuthorType;
import discord4j.core.object.guildmessagesearch.GuildSearchResult;
import discord4j.core.object.guildmessagesearch.SearchEmbedType;
import discord4j.core.object.guildmessagesearch.SearchHasType;
import discord4j.core.object.guildmessagesearch.SortBy;
import discord4j.core.object.guildmessagesearch.SortOrder;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.service.GuildService;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Value.Immutable
public interface GuildMessageSearchSpecGenerator extends Spec<Map<String, Object>> {

    Possible<Integer> limit();

    Possible<Integer> offset();

    Possible<Snowflake> before();

    Possible<Snowflake> after();

    Possible<Integer> slop();

    Possible<String> content();

    Possible<List<Snowflake>> channelIds();

    Possible<List<Snowflake>> authorIds();

    Possible<List<Snowflake>> mentionUsers();

    Possible<List<Snowflake>> mentionRoles();

    Possible<Boolean> mentionEveryone();

    Possible<List<Snowflake>> repliedUsers();

    Possible<List<Snowflake>> repliedMessages();

    Possible<Boolean> pinned();

    Possible<List<AuthorType>> authorTypes();

    Possible<List<AuthorType>> authorNotTypes();

    Possible<List<SearchHasType>> hasType();

    Possible<List<SearchHasType>> hasNotType();

    Possible<List<SearchEmbedType>> hasEmbedType();

    Possible<List<String>> embedProvider();

    Possible<List<String>> linkHostname();

    Possible<List<String>> attachmentFilename();

    Possible<List<String>> attachmentExtension();

    Possible<SortBy> sortBy();

    Possible<SortOrder> sortOrder();

    Possible<Boolean> includeNsfw();

    @Override
    default Map<String, Object> asRequest() {
        Map<String, Object> args = new HashMap<>();

        if (limit().isPresent()) args.put("limit", limit().get());
        if (offset().isPresent()) args.put("offset", offset().get());
        if (before().isPresent()) args.put("max_id", before().get());
        if (after().isPresent()) args.put("min_id", after().get());
        if (slop().isPresent()) args.put("slop", slop().get());
        if (content().isPresent()) args.put("content", content().get());
        if (mentionEveryone().isPresent()) args.put("mention_everyone", mentionEveryone().get());
        if (pinned().isPresent()) args.put("pinned", pinned().get());
        if (embedProvider().isPresent()) args.put("embed_provider", embedProvider().get());
        if (linkHostname().isPresent()) args.put("link_hostname", linkHostname().get());
        if (attachmentFilename().isPresent()) args.put("attachment_filename", attachmentFilename().get());
        if (attachmentExtension().isPresent()) args.put("attachment_extension", attachmentExtension().get());
        if (includeNsfw().isPresent()) args.put("include_nsfw", includeNsfw().get());

        if (sortBy().isPresent()) args.put("sort_by", sortBy().get().toString());
        if (sortOrder().isPresent()) args.put("sort_order", sortOrder().get().toString());

        if (hasEmbedType().isPresent()) args.put("embed_type", hasEmbedType().get().stream().map(SearchEmbedType::toString).collect(Collectors.joining(",")));

        if (channelIds().isPresent()) args.put("channel_id", channelIds().get().stream().map(Snowflake::asString).collect(Collectors.joining(",")));
        if (authorIds().isPresent()) args.put("author_id", authorIds().get().stream().map(Snowflake::asString).collect(Collectors.joining(",")));
        if (mentionUsers().isPresent()) args.put("mentions", mentionUsers().get().stream().map(Snowflake::asString).collect(Collectors.joining(",")));
        if (mentionRoles().isPresent()) args.put("mentions_role_id", mentionRoles().get().stream().map(Snowflake::asString).collect(Collectors.joining(",")));
        if (repliedUsers().isPresent()) args.put("replied_to_user_id", repliedUsers().get().stream().map(Snowflake::asString).collect(Collectors.joining(",")));
        if (repliedMessages().isPresent()) args.put("replied_to_message_id", repliedMessages().get().stream().map(Snowflake::asString).collect(Collectors.joining(",")));

        List<String> finalAuthorTypes = new ArrayList<>();
        List<String> finalHasTypes = new ArrayList<>();

        if (authorTypes().isPresent()) finalAuthorTypes.add(authorTypes().get().stream().map(AuthorType::toString).collect(Collectors.joining(",")));
        if (authorNotTypes().isPresent()) finalAuthorTypes.add(authorTypes().get().stream().map(authorType -> "-" + authorType).collect(Collectors.joining(",")));

        if (hasType().isPresent()) finalHasTypes.add(hasType().get().stream().map(SearchHasType::toString).collect(Collectors.joining(",")));
        if (hasNotType().isPresent()) finalHasTypes.add(hasType().get().stream().map(searchHasType -> "-" + searchHasType).collect(Collectors.joining(",")));

        if (!finalHasTypes.isEmpty()) args.put("has", String.join(",", finalHasTypes));
        if (!finalAuthorTypes.isEmpty()) args.put("author_type", String.join(",", finalAuthorTypes));

        return args;
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class GuildMessageSearchMonoGenerator extends Mono<GuildSearchResult>
        implements GuildMessageSearchSpecGenerator {

    abstract long guildId();

    abstract GuildService guildService();

    abstract GatewayDiscordClient gateway();

    @Override
    public void subscribe(CoreSubscriber<? super GuildSearchResult> actual) {
        guildService().searchGuildMessages(guildId(), asRequest())
                .map(data -> new GuildSearchResult(gateway(), data))
                .subscribe(actual);
    }
}
