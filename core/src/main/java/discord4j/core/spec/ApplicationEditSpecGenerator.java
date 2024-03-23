package discord4j.core.spec;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.discordjson.json.ApplicationInfoData;
import discord4j.discordjson.json.ApplicationInfoRequest;
import discord4j.discordjson.json.InstallParamsData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static discord4j.core.spec.InternalSpecUtils.mapPossibleOptional;

@Value.Immutable(singleton = true)
interface ApplicationEditSpecGenerator extends AuditSpec<ApplicationInfoRequest> {

    Possible<String> customInstallUrl();

    Possible<String> description();

    Possible<String> roleConnectionsVerificationUrl();

    Possible<InstallParamsData> installParams();

    Possible<Integer> flags();

    Possible<Optional<Image>> icon();

    Possible<Optional<Image>> coverImage();

    Possible<String> interactionsEndpointUrl();

    Possible<List<String>> tags();

    @Override
    default ApplicationInfoRequest asRequest() {
        return ApplicationInfoRequest.builder()
            .customInstallUrl(customInstallUrl())
            .description(description())
            .roleConnectionsVerificationUrl(roleConnectionsVerificationUrl())
            .installParams(installParams())
            .flags(flags())
            .icon(mapPossibleOptional(icon(), Image::getDataUri))
            .coverImage(mapPossibleOptional(coverImage(), Image::getDataUri))
            .interactionsEndpointUrl(interactionsEndpointUrl())
            .tags(tags())
            .build();
    }

    @Value.Immutable(builder = false)
    abstract class ApplicationEditMonoGenerator extends Mono<ApplicationInfo> implements ApplicationEditSpecGenerator {
        abstract ApplicationInfo applicationInfo();

        @Override
        public void subscribe(CoreSubscriber<? super ApplicationInfo> actual) {
            applicationInfo().edit(ApplicationEditSpec.copyOf(this)).subscribe(actual);
        }

        @Override
        public abstract String toString();
    }
}
