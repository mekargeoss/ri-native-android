/**
 * 2026 Mekarge OSS and Maintainers
 * Licensed under the MIT License. See LICENSE file in the project root
 * for full license information.
 */

package oss.mekarge.a3.nat.android

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.util.Base64
import java.util.UUID

object DPoPJwt {
    fun make(method: String, url: String): String {
        val header = buildJsonObject {
            put("typ", JsonPrimitive("dpop+jwt"))
            put("alg", JsonPrimitive("ES256"))
            put("jwk", DPoPKeys.publicJwk().toJsonObject())
        }
        val claims = buildJsonObject {
            put("htm", JsonPrimitive(method.uppercase()))
            put("htu", JsonPrimitive(url))
            put("iat", JsonPrimitive(System.currentTimeMillis() / 1000))
            put("jti", JsonPrimitive(UUID.randomUUID().toString()))
        }

        val headerB64 = b64url(header.toString().toByteArray(Charsets.UTF_8))
        val claimsB64 = b64url(claims.toString().toByteArray(Charsets.UTF_8))
        val signingInput = "$headerB64.$claimsB64".toByteArray(Charsets.UTF_8)

        val sigRaw = DPoPKeys.signSigningInput(signingInput)
        val sigB64 = b64url(sigRaw)
        return "$headerB64.$claimsB64.$sigB64"
    }

    private fun b64url(data: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(data)
}
