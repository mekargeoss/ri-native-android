/**
 * 2026 Mekarge OSS and Maintainers
 * Licensed under the MIT License. See LICENSE file in the project root
 * for full license information.
 */

package oss.mekarge.a3.nat.android

import android.os.Build
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private val json = Json { ignoreUnknownKeys = true }
private val http = OkHttpClient()
private val JSON_MEDIA = "application/json".toMediaType()

@Serializable
data class BootstrapResponse(
    @SerialName("login_start_url") val loginStartUrl: String,
)

@Serializable
data class FinishResponse(
    @SerialName("session_token") val sessionToken: String,
    @SerialName("expires_at") val expiresAt: Long
)

@Serializable
data class ChallengeResponse(
    val jkt: String,
    val nonce: String
)

object Api {

    fun attestationChallenge(jwk: Map<String, String>): ChallengeResponse {
        val url =
            Config.ATTESTATION_SERVER.newBuilder().addPathSegments("attestation/challenge").build()
        val body = json.encodeToString(
            JsonObject.serializer(),
            mapOf("public_jwk" to jwk).toJsonObject()
        )
        val req = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody(JSON_MEDIA))
            .build()
        http.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw RuntimeException("HTTP ${resp.code}: $text")
            return json.decodeFromString(ChallengeResponse.serializer(), text)
        }
    }

    fun bootstrap(jkt: String, evidences: Map<String, Any>): BootstrapResponse {
        val url =
            Config.ATTESTATION_SERVER.newBuilder().addPathSegments("attestation/verify").build()
        val bodyObj = mapOf(
            "jkt" to jkt,
            "evidences" to evidences,
            "platform" to "android",
            "device_info" to mapOf(
                "sdk" to Build.VERSION.SDK_INT,
                "model" to Build.MODEL
            )
        )
        val body = json.encodeToString(
            JsonObject.serializer(),
            bodyObj.toJsonObject()
        )
        val req = Request.Builder().url(url).post(body.toRequestBody(JSON_MEDIA)).build()
        http.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw RuntimeException("HTTP ${resp.code}: $text")
            return json.decodeFromString(BootstrapResponse.serializer(), text)
        }
    }

    fun finish(appHandoffCode: String): FinishResponse {
        val url = Config.ATTESTATION_SERVER.newBuilder().addPathSegments("auth/finish").build()
        val dpop = DPoPJwt.make(method = "POST", url = url.toString())
        val body = json.encodeToString(
            JsonObject.serializer(),
            mapOf("app_handoff_code" to appHandoffCode).toJsonObject()
        )
        val req = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("DPoP", dpop)
            .post(body.toRequestBody(JSON_MEDIA))
            .build()
        http.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw RuntimeException("HTTP ${resp.code}: $text")
            return json.decodeFromString(FinishResponse.serializer(), text)
        }
    }

    fun getResource(sessionToken: String): String {
        val url = Config.ATTESTATION_SERVER.newBuilder().addPathSegments("api/resource").build()
        val dpop = DPoPJwt.make(method = "GET", url = url.toString())
        val req = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $sessionToken")
            .addHeader("DPoP", dpop)
            .get()
            .build()
        http.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw RuntimeException("HTTP ${resp.code}: $text")
            return text
        }
    }

    fun logout(sessionToken: String) {
        val url = Config.ATTESTATION_SERVER.newBuilder().addPathSegments("auth/logout").build()
        val dpop = DPoPJwt.make(method = "POST", url = url.toString())
        val req = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $sessionToken")
            .addHeader("DPoP", dpop)
            .post("".toRequestBody("text/plain".toMediaType()))
            .build()
        http.newCall(req).execute().use { resp ->
            if (resp.code != 204) {
                val text = resp.body?.string().orEmpty()
                throw RuntimeException("HTTP ${resp.code}: $text")
            }
        }
    }
}
