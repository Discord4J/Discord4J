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
package discord4j.core.object.component;

import discord4j.core.object.component.impl.Button;
import discord4j.core.object.component.impl.Checkbox;
import discord4j.core.object.component.impl.CheckboxGroup;
import discord4j.core.object.component.impl.File;
import discord4j.core.object.component.impl.FileUpload;
import discord4j.core.object.component.impl.Label;
import discord4j.core.object.component.impl.MediaGallery;
import discord4j.core.object.component.impl.RadioGroup;
import discord4j.core.object.component.impl.TextDisplay;
import discord4j.core.object.component.impl.TextInput;
import discord4j.core.object.component.impl.Thumbnail;
import discord4j.core.object.component.impl.layout.ActionRow;
import discord4j.core.object.component.impl.layout.Container;
import discord4j.core.object.component.impl.layout.Section;
import discord4j.core.object.component.impl.layout.Separator;
import discord4j.core.object.component.impl.selectmenu.ChannelSelectMenu;
import discord4j.core.object.component.impl.selectmenu.MentionableSelectMenu;
import discord4j.core.object.component.impl.selectmenu.RoleSelectMenu;
import discord4j.core.object.component.impl.selectmenu.StringSelectMenu;
import discord4j.core.object.component.impl.selectmenu.UserSelectMenu;
import discord4j.core.object.component.kind.BaseComponent;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.component.ActionRowComponentData;
import discord4j.discordjson.json.component.ButtonComponentData;
import discord4j.discordjson.json.component.ChannelSelectComponentData;
import discord4j.discordjson.json.component.CheckboxComponentData;
import discord4j.discordjson.json.component.CheckboxGroupComponentData;
import discord4j.discordjson.json.component.ContainerComponentData;
import discord4j.discordjson.json.component.FileComponentData;
import discord4j.discordjson.json.component.FileUploadComponentData;
import discord4j.discordjson.json.component.LabelComponentData;
import discord4j.discordjson.json.component.MediaGalleryComponentData;
import discord4j.discordjson.json.component.MentionableSelectComponentData;
import discord4j.discordjson.json.component.RadioGroupComponentData;
import discord4j.discordjson.json.component.RoleSelectComponentData;
import discord4j.discordjson.json.component.SectionComponentData;
import discord4j.discordjson.json.component.SeparatorComponentData;
import discord4j.discordjson.json.component.StringSelectComponentData;
import discord4j.discordjson.json.component.TextDisplayComponentData;
import discord4j.discordjson.json.component.TextInputComponentData;
import discord4j.discordjson.json.component.ThumbnailComponentData;
import discord4j.discordjson.json.component.UserSelectComponentData;
import org.jspecify.annotations.Nullable;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * A Discord message component.
 *
 * @see <a href="https://discord.com/developers/docs/components/using-message-components">Message Components</a>
 */
public class Component<D extends ComponentData> implements BaseComponent {

    private static final Logger LOGGER = Loggers.getLogger(Component.class);

    /**
     * Constructs a {@code MessageComponent} from raw data.
     * <p>
     * The correct subtype will be chosen based on the component's {@link ComponentType}.
     *
     * @param data The raw component data.
     * @return A component with the given data.
     */
    public static Component<?> fromData(ComponentData data) {
        switch (ComponentType.of(data.type())) {
            case CONTAINER:
                return Container.of((ContainerComponentData) data);
            case SECTION:
                return Section.of((SectionComponentData) data);
            case SEPARATOR:
                return Separator.of((SeparatorComponentData) data);
            case ACTION_ROW:
                return ActionRow.of((ActionRowComponentData) data);
            case TEXT_DISPLAY:
                return TextDisplay.of((TextDisplayComponentData) data);
            case THUMBNAIL:
                return Thumbnail.of((ThumbnailComponentData) data);
            case MEDIA_GALLERY:
                return MediaGallery.of((MediaGalleryComponentData) data);
            case FILE:
                return File.of((FileComponentData) data);
            case BUTTON:
                return Button.of((ButtonComponentData) data);
            case SELECT_MENU_ROLE:
                return RoleSelectMenu.of((RoleSelectComponentData) data);
            case SELECT_MENU_CHANNEL:
                return ChannelSelectMenu.of((ChannelSelectComponentData) data);
            case SELECT_MENU_MENTIONABLE:
                return MentionableSelectMenu.of((MentionableSelectComponentData) data);
            case SELECT_MENU_USER:
                return UserSelectMenu.of((UserSelectComponentData) data);
            case SELECT_MENU_STRING:
                return StringSelectMenu.of((StringSelectComponentData) data);
            case TEXT_INPUT:
                return TextInput.of((TextInputComponentData) data);
            case LABEL:
                return Label.of((LabelComponentData) data);
            case FILE_UPLOAD:
                return FileUpload.of((FileUploadComponentData) data);
            case RADIO_GROUP:
                return RadioGroup.of((RadioGroupComponentData) data);
            case CHECKBOX_GROUP:
                return CheckboxGroup.of((CheckboxGroupComponentData) data);
            case CHECKBOX:
                return Checkbox.of((CheckboxComponentData) data);
            default: {
                Component.LOGGER.warn("Unhandled component type: " + data.type());
                return new Component<>(data);
            }
        }
    }

    private final D data;

    protected Component(D data) {
        this.data = data;
    }

    /**
     * Get the component id
     *
     * @return the component id
     */
    @Override
    public @Nullable Integer getComponentId() {
        return this.data.id().toOptional().orElse(null);
    }

    /**
     * Gets the data of the component.
     *
     * @return The data of the component.
     */
    @Override
    public D getData() {
        return this.data;
    }

    /**
     * Gets the type of the component.
     *
     * @return The type of the component.
     */
    @Override
    public ComponentType getType() {
        return ComponentType.of(this.data.type());
    }

}
