package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.onboarding.Onboarding;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.OnboardingData;
import discord4j.discordjson.json.OnboardingEditData;
import discord4j.discordjson.json.OnboardingEditPromptData;
import discord4j.discordjson.json.OnboardingPromptData;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.List;

@Value.Immutable
public interface OnboardingEditSpecGenerator extends AuditSpec<OnboardingEditData> {

    Possible<List<OnboardingEditPromptData>> prompts();

    Possible<List<Snowflake>> defaultChannelIds();

    Possible<Boolean> enabled();

    Possible<Onboarding.Mode> mode();

    @Override
    default OnboardingEditData asRequest() {
        return OnboardingEditData.builder()
            .prompts(this.prompts().toOptional().orElseThrow(() -> new IllegalStateException("Prompts are required.")))
            .defaultChannelIds(this.defaultChannelIds().toOptional().orElseThrow(() -> new IllegalStateException("Default channel IDs are required.")).stream().map(snowflake -> Id.of(snowflake.asLong())).collect(java.util.stream.Collectors.toList()))
            .enabled(this.enabled().toOptional().orElseThrow(() -> new IllegalStateException("Enabled is required.")).booleanValue())
            .mode(this.mode().toOptional().orElseThrow(() -> new IllegalStateException("Mode is required.")).getValue())
            .build();
    }

}

@Value.Immutable(builder = false)
abstract class OnboardingEditMonoGenerator extends Mono<Onboarding> implements OnboardingEditSpecGenerator {

    abstract Guild guild();

    @Override
    public void subscribe(CoreSubscriber<? super Onboarding> actual) {
        this.guild().modifyOnboarding(OnboardingEditSpec.copyOf(this)).subscribe(actual);
    }

}
