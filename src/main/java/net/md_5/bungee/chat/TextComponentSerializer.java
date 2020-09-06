package net.md_5.bungee.chat;

import com.google.gson.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.lang.reflect.Type;
import java.util.List;

public class TextComponentSerializer
        extends BaseComponentSerializer
        implements JsonSerializer<TextComponent>,
        JsonDeserializer<TextComponent> {
    @Override
    public TextComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        TextComponent component = new TextComponent();
        JsonObject object = json.getAsJsonObject();
        this.deserialize(object, component, context);
        component.setText(object.get("text").getAsString());
        return component;
    }

    @Override
    public JsonElement serialize(TextComponent src, Type typeOfSrc, JsonSerializationContext context) {
        List<BaseComponent> extra = src.getExtra();
        if (!src.hasFormatting() && (extra == null || extra.isEmpty())) {
            return new JsonPrimitive(src.getText());
        }
        JsonObject object = new JsonObject();
        this.serialize(object, src, context);
        object.addProperty("text", src.getText());
        return object;
    }
}

