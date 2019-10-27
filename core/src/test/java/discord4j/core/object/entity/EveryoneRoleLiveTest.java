package discord4j.core.object.entity;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import org.junit.Before;

public class EveryoneRoleLiveTest {
    private DiscordClient client;

    @Before
    public void initialize() {
        String token = System.getenv("token");

        client = new DiscordClientBuilder(token)
                .build();
    }

//    @Test
//    public void testEveryoneRolesLive() {
//        List<Guild> guilds = client.getGuilds().collectList().block();
//
//        for (Guild g : guilds) {
//            // Get everyone role via Guild#getEveryoneRole
//            Role everyoneRole = g.getEveryoneRole().block();
//
//            assertTrue(everyoneRole.isEveryone());
//            assertEquals(g.getId(), everyoneRole.getId());
//
//            // Get everyone role via Role#isEveryone
//            List<Role> everyoneRoles = g.getRoles().filter(Role::isEveryone).collectList().block();
//
//            assertEquals(1, everyoneRoles.size());
//            assertEquals(everyoneRole, everyoneRoles.get(0));
//        }
//    }
}
