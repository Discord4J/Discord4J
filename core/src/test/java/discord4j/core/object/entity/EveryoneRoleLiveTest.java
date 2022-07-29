package discord4j.core.object.entity;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "D4J_TEST_DISCORD", matches = "true")
public class EveryoneRoleLiveTest {

    private GatewayDiscordClient client;

    @BeforeAll
    public void setup() {
        String token = System.getenv("token");
        client = DiscordClient.create(token).login().block();
    }

    @Test
    public void testEveryoneRolesLive() {
        /*~~>*/List<Guild> guilds = client.getGuilds().collectList().block();

        assert guilds != null;
        for (Guild g : guilds) {
            // Get everyone role via Guild#getEveryoneRole
            Role everyoneRole = g.getEveryoneRole().block();

            assert everyoneRole != null;
            assertTrue(everyoneRole.isEveryone());
            assertEquals(g.getId(), everyoneRole.getId());

            // Get everyone role via Role#isEveryone
            /*~~>*/List<Role> everyoneRoles = g.getRoles().filter(Role::isEveryone).collectList().block();

            assert everyoneRoles != null;
            assertEquals(1, everyoneRoles.size());
            assertEquals(everyoneRole, everyoneRoles.get(0));
        }
    }

    @AfterAll
    public void destroy() {
        client.logout().block();
    }
}
