/**
 * 2026 Mekarge OSS and Maintainers
 * Licensed under the MIT License. See LICENSE file in the project root
 * for full license information.
 */

package oss.mekarge.a3.nat.android

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is String -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Map<*, *> -> buildJsonObject {
        for ((k, v) in this@toJsonElement) if (k is String) put(k, v.toJsonElement())
    }

    is List<*> -> buildJsonArray { for (x in this@toJsonElement) add(x.toJsonElement()) }
    else -> JsonPrimitive(this.toString())
}

fun Map<String, Any?>.toJsonObject(): JsonObject = buildJsonObject {
    for ((k, v) in this@toJsonObject) put(k, v.toJsonElement())
}
