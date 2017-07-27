/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.util.components;

import org.junit.BeforeClass;
import org.junit.Test;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.modules.IModule;
import sx.blah.discord.modules.ModuleLoader;
import sx.blah.discord.util.Image;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ComponentTest {
	
	@BeforeClass
	public static void setupLogger() {
		Discord4J.Discord4JLogger l = (Discord4J.Discord4JLogger) Discord4J.LOGGER;
		l.setLevel(Discord4J.Discord4JLogger.Level.TRACE);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testDuplicateComponentCheck() {
		ComponentRegistry registry = getRegistry();
		registry.registerComponentProvider(IComponentProvider.singletonProvider(new TestComponent()));
	}
	
	@Test
	public void testManualInjection() {
		ComponentRegistry registry = getRegistry();
		ComponentRegistry.Injector injector = registry.injectorFor(this);
		IComponent component1 = injector.createComponent(TestComponent.class);
		IComponent component2 = injector.createComponent(TestComponent2.class);
		
		assertFalse(component1 == component2);
		
		assertTrue(component1 instanceof TestComponent);
		assertFalse(component2 instanceof TestComponent);
	}
	
	@Test
	public void testAutoInjection() {
		ComponentRegistry registry = getRegistry();
		TestInjectionSite site = new TestInjectionSite();
		registry.injectInto(site);
		site.componentChecks();
	}
	
	@Test
	public void testInferredInjections() {
		ComponentRegistry registry = getRegistry();
		TestTypeInference site = new TestTypeInference();
		registry.injectInto(site);
		site.testFields();
	}
	
	@Test
	public void testModule() {
		ComponentRegistry registry = new ComponentRegistry();
		EventDispatcher dispatcher = new EventDispatcher(null, new EventDispatcher.CallerRunsPolicy(),
				1, Runtime.getRuntime().availableProcessors() * 4, 128,
				60L, TimeUnit.SECONDS);
		ModuleLoader loader = new ModuleLoader(new IDiscordClient() {
			@Override
			public EventDispatcher getDispatcher() {
				return dispatcher;
			}
			
			@Override
			public ModuleLoader getModuleLoader() {
				return null;
			}
			
			@Override
			public List<IShard> getShards() {
				return null;
			}
			
			@Override
			public int getShardCount() {
				return 0;
			}
			
			@Override
			public String getToken() {
				return null;
			}
			
			@Override
			public void login() {
			
			}
			
			@Override
			public void logout() {
			
			}
			
			@Override
			public void changeUsername(String username) {
			
			}
			
			@Override
			public void changeAvatar(Image avatar) {
			
			}
			
			@Override
			public void changePresence(boolean isIdle) {
			
			}
			
			@Override
			public void changeStatus(Status status) {
			
			}
			
			@Override
			public void changePlayingText(String playingText) {
			
			}
			
			@Override
			public void online(String playingText) {
			
			}
			
			@Override
			public void online() {
			
			}
			
			@Override
			public void idle(String playingText) {
			
			}
			
			@Override
			public void idle() {
			
			}
			
			@Override
			public void streaming(String playingText, String streamingUrl) {
			
			}
			
			@Override
			public void mute(IGuild guild, boolean isSelfMuted) {
			
			}
			
			@Override
			public void deafen(IGuild guild, boolean isSelfDeafened) {
			
			}
			
			@Override
			public boolean isReady() {
				return false;
			}
			
			@Override
			public boolean isLoggedIn() {
				return false;
			}
			
			@Override
			public IUser getOurUser() {
				return null;
			}
			
			@Override
			public List<IChannel> getChannels(boolean includePrivate) {
				return null;
			}
			
			@Override
			public List<IChannel> getChannels() {
				return null;
			}
			
			@Override
			public IChannel getChannelByID(long channelID) {
				return null;
			}
			
			@Override
			public List<IVoiceChannel> getVoiceChannels() {
				return null;
			}
			
			@Override
			public IVoiceChannel getVoiceChannelByID(long id) {
				return null;
			}
			
			@Override
			public List<IGuild> getGuilds() {
				return null;
			}
			
			@Override
			public IGuild getGuildByID(long guildID) {
				return null;
			}
			
			@Override
			public List<IUser> getUsers() {
				return null;
			}
			
			@Override
			public IUser getUserByID(long userID) {
				return null;
			}
			
			@Override
			public IUser fetchUser(long id) {
				return null;
			}
			
			@Override
			public List<IUser> getUsersByName(String name) {
				return null;
			}
			
			@Override
			public List<IUser> getUsersByName(String name, boolean ignoreCase) {
				return null;
			}
			
			@Override
			public List<IRole> getRoles() {
				return null;
			}
			
			@Override
			public IRole getRoleByID(long roleID) {
				return null;
			}
			
			@Override
			public List<IMessage> getMessages(boolean includePrivate) {
				return null;
			}
			
			@Override
			public List<IMessage> getMessages() {
				return null;
			}
			
			@Override
			public IMessage getMessageByID(long messageID) {
				return null;
			}
			
			@Override
			public IPrivateChannel getOrCreatePMChannel(IUser user) {
				return null;
			}
			
			@Override
			public IInvite getInviteForCode(String code) {
				return null;
			}
			
			@Override
			public List<IRegion> getRegions() {
				return null;
			}
			
			@Override
			public IRegion getRegionByID(String regionID) {
				return null;
			}
			
			@Override
			public List<IVoiceChannel> getConnectedVoiceChannels() {
				return null;
			}
			
			@Override
			public String getApplicationDescription() {
				return null;
			}
			
			@Override
			public String getApplicationIconURL() {
				return null;
			}
			
			@Override
			public String getApplicationClientID() {
				return null;
			}
			
			@Override
			public String getApplicationName() {
				return null;
			}
			
			@Override
			public IUser getApplicationOwner() {
				return null;
			}
			
			@Override
			public ComponentRegistry getComponentRegistry() {
				return registry;
			}
		});
		
		TestModule module = new TestModule();
		loader.loadModule(module);
		assertNotNull(module.component);
	}
	
	private ComponentRegistry getRegistry() {
		ComponentRegistry registry = new ComponentRegistry();
		registry.registerComponentProvider(IComponentProvider.singletonProvider(new TestComponent()));
		registry.registerComponentProvider(IComponentProvider.singletonProvider(new TestComponent2()));
		registry.registerComponentProvider(IComponentProvider.singletonProvider(new TestComponent3()));
		return registry;
	}
	
	public static class TestTypeInference {
		
		@ComponentInjection
		IComponent obscureComponent;
		
		@ComponentInjection
		TestComponent concreteComponent;
		
		@ComponentInjection
		TestComponent3 concreteComponent2;
		
		public void testFields() {
			assertNotNull(obscureComponent);
			assertNotNull(concreteComponent);
			assertNotNull(concreteComponent2);
			assertFalse(concreteComponent instanceof TestComponent3);
			assertFalse(concreteComponent == concreteComponent2);
		}
		
		@ComponentInjection
		public void methodInjection(TestComponent2 component) {
			assertNotNull(component);
		}
		
		public void methodInjection2(@ComponentInjection String invalid,
									 @ComponentInjection TestComponent component,
									 @ComponentInjection TestComponent3 component2) {
			assertNull(invalid);
			assertNotNull(component);
			assertNotNull(component2);
			assertFalse(component instanceof TestComponent3);
			assertFalse(component == component2);
		}
	}
	
	public static class TestInjectionSite {
		
		@ComponentInjection("sx.blah.discord.util.components.ComponentTest$TestComponent")
		IComponent component;
		
		@ComponentInjection("sx.blah.discord.util.components.ComponentTest$TestComponent2")
		IComponent component2;
		
		public void componentChecks() {
			assertNotNull(component);
			assertNotNull(component2);
		}
		
		@ComponentInjection("sx.blah.discord.util.components.ComponentTest$TestComponent")
		public void methodInjection(IComponent component) {
			assertTrue(this.component == component);
		}
		
		public void methodInjection2(@ComponentInjection("sx.blah.discord.util.components.ComponentTest$TestComponent")
									 IComponent component1,
									 @ComponentInjection("sx.blah.discord.util.components.ComponentTest$TestComponent2")
									 IComponent component2) {
			assertTrue(this.component == component1);
			assertTrue(this.component2 == component2);
		}
	}
	
	public static class TestComponent implements IComponent {
	
	}
	
	public static class TestComponent2 implements IComponent {
	
	}
	
	public static class TestComponent3 extends TestComponent {
	
	}
	
	public static class TestModule implements IModule {
		
		@ComponentInjection("sx.blah.discord.util.components.ComponentTest$TestComponent")
		IComponent component;
		
		@Override
		public boolean enable(IDiscordClient client) {
			assertNotNull(component);
			assertTrue(component instanceof TestComponent);
			return true;
		}
		
		@Override
		public void disable() {}
		
		@Override
		public IComponentProvider[] provideComponents(IDiscordClient client) {
			return new IComponentProvider[] { IComponentProvider.singletonProvider(new TestComponent()) };
		}
		
		@Override
		public String getName() {
			return "Test";
		}
		
		@Override
		public String getAuthor() {
			return "Test";
		}
		
		@Override
		public String getVersion() {
			return "0.1";
		}
		
		@Override
		public String getMinimumDiscord4JVersion() {
			return "2.8.0";
		}
	}
}
