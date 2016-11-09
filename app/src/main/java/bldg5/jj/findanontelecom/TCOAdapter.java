package bldg5.jj.findanontelecom;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TCOAdapter implements JsonSerializer<TCOption>, JsonDeserializer<TCOption>
{
    // http://ovaraksin.blogspot.com.es/2011/05/json-with-gson-and-abstract-classes.html
    @Override
    public JsonElement serialize(TCOption src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add("payloadType", new JsonPrimitive(src.getClass().getSimpleName()));
        result.add("payload", context.serialize(src, src.getClass()));

        return result;
    }

    @Override
    public TCOption deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("payloadType").getAsString();

        try {
            TCOption p = context.deserialize(jsonObject, Class.forName(type));
            return p;
        } catch (ClassNotFoundException cnfe) {
            throw new JsonParseException("Unknown element type: " + type, cnfe);
        }
    }
}