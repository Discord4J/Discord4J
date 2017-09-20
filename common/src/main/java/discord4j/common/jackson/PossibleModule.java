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
package discord4j.common.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

public class PossibleModule extends Module {

	@Override
	public String getModuleName() {
		return "PossibleModule";
	}

	@Override
	public Version version() {
		return new Version(1, 0, 0, null, null, null);
	}

	@Override
	public void setupModule(SetupContext context) {
		context.addSerializers(new PossibleSerializers());
		context.addDeserializers(new PossibleDeserializers());
		context.addTypeModifier(new PossibleTypeModifier());
	}
}
