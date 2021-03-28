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
package discord4j.rest.service;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.TemplateCreateRequest;
import discord4j.rest.RestTests;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import reactor.test.StepVerifier;
import reactor.util.Logger;
import reactor.util.Loggers;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "D4J_TEST_DISCORD", matches = "true")
public class TemplateServiceTest {

    private static final Logger log = Loggers.getLogger(TemplateServiceTest.class);

    private static final long guild = Snowflake.asLong(System.getenv("guild"));

    private TemplateService templateService;

    @BeforeAll
    public void setup() {
        templateService = new TemplateService(RestTests.defaultRouter());
    }

    @Test
    public void testCreateTemplate() {
        // Delete existing templates - we can only create one per guild
        StepVerifier.create(templateService.getTemplates(guild)
                .flatMap(it -> templateService.deleteTemplate(guild, it.code()))
                .then())
                .verifyComplete();

        // To create a new one and verify it's valid
        StepVerifier.create(templateService.createTemplate(guild, TemplateCreateRequest.builder()
                .name("Test template")
                .description("A template created from a test method")
                .build(), null))
                .expectNextMatches(it -> it.sourceGuildId().equals(Id.of(guild)))
                .verifyComplete();

        // Fetch existing
        StepVerifier.create(templateService.getTemplates(guild))
                .expectNextCount(1)
                .verifyComplete();
    }

}
