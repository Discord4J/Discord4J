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
import discord4j.rest.util.Multimap;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Value.Immutable
public interface GuildMessageSearchSpecGenerator extends Spec<Multimap<String, Object>> {

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
    default Multimap<String, Object> asRequest() {
        Multimap<String, Object> args = new Multimap<>();

        if (limit().isPresent()) args.put("limit", Collections.singletonList(limit().get()));
        if (offset().isPresent()) args.put("offset", Collections.singletonList(offset().get()));
        if (before().isPresent()) args.put("max_id", Collections.singletonList(before().get()));
        if (after().isPresent()) args.put("min_id", Collections.singletonList(after().get()));
        if (slop().isPresent()) args.put("slop", Collections.singletonList(slop().get()));
        if (content().isPresent()) args.put("content", Collections.singletonList(content().get()));
        if (mentionEveryone().isPresent()) args.put("mention_everyone", Collections.singletonList(mentionEveryone().get()));
        if (pinned().isPresent()) args.put("pinned", Collections.singletonList(pinned().get()));
        if (embedProvider().isPresent()) args.put("embed_provider", Collections.singletonList(embedProvider().get()));
        if (linkHostname().isPresent()) args.put("link_hostname", Collections.singletonList(linkHostname().get()));
        if (attachmentFilename().isPresent()) args.put("attachment_filename", Collections.singletonList(attachmentFilename().get()));
        if (attachmentExtension().isPresent()) args.put("attachment_extension", Collections.singletonList(attachmentExtension().get()));
        if (includeNsfw().isPresent()) args.put("include_nsfw", Collections.singletonList(includeNsfw().get()));

        if (sortBy().isPresent()) args.put("sort_by", Collections.singletonList(sortBy().get().toString()));
        if (sortOrder().isPresent()) args.put("sort_order", Collections.singletonList(sortOrder().get().toString()));

        if (hasEmbedType().isPresent()) args.put("embed_type", hasEmbedType().get().stream().map(SearchEmbedType::toString).collect(Collectors.toList()));

        if (channelIds().isPresent()) args.put("channel_id", channelIds().get().stream().map(Snowflake::asString).collect(Collectors.toList()));
        if (authorIds().isPresent()) args.put("author_id", authorIds().get().stream().map(Snowflake::asString).collect(Collectors.toList()));
        if (mentionUsers().isPresent()) args.put("mentions", mentionUsers().get().stream().map(Snowflake::asString).collect(Collectors.toList()));
        if (mentionRoles().isPresent()) args.put("mentions_role_id", mentionRoles().get().stream().map(Snowflake::asString).collect(Collectors.toList()));
        if (repliedUsers().isPresent()) args.put("replied_to_user_id", repliedUsers().get().stream().map(Snowflake::asString).collect(Collectors.toList()));
        if (repliedMessages().isPresent()) args.put("replied_to_message_id", repliedMessages().get().stream().map(Snowflake::asString).collect(Collectors.toList()));

        List<Object> finalAuthorTypes = new ArrayList<>();
        List<Object> finalHasTypes = new ArrayList<>();

        if (authorTypes().isPresent()) finalAuthorTypes.addAll(authorTypes().get().stream().map(AuthorType::toString).collect(Collectors.toList()));
        if (authorNotTypes().isPresent()) finalAuthorTypes.addAll(authorNotTypes().get().stream().map(authorType -> "-" + authorType).collect(Collectors.toList()));

        if (hasType().isPresent()) finalHasTypes.addAll(hasType().get().stream().map(SearchHasType::toString).collect(Collectors.toList()));
        if (hasNotType().isPresent()) finalHasTypes.addAll(hasNotType().get().stream().map(searchHasType -> "-" + searchHasType).collect(Collectors.toList()));

        if (!finalHasTypes.isEmpty()) args.put("has", finalHasTypes);
        if (!finalAuthorTypes.isEmpty()) args.put("author_type", finalAuthorTypes);

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
