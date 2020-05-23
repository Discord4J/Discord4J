package discord4j.core.object.entity;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EveryoneRoleLiveTest {

    private DiscordClient client;

    @BeforeAll
    public void setup() {
        String token = System.getenv("token");
        client = DiscordClientBuilder.create(token).build();
    }

    @Test
    public void testEveryoneRolesLive() {
        List<Guild> guilds = client.getGuilds().collectList().block();

        assert guilds != null;
        for (Guild g : guilds) {
            // Get everyone role via Guild#getEveryoneRole
            Role everyoneRole = g.getEveryoneRole().block();

            assert everyoneRole != null;
            assertTrue(everyoneRole.isEveryone());
            assertEquals(g.getId(), everyoneRole.getId());

            // Get everyone role via Role#isEveryone
            List<Role> everyoneRoles = g.getRoles().filter(Role::isEveryone).collectList().block();

            assert everyoneRoles != null;
            assertEquals(1, everyoneRoles.size());
            assertEquals(everyoneRole, everyoneRoles.get(0));
        }
    }
}
