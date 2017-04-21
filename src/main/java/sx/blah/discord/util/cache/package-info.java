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

/**
 * This package contains classes related to the internal cache system.
 *
 * Caches provide an abstraction level which allow for developers to customize how objects are stored in Discord4J. It
 * works through the {@link sx.blah.discord.util.cache.Cache} class.
 *
 * The {@link sx.blah.discord.util.cache.Cache} class acts as a middle man for Discord4J's data-related operations and
 * the backing storage implementation. Cache objects delegate all data-related operations to
 * {@link sx.blah.discord.util.cache.ICacheDelegate}.
 *
 * {@link sx.blah.discord.util.cache.ICacheDelegate}s are created on instantiation of the Cache object and are created
 * through {@link sx.blah.discord.util.cache.ICacheDelegateProvider}s. These providers are given a class relating to the
 * objects intended to be stored in the requested ICacheDelegate and provide a new instance of ICacheDelegate based on
 * this.
 *
 * {@link sx.blah.discord.util.cache.Cache#DEFAULT_PROVIDER} represents the
 * {@link sx.blah.discord.util.cache.ICacheDelegateProvider} which is used by default and
 * {@link sx.blah.discord.util.cache.Cache#IGNORING_PROVIDER} represents a provider which provides NO-OP implementations
 * of ICacheDelegate.
 *
 * <b>Implementation Notes</b>
 * <bl>
 *     <li>Objects cached by Cache should implement {@link sx.blah.discord.handle.obj.IIDLinkedObject}</li>
 *     <li>ICacheDelegates <b>MUST</b> be thread-safe in their implementations</li>
 * </bl>
 *
 * @see sx.blah.discord.api.ClientBuilder#setCacheProvider(sx.blah.discord.util.cache.ICacheDelegateProvider)
 * @see sx.blah.discord.util.cache.Cache
 * @see sx.blah.discord.util.cache.ICacheDelegate
 * @see sx.blah.discord.util.cache.ICacheDelegateProvider
 * @see sx.blah.discord.handle.obj.IIDLinkedObject
 */
package sx.blah.discord.util.cache;
