package com.mihaiim.sisgesviewbinding.others

import android.net.Uri
import com.google.gson.*
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class UriTypeAdapter : JsonDeserializer<Uri>, JsonSerializer<Uri> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): Uri {
        return Uri.parse(json.asString)
    }

    override fun serialize(
        src: Uri,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ): JsonElement {
        return JsonPrimitive(src.toString())
    }
}